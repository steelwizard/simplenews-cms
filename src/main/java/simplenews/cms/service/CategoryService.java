package simplenews.cms.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.CategoryDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.CategoryTypeEnum;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.NewsRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ApplicationScope
public class CategoryService implements ICategoryService {

	private static final Logger LOG = Logger.getLogger(CategoryService.class.getName());
	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private NewsRepository newsRepository;

//	private List<ICategoryDto> categories = null;

	@Override
	public Category getCategoryByName(String name) {
		return categoryRepository.findByName(name);
	}

	@Override
	public ICategoryDto getCategoryDtoByName(String name) {
		return new CategoryDto(getCategoryByName(name));
	}
	@Override
	public List<Category> getCategoríesByIds(List<Long> ids, boolean visibleOnly) {
		List<Category> categories = categoryRepository.findAllById(ids);
		if (visibleOnly) return categories.stream().filter(c -> c.getVisible()).collect(Collectors.toList());
		else return categories;
	}
//	@Override
//	public List<Category> getCategoríesByIds(List<Long> ids) {
//		return categoryRepository.findAllById(ids);
//	}
	@Override
	public List<ICategoryDto> getCategoryDtosByIds(List<Long> ids) {
		List<Category> categories = categoryRepository.findAllById(ids);
		List<ICategoryDto> dtos = new ArrayList<>();
		categories.forEach(c -> dtos.add(new CategoryDto(c)));
		return dtos;
	}

	@Override
	@Cacheable("categoryDto")
	public List<ICategoryDto> getAllCategoryDtos(boolean visibleOnly) {
		if (visibleOnly) return loadCategories();
		else return loadCategories().stream().filter(c -> c.isVisible()).collect(Collectors.toList());
	}
	@Cacheable("category")
	@Override
	public List<Category> getAllCategories(boolean visibleOnly) {
		List<Category> categories = categoryRepository.findAll();
		if (visibleOnly) return categories;
		else return categories.stream().filter(c -> c.getVisible()).collect(Collectors.toList());
	}
	@Override
	@Cacheable("rootCategory")
	public List<ICategoryDto> getRootCategories(boolean visibleOnly) {
		List<ICategoryDto> categoryDtos =
				categoryDtos = getAllCategoryDtos(visibleOnly).stream().filter(c -> c.getParent()==null).collect(Collectors.toList());

		return categoryDtos;
	}

	@Override
	public Category getCategoryById(long id) {
		return categoryRepository.getReferenceById(id);
	}
	@Override
	public ICategoryDto getCategoryDtoById(long id) {
		return new CategoryDto(categoryRepository.getReferenceById(id));
	}

	@CacheEvict(cacheNames={"category", "categoryDto", "rootCategory"}, allEntries = true)
	public ICategoryDto saveCategory(ICategoryDto categoryDto, String user) {
		Category category;
		LOG.info("saving category "+categoryDto.getName());
		if (categoryDto.getId() != null) {
			category = categoryRepository.getReferenceById(categoryDto.getId());
			LOG.info("Updating category #"+categoryDto.getId());

			category.setName(categoryDto.getName());
			category.setTitle(categoryDto.getTitle());
			category.setParent(categoryDto.getParent());
			category.setType(categoryDto.getType());
			category.setVisible(categoryDto.isVisible());
			category.setMetaKeywords(categoryDto.getMetaKeywords());
			category.setMetaDescription(categoryDto.getMetaDescription());
		} else {
			LOG.info("Creating new category "+categoryDto.getName());
			category = new Category(categoryDto);
			category.setName(categoryDto.getName());
			category.setTitle(categoryDto.getTitle());
			category.setParent(categoryDto.getParent());
			category.setType(CategoryTypeEnum.NEWS);
			category.setCreator(user);
			category.setCreated(new Timestamp(System.currentTimeMillis()));
			category.setVisible(categoryDto.isVisible());
			category.setMetaKeywords(categoryDto.getMetaKeywords());
			category.setMetaDescription(categoryDto.getMetaDescription());
		}
		category.setLastModified(new Timestamp(System.currentTimeMillis()));
		categoryRepository.save(category);
		refreshCategories();
		return new CategoryDto(category);
	}

