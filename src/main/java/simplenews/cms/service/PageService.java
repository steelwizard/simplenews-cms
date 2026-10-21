package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import simplenews.cms.data.dto.PageDto;
import simplenews.cms.data.entity.StaticPage;
import simplenews.cms.data.repository.NavigationItemRepository;
import simplenews.cms.data.repository.StaticPageRepository;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.event.PageCreatedEvent;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;
import simplenews.cms.service.util.SanitizeUtil;
import simplenews.cms.service.util.SlugUtil;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;

@Service
public class PageService implements IPageService {

	private static final Logger LOG = Logger.getLogger(PageService.class.getName());
	private static final int KEY_MAX = 80;

	@Autowired
	StaticPageRepository pageRepository;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	IUserService userService;
	@Autowired
	NavigationItemRepository navigationRepository;
	@Autowired
	ApplicationEventPublisher eventPublisher;

	@Override
	public List<PageDto> list(int limit, int offset) throws InsufficientPermissionException {
		requireEditor();
		return pageRepository.findOrderByLastModifiedDesc(limit, offset).stream()
				.map(PageDto::new)
				.toList();
	}

	@Override
	public PageDto getById(Long id) {
		if (id == null) {
			return null;
		}
		return pageRepository.findById(id).map(PageDto::new).orElse(null);
	}

	@Override
	@Cacheable(cacheNames = "pages", key = "#key", unless = "#result == null")
	public PageDto getByKey(String key) {
		if (key == null || key.isBlank()) {
			return null;
		}
		return pageRepository.findByKey(key).map(PageDto::new).orElse(null);
	}

	@Override
	@CacheEvict(cacheNames = {"pages", NavigationService.NAVIGATION_CACHE}, allEntries = true)
	public PageDto create(PageDto pageDto, String user) throws InsufficientPermissionException, DuplicateKeyException {
		requireEditor();
		validate(pageDto);
		prepareKey(pageDto);
		if (pageRepository.existsByKey(pageDto.getKey())) {
			throw new DuplicateKeyException("key already exists");
		}

		Timestamp now = new Timestamp(System.currentTimeMillis());
		StaticPage page = new StaticPage();
		page.setTitle(pageDto.getTitle().strip());
		page.setKey(pageDto.getKey());
		page.setContent(sanitize(pageDto.getContent()));
		page.setVisible(Boolean.TRUE.equals(pageDto.getVisible()));
		page.setCreator(user);
		page.setLastModifiedBy(user);
		page.setCreated(now);
		page.setLastModified(now);
		page.setMetaKeywords(blankToNull(pageDto.getMetaKeywords()));
		page.setMetaDescription(blankToNull(pageDto.getMetaDescription()));

		StaticPage saved = saveUnique(page);
		LOG.info(String.format("User %s created page %d %s: %s", user, saved.getId(), saved.getKey(), saved.getTitle()));
		eventPublisher.publishEvent(new PageCreatedEvent(saved.getId()));
		return new PageDto(saved);
	}

	@Override
	@CacheEvict(cacheNames = {"pages", NavigationService.NAVIGATION_CACHE}, allEntries = true)
	public PageDto update(Long id, PageDto pageDto, String user) throws InsufficientPermissionException, DuplicateKeyException {
		requireEditor();
		validate(pageDto);
		prepareKey(pageDto);

		StaticPage page = pageRepository.findById(id).orElse(null);
		if (page == null) {
			throw new IllegalArgumentException("Page does not exist");
		}
		if (!pageDto.getKey().equals(page.getKey()) && pageRepository.existsByKey(pageDto.getKey())) {
			throw new DuplicateKeyException("key already exists");
		}

		page.setTitle(pageDto.getTitle().strip());
		page.setKey(pageDto.getKey());
		page.setContent(sanitize(pageDto.getContent()));
		page.setVisible(Boolean.TRUE.equals(pageDto.getVisible()));
		page.setLastModifiedBy(user);
		page.setLastModified(new Timestamp(System.currentTimeMillis()));
		page.setMetaKeywords(blankToNull(pageDto.getMetaKeywords()));
		page.setMetaDescription(blankToNull(pageDto.getMetaDescription()));

		StaticPage saved = saveUnique(page);
		LOG.info(String.format("User %s updated page %d %s: %s", user, saved.getId(), saved.getKey(), saved.getTitle()));
		return new PageDto(saved);
	}

	@Override
	@CacheEvict(cacheNames = {"pages", NavigationService.NAVIGATION_CACHE}, allEntries = true)
	public void delete(Long id) throws InsufficientPermissionException {
		requireEditor();
		if (id == null || !pageRepository.existsById(id)) {
			throw new IllegalArgumentException("Page does not exist");
		}
		navigationRepository.deleteChildrenOfPage(id);
		navigationRepository.deleteByPage(id);
		pageRepository.deleteById(id);
		LOG.info("User " + userService.getCurrentUserName() + " deleted page " + id);
	}

	@Override
	public void prepareKey(PageDto pageDto) {
		pageDto.setKey(normalizeKey(pageDto.getKey(), pageDto.getTitle()));
	}

	private StaticPage saveUnique(StaticPage page) throws DuplicateKeyException {
		try {
			return pageRepository.save(page);
		} catch (DataIntegrityViolationException e) {
			throw new DuplicateKeyException("key already exists");
		}
	}

	private void validate(PageDto pageDto) {
		if (pageDto.getTitle() == null || pageDto.getTitle().isBlank()) {
			throw new IllegalArgumentException("Title is required");
		}
		if (pageDto.getTitle().strip().length() > 200) {
			throw new IllegalArgumentException("Title is too long");
		}
	}

	private void requireEditor() throws InsufficientPermissionException {
		if (!permissionService.hasPermission(PermissionEnum.EDIT_ALL)) {
			throw new InsufficientPermissionException("Only editors and admins can change pages");
		}
	}

	static String normalizeKey(String key, String title) {
		String source = (key == null || key.isBlank()) ? title : key;
		String slug = SlugUtil.slugify(source, KEY_MAX);
		if (slug.isEmpty()) {
			slug = "page-" + System.currentTimeMillis();
		}
		return slug;
	}

	private static String sanitize(String content) {
		return SanitizeUtil.sanitize(content == null ? "" : content);
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.strip();
	}
}
