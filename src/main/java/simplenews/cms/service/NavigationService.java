package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriUtils;
import simplenews.cms.data.dto.NavAdminRowDto;
import simplenews.cms.data.dto.NavLinkDto;
import simplenews.cms.data.dto.NavSyncResultDto;
import simplenews.cms.data.dto.NavigationItemDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.NavigationItem;
import simplenews.cms.data.entity.NavigationMenuEnum;
import simplenews.cms.data.entity.NavigationTypeEnum;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.data.entity.SiteSetting;
import simplenews.cms.data.entity.StaticPage;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.NavigationItemRepository;
import simplenews.cms.data.repository.ReviewTypeRepository;
import simplenews.cms.data.repository.SiteSettingRepository;
import simplenews.cms.data.repository.StaticPageRepository;
import simplenews.cms.service.event.CategoryCreatedEvent;
import simplenews.cms.service.event.PageCreatedEvent;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class NavigationService implements INavigationService {

	private static final Logger LOG = Logger.getLogger(NavigationService.class.getName());
	public static final String NAVIGATION_CACHE = "navigation";
	private static final String SEEDED_SETTING = "navigation.seeded";
	private static final String AUTO_CATEGORY_SETTING = "navigation.autoCategories";
	private static final String AUTO_PAGE_SETTING = "navigation.autoPages";
	private static final String AUTO_OFF = "OFF";
	private static final Pattern URL_PATTERN = Pattern.compile("^(/|https?://)\\S*$");

	@Autowired
	NavigationItemRepository navigationRepository;
	@Autowired
	StaticPageRepository pageRepository;
	@Autowired
	ReviewTypeRepository reviewTypeRepository;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	SiteSettingRepository siteSettingRepository;
	@Autowired
	CategoryRepository categoryRepository;

	@Override
	@Cacheable(cacheNames = NAVIGATION_CACHE, key = "#menu")
	public List<NavLinkDto> getMenu(NavigationMenuEnum menu) {
		Targets targets = loadTargets();
		List<NavigationItem> items = navigationRepository.findByMenuOrderByPositionAscIdAsc(menu);
		Map<Long, NavLinkDto> roots = new LinkedHashMap<>();
		for (NavigationItem item : items) {
			if (item.getParent() != null) continue;
			Resolved resolved = resolve(item, targets);
			if (!item.isVisible() || resolved.broken || resolved.hidden) continue;
			roots.put(item.getId(), toLink(item, resolved));
		}
		for (NavigationItem item : items) {
			if (item.getParent() == null) continue;
			NavLinkDto parent = roots.get(item.getParent());
			if (parent == null) continue;
			Resolved resolved = resolve(item, targets);
			if (!item.isVisible() || resolved.broken || resolved.hidden) continue;
			parent.getChildren().add(toLink(item, resolved));
		}
		return new ArrayList<>(roots.values());
	}

	@Override
	public List<NavAdminRowDto> getAdminRows(NavigationMenuEnum menu) {
		Targets targets = loadTargets();
		List<NavigationItem> items = navigationRepository.findByMenuOrderByPositionAscIdAsc(menu);
		List<NavigationItem> roots = items.stream().filter(i -> i.getParent() == null).toList();
		List<NavAdminRowDto> rows = new ArrayList<>();
		for (int r = 0; r < roots.size(); r++) {
			NavigationItem root = roots.get(r);
			rows.add(adminRow(root, 0, targets, r == 0, r == roots.size() - 1));
			List<NavigationItem> children = items.stream()
					.filter(i -> root.getId().equals(i.getParent())).toList();
			for (int c = 0; c < children.size(); c++) {
				rows.add(adminRow(children.get(c), 1, targets, c == 0, c == children.size() - 1));
			}
		}
		return rows;
	}

	@Override
	public NavigationItem getItem(Long id) {
		if (id == null) return null;
		return navigationRepository.findById(id).orElse(null);
	}

	@Override
	public List<NavigationItem> getParentCandidates(Long excludeId) {
		List<NavigationItem> candidates = new ArrayList<>();
		for (NavigationMenuEnum menu : NavigationMenuEnum.values()) {
			navigationRepository.findRootItems(menu).stream()
					.filter(i -> i.getType() != NavigationTypeEnum.CATEGORY_TREE)
					.filter(i -> excludeId == null || !excludeId.equals(i.getId()))
					.forEach(candidates::add);
		}
		return candidates;
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public NavigationItem save(NavigationItemDto dto, String user) {
		if (dto.getMenu() == null) throw new IllegalArgumentException("Choose a menu.");
		if (dto.getType() == null) throw new IllegalArgumentException("Choose a type.");

		NavigationItem item;
		if (dto.getId() != null) {
			item = navigationRepository.findById(dto.getId())
					.orElseThrow(() -> new IllegalArgumentException("Navigation item does not exist."));
		} else {
			item = new NavigationItem();
			item.setCreator(user);
			item.setCreated(new Timestamp(System.currentTimeMillis()));
		}
		List<NavigationItem> children = item.getId() == null
				? List.of() : navigationRepository.findByParentOrderByPositionAscIdAsc(item.getId());

		validateParent(dto, item, children);
		validateTarget(dto);

		String label = dto.getLabel() == null ? null : dto.getLabel().strip();
		if (label != null && label.isEmpty()) label = null;
		if (label != null && label.length() > 100) throw new IllegalArgumentException("Label is too long (max. 100 characters).");

		boolean moved = item.getId() == null
				|| item.getMenu() != dto.getMenu()
				|| !java.util.Objects.equals(item.getParent(), dto.getParent());

		item.setMenu(dto.getMenu());
		item.setParent(dto.getParent());
		item.setType(dto.getType());
		item.setLabel(label);
		item.setPageId(dto.getType() == NavigationTypeEnum.PAGE ? dto.getPageId() : null);
		item.setCategoryId(dto.getType() == NavigationTypeEnum.CATEGORY ? dto.getCategoryId() : null);
		item.setReviewTypeId(dto.getType() == NavigationTypeEnum.REVIEW_INDEX ? dto.getReviewTypeId() : null);
		item.setUrl(dto.getType() == NavigationTypeEnum.URL ? dto.getUrl().strip() : null);
		item.setVisible(dto.isVisible());
		item.setNewTab(dto.isNewTab());
		item.setAutoManaged(false);
		item.setLastModified(new Timestamp(System.currentTimeMillis()));
		if (moved) {
			item.setPosition(navigationRepository.findMaxPosition(dto.getMenu(), dto.getParent()) + 1);
		}
		NavigationItem saved = navigationRepository.save(item);

		for (NavigationItem child : children) {
			if (child.getMenu() != saved.getMenu()) {
				child.setMenu(saved.getMenu());
				navigationRepository.save(child);
			}
		}
		LOG.info(String.format("User %s saved navigation item %d (%s, %s)", user, saved.getId(), saved.getMenu(), saved.getType()));
		return saved;
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public void move(Long id, boolean up) {
		NavigationItem item = getItem(id);
		if (item == null) throw new IllegalArgumentException("Navigation item does not exist.");
		List<NavigationItem> siblings = new ArrayList<>(item.getParent() == null
				? navigationRepository.findRootItems(item.getMenu())
				: navigationRepository.findByParentOrderByPositionAscIdAsc(item.getParent()));
		int index = -1;
		for (int i = 0; i < siblings.size(); i++) {
			if (siblings.get(i).getId().equals(id)) index = i;
		}
		int target = up ? index - 1 : index + 1;
		if (index < 0 || target < 0 || target >= siblings.size()) return;
		siblings.set(index, siblings.get(target));
		siblings.set(target, item);
		for (int i = 0; i < siblings.size(); i++) {
			siblings.get(i).setPosition(i);
		}
		navigationRepository.saveAll(siblings);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public void delete(Long id) {
		if (id == null || !navigationRepository.existsById(id)) {
			throw new IllegalArgumentException("Navigation item does not exist.");
		}
		navigationRepository.deleteByParent(id);
		navigationRepository.deleteById(id);
		LOG.info("Deleted navigation item " + id);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public void seedDefaults() {
		if (siteSettingRepository.existsById(SEEDED_SETTING)) return;
		if (navigationRepository.count() == 0) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			NavigationItem home = seedItem(NavigationMenuEnum.HEADER, 0, "Home", NavigationTypeEnum.NEWS_INDEX, now);
			navigationRepository.save(home);
			Category news = categoryService.getCategoryByName("news");
			if (news != null) {
				NavigationItem newsItem = seedItem(NavigationMenuEnum.HEADER, 1, "News", NavigationTypeEnum.CATEGORY, now);
				newsItem.setCategoryId(news.getId());
				navigationRepository.save(newsItem);
			}
			navigationRepository.save(seedItem(NavigationMenuEnum.SIDEBAR, 0, null, NavigationTypeEnum.CATEGORY_TREE, now));
			LOG.info("Created default navigation.");
		}
		SiteSetting flag = new SiteSetting();
		flag.setKey(SEEDED_SETTING);
		flag.setValue("true");
		siteSettingRepository.save(flag);
	}

	@Override
	public NavigationMenuEnum getAutoCategoryMenu() {
		return readMenuSetting(AUTO_CATEGORY_SETTING);
	}

	@Override
	public void setAutoCategoryMenu(NavigationMenuEnum menu) {
		writeMenuSetting(AUTO_CATEGORY_SETTING, menu);
	}

	@Override
	public NavigationMenuEnum getAutoPageMenu() {
		return readMenuSetting(AUTO_PAGE_SETTING);
	}

	@Override
	public void setAutoPageMenu(NavigationMenuEnum menu) {
		writeMenuSetting(AUTO_PAGE_SETTING, menu);
	}

	private NavigationMenuEnum readMenuSetting(String key) {
		String value = siteSettingRepository.findById(key)
				.map(SiteSetting::getValue)
				.orElse(NavigationMenuEnum.HEADER.name());
		try {
			return NavigationMenuEnum.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private void writeMenuSetting(String key, NavigationMenuEnum menu) {
		SiteSetting setting = new SiteSetting();
		setting.setKey(key);
		setting.setValue(menu == null ? AUTO_OFF : menu.name());
		siteSettingRepository.save(setting);
	}

	@EventListener
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public void onPageCreated(PageCreatedEvent event) {
		NavigationMenuEnum menu = getAutoPageMenu();
		if (menu == null || event.pageId() == null) return;
		pageRepository.findById(event.pageId()).ifPresent(page -> {
			if (addPageEntry(page, menu)) {
				LOG.info("Added page " + page.getKey() + " to the " + menu + " menu.");
			}
		});
	}

	@EventListener
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public void onCategoryCreated(CategoryCreatedEvent event) {
		NavigationMenuEnum menu = getAutoCategoryMenu();
		if (menu == null || event.categoryId() == null) return;
		Map<Long, Category> categories = loadCategories();
		Category category = categories.get(event.categoryId());
		if (category != null && addCategoryEntry(category, menu, categories)) {
			LOG.info("Added category " + category.getName() + " to the " + menu + " menu.");
		}
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NAVIGATION_CACHE, allEntries = true)
	public NavSyncResultDto sync() {
		int removed = removeBrokenEntries();
		int addedCategories = 0;
		int addedPages = 0;
		int moved = 0;

		NavigationMenuEnum pageMenu = getAutoPageMenu();
		if (pageMenu != null) {
			for (StaticPage page : pageRepository.findAll(Sort.by("title", "id"))) {
				if (addPageEntry(page, pageMenu)) addedPages++;
			}
		}

		NavigationMenuEnum menu = getAutoCategoryMenu();
		if (menu != null) {
			Map<Long, Category> categories = loadCategories();
			List<Category> ordered = categories.values().stream()
					.sorted(Comparator.comparingInt((Category c) -> depth(c, categories)).thenComparing(Category::getId))
					.toList();
			for (Category category : ordered) {
				if (addCategoryEntry(category, menu, categories)) addedCategories++;
			}
			for (NavigationItem item : navigationRepository.findByMenuOrderByPositionAscIdAsc(menu)) {
				if (!item.isAutoManaged() || item.getType() != NavigationTypeEnum.CATEGORY) continue;
				Category category = categories.get(item.getCategoryId());
				if (category == null) continue;
				Long desiredParent = findParentEntry(category, menu, categories);
				if (Objects.equals(desiredParent, item.getParent())) continue;
				if (desiredParent != null && !navigationRepository.findByParentOrderByPositionAscIdAsc(item.getId()).isEmpty()) {
					continue;
				}
				item.setParent(desiredParent);
				item.setPosition(navigationRepository.findMaxPosition(menu, desiredParent) + 1);
				item.setLastModified(new Timestamp(System.currentTimeMillis()));
				navigationRepository.save(item);
				moved++;
			}
		}
		LOG.info(String.format("Navigation sync: %d categories and %d pages added, %d moved, %d removed.",
				addedCategories, addedPages, moved, removed));
		return new NavSyncResultDto(addedCategories, addedPages, moved, removed);
	}

	private boolean addPageEntry(StaticPage page, NavigationMenuEnum menu) {
		if (navigationRepository.existsByMenuAndTypeAndPageId(menu, NavigationTypeEnum.PAGE, page.getId())) {
			return false;
		}
		NavigationItem item = seedItem(menu, navigationRepository.findMaxPosition(menu, null) + 1, null,
				NavigationTypeEnum.PAGE, new Timestamp(System.currentTimeMillis()));
		item.setPageId(page.getId());
		item.setAutoManaged(true);
		navigationRepository.save(item);
		return true;
	}

	private int removeBrokenEntries() {
		Targets targets = loadTargets();
		Set<Long> removed = new HashSet<>();
		for (NavigationItem item : navigationRepository.findAll()) {
			if (removed.contains(item.getId()) || !resolve(item, targets).broken) continue;
			navigationRepository.findByParentOrderByPositionAscIdAsc(item.getId())
					.forEach(child -> removed.add(child.getId()));
			navigationRepository.deleteByParent(item.getId());
			navigationRepository.deleteById(item.getId());
			removed.add(item.getId());
		}
		return removed.size();
	}

	private boolean addCategoryEntry(Category category, NavigationMenuEnum menu, Map<Long, Category> categories) {
		if (navigationRepository.existsByMenuAndTypeAndCategoryId(menu, NavigationTypeEnum.CATEGORY, category.getId())) {
			return false;
		}
		Long parent = findParentEntry(category, menu, categories);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		NavigationItem item = seedItem(menu, navigationRepository.findMaxPosition(menu, parent) + 1, null,
				NavigationTypeEnum.CATEGORY, now);
		item.setParent(parent);
		item.setCategoryId(category.getId());
		item.setAutoManaged(true);
		navigationRepository.save(item);
		return true;
	}

	/**
	 * Top-level entry of the nearest ancestor category in this menu. Deeper categories are
	 * flattened onto it because menus only support one submenu level.
	 */
	private Long findParentEntry(Category category, NavigationMenuEnum menu, Map<Long, Category> categories) {
		Set<Long> visited = new HashSet<>();
		Long parentId = category.getParent();
		while (parentId != null && visited.add(parentId)) {
			List<NavigationItem> entries = navigationRepository
					.findByMenuAndTypeAndCategoryIdAndParentIsNull(menu, NavigationTypeEnum.CATEGORY, parentId);
			if (!entries.isEmpty()) return entries.get(0).getId();
			Category parent = categories.get(parentId);
			parentId = parent == null ? null : parent.getParent();
		}
		return null;
	}

	private static int depth(Category category, Map<Long, Category> categories) {
		Set<Long> visited = new HashSet<>();
		int depth = 0;
		Long parentId = category.getParent();
		while (parentId != null && visited.add(parentId)) {
			depth++;
			Category parent = categories.get(parentId);
			parentId = parent == null ? null : parent.getParent();
		}
		return depth;
	}

	private Map<Long, Category> loadCategories() {
		return categoryRepository.findAll().stream()
				.collect(Collectors.toMap(Category::getId, Function.identity()));
	}

	private NavigationItem seedItem(NavigationMenuEnum menu, int position, String label, NavigationTypeEnum type, Timestamp now) {
		NavigationItem item = new NavigationItem();
		item.setMenu(menu);
		item.setPosition(position);
		item.setLabel(label);
		item.setType(type);
		item.setVisible(true);
		item.setCreator("system");
		item.setCreated(now);
		item.setLastModified(now);
		return item;
	}

	private void validateParent(NavigationItemDto dto, NavigationItem item, List<NavigationItem> children) {
		if (dto.getType() == NavigationTypeEnum.CATEGORY_TREE) {
			if (dto.getMenu() != NavigationMenuEnum.SIDEBAR) {
				throw new IllegalArgumentException("The category tree can only be placed in the sidebar.");
			}
			if (dto.getParent() != null) {
				throw new IllegalArgumentException("The category tree cannot be a submenu entry.");
			}
			if (!children.isEmpty()) {
				throw new IllegalArgumentException("Remove the submenu entries before turning this into a category tree.");
			}
		}
		if (dto.getParent() == null) return;
		if (dto.getParent().equals(item.getId())) {
			throw new IllegalArgumentException("An entry cannot be its own parent.");
		}
		if (!children.isEmpty()) {
			throw new IllegalArgumentException("This entry has submenu entries, so it cannot become a submenu entry itself.");
		}
		NavigationItem parent = getItem(dto.getParent());
		if (parent == null) throw new IllegalArgumentException("The parent entry does not exist.");
		if (parent.getParent() != null) throw new IllegalArgumentException("Only one level of submenus is supported.");
		if (parent.getMenu() != dto.getMenu()) throw new IllegalArgumentException("The parent entry belongs to a different menu.");
		if (parent.getType() == NavigationTypeEnum.CATEGORY_TREE) {
			throw new IllegalArgumentException("The category tree cannot have submenu entries.");
		}
	}

	private void validateTarget(NavigationItemDto dto) {
		switch (dto.getType()) {
			case PAGE -> {
				if (dto.getPageId() == null || !pageRepository.existsById(dto.getPageId())) {
					throw new IllegalArgumentException("Choose a page.");
				}
			}
			case CATEGORY -> {
				if (dto.getCategoryId() == null || !categoryRepository.existsById(dto.getCategoryId())) {
					throw new IllegalArgumentException("Choose a category.");
				}
			}
			case REVIEW_INDEX -> {
				if (dto.getReviewTypeId() == null || !reviewTypeRepository.existsById(dto.getReviewTypeId())) {
					throw new IllegalArgumentException("Choose a review type.");
				}
			}
			case URL -> {
				String url = dto.getUrl() == null ? "" : dto.getUrl().strip();
				if (url.length() > 500 || !URL_PATTERN.matcher(url).matches()) {
					throw new IllegalArgumentException("The address must start with /, http:// or https://.");
				}
			}
			default -> { }
		}
	}

	private NavAdminRowDto adminRow(NavigationItem item, int depth, Targets targets, boolean first, boolean last) {
		Resolved resolved = resolve(item, targets);
		String label = resolved.label != null ? resolved.label : "(no heading)";
		String target = resolved.description;
		if (resolved.hidden) target += " (hidden target)";
		return new NavAdminRowDto(item, depth, label, target, resolved.broken, first, last);
	}

	private NavLinkDto toLink(NavigationItem item, Resolved resolved) {
		NavLinkDto link = new NavLinkDto(resolved.label, resolved.href, item.isNewTab());
		if (item.getType() == NavigationTypeEnum.CATEGORY_TREE) {
			link.setCategoryTree(categoryService.getRootCategories(true));
		}
		return link;
	}

	private Resolved resolve(NavigationItem item, Targets targets) {
		String custom = item.getLabel();
		switch (item.getType()) {
			case PAGE -> {
				StaticPage page = item.getPageId() == null ? null : targets.pages.get(item.getPageId());
				if (page == null) return Resolved.broken(custom, "Page (deleted)");
				return new Resolved(orDefault(custom, page.getTitle()), "/pages/" + page.getKey() + ".html",
						"Page: /pages/" + page.getKey() + ".html", false, !Boolean.TRUE.equals(page.getVisible()));
			}
			case CATEGORY -> {
				Category category = item.getCategoryId() == null ? null : targets.categories.get(item.getCategoryId());
				if (category == null) return Resolved.broken(custom, "Category (deleted)");
				String title = category.getTitle() != null ? category.getTitle() : category.getName();
				String href = "/news/category/" + UriUtils.encodePathSegment(category.getName(), StandardCharsets.UTF_8) + ".html";
				return new Resolved(orDefault(custom, title), href, "Category: " + category.getName(),
						false, !Boolean.TRUE.equals(category.getVisible()));
			}
			case REVIEW_INDEX -> {
				ReviewType type = item.getReviewTypeId() == null ? null : targets.reviewTypes.get(item.getReviewTypeId());
				if (type == null) return Resolved.broken(custom, "Review list (deleted)");
				return new Resolved(orDefault(custom, type.getTitle()), "/reviews/" + type.getKey() + ".html",
						"Review list: " + type.getTitle(), false, false);
			}
			case NEWS_INDEX -> {
				return new Resolved(orDefault(custom, "News"), "/news/index.html", "Latest news", false, false);
			}
			case CATEGORY_TREE -> {
				return new Resolved(custom, null, "Category tree", false, false);
			}
			case URL -> {
				String url = item.getUrl();
				if (url == null || !URL_PATTERN.matcher(url).matches()) return Resolved.broken(custom, "Invalid address");
				return new Resolved(orDefault(custom, url), url, "Address: " + url, false, false);
			}
			default -> {
				return Resolved.broken(custom, "Unknown type");
			}
		}
	}

	private static String orDefault(String custom, String fallback) {
		return custom != null && !custom.isBlank() ? custom : fallback;
	}

	private Targets loadTargets() {
		Targets targets = new Targets();
		targets.pages = pageRepository.findAll().stream()
				.collect(Collectors.toMap(StaticPage::getId, Function.identity()));
		targets.categories = loadCategories();
		targets.reviewTypes = reviewTypeRepository.findAll().stream()
				.collect(Collectors.toMap(ReviewType::getId, Function.identity()));
		return targets;
	}

	private static class Targets {
		Map<Long, StaticPage> pages;
		Map<Long, Category> categories;
		Map<Long, ReviewType> reviewTypes;
	}

	private record Resolved(String label, String href, String description, boolean broken, boolean hidden) {
		static Resolved broken(String label, String description) {
			return new Resolved(label != null ? label : "(broken)", null, description, true, false);
		}
	}
}