	@Transactional
	@Override
	@CacheEvict(cacheNames={"category", "categoryDto", "rootCategory"}, allEntries = true)
	public void deleteCategory(Long id) {
		categoryRepository.makeRootCategoryIfChildOf(id);
		List<Category> categoryList = categoryRepository.findAll();
		categoryList.removeIf(cat -> cat.getId().equals(id));
		List<News> newslist = newsRepository.findByCategoryId(id);
		Category replace = categoryList.get(0);
		newslist.forEach(news -> news.setCategory(replace));
		newsRepository.saveAll(newslist);
		categoryRepository.deleteById(id);
		refreshCategories();
	}

	@CacheEvict(cacheNames={"category", "categoryDto", "rootCategory"}, allEntries = true)
	public void refreshCategories() {
		LOG.info("Evicting category cache.");
//		LOG.info("Loading Categories from database.");
//		List<Category> categoriesFromDb = categoryRepository.findAll();
//		List<ICategoryDto> categoryDtos = new ArrayList<>();
//		categoriesFromDb.forEach(category -> {
//			CategoryDto dto = new CategoryDto(category);
//			mapDtoChildren(dto);
//			categoryDtos.add(dto);
//		});
//		categories = categoryDtos;
	}
//	private List<ICategoryDto> loadCategories() {
//		LOG.info("Loading Categories from database.");
//		List<Category> categoriesFromDb = categoryRepository.findAll();
//		List<ICategoryDto> categoryDtos = new ArrayList<>();
//		categoriesFromDb.forEach(category -> {
//			CategoryDto dto = new CategoryDto(category);
//			mapDtoChildren(dto);
//			categoryDtos.add(dto);
//		});
//		return categoryDtos;
//	}
	private List<ICategoryDto> loadCategories() {
		LOG.info("Loading Categories from database.");

		List<Category> allCategories = categoryRepository.findAll();

		// Map all entities to DTOs and store in a lookup map by ID
		Map<Long, CategoryDto> dtoMap = allCategories.stream()
				.map(CategoryDto::new)
				.collect(Collectors.toMap(CategoryDto::getId, dto -> dto));

		// Link children to parents and identify root nodes
		List<ICategoryDto> rootCategories = new ArrayList<>();

		allCategories.forEach(category -> {
			CategoryDto dto = dtoMap.get(category.getId());
			if (category.getParent() == null) {
				rootCategories.add(dto);
			} else {
				CategoryDto parentDto = dtoMap.get(category.getParent());
				if (parentDto != null) {
					parentDto.getChildren().add(dto);
				}
			}
		});

		return rootCategories;
	}

	@Override
	public List<ICategoryDto> getChildCategories(ICategoryDto parent, boolean visibleOnly) {
		return categoryRepository.findByParent(parent.getId()).stream()
				.filter(c -> !visibleOnly || c.getVisible()) // Combined filter
				.map(CategoryDto::new) // Convert to DTO
				.peek(dto -> dto.setChildren(getChildCategories(dto, visibleOnly)))
				.collect(Collectors.toList());
	}
//	@Override
//	public List<ICategoryDto> getChildCategories(ICategoryDto parent, boolean visibleOnly) {
//		List<Category> categories = categoryRepository.findByParent(parent.getId());
//		if (visibleOnly) categories = categories.stream().filter(c -> c.getVisible()).collect(Collectors.toList());
//		List<ICategoryDto> categoryDtos = new ArrayList<>();
//		categories.forEach(c -> {
//			CategoryDto dto = new CategoryDto(c);
//			mapDtoChildren(dto);
//			categoryDtos.add(dto);
//		});
//		return categoryDtos;
//	}
//
//	private void mapDtoChildren(ICategoryDto category) {
//		List<ICategoryDto> children = getChildCategories(category, false);
//		category.setChildren(children);
//		if(!category.getChildren().isEmpty()) {
//			category.getChildren().forEach(childOfChild -> {
//				mapDtoChildren(childOfChild);
//			});
//		}
//	}

	@Override
	public List<ICategoryDto> findMany(List<Long> ids) {
		List<Category> categories = categoryRepository.findAllByIdIn(ids);
		List<ICategoryDto> dtoList = new ArrayList<>();
		categories.forEach(cat -> dtoList.add(new CategoryDto(cat)));
		return dtoList;
	}
}
