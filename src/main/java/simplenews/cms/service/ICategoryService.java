package simplenews.cms.service;

import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.Cacheable;
import simplenews.cms.data.api.ICategory;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.entity.Category;

import java.util.List;

public interface ICategoryService {
	Category getCategoryByName(String name);
	ICategoryDto getCategoryDtoByName(String name);

	List<Category> getCategoríesByIds(List<Long> ids, boolean visibleOnly);

	List<ICategoryDto> getCategoryDtosByIds(List<Long> ids);

	List<ICategoryDto> getAllCategoryDtos(boolean visibleOnly);

	List<Category> getAllCategories(boolean visibleOnly);

	List<ICategoryDto> getRootCategories(boolean visibleOnly);

	List<ICategoryDto> getChildCategories(ICategoryDto parent, boolean visibleOnly);

	Category getCategoryById(long id);

	ICategoryDto getCategoryDtoById(long id);

	ICategoryDto saveCategory(ICategoryDto categoryDto, String user);

	@Transactional
	void deleteCategory(Long id);

	List<ICategoryDto> findMany(List<Long> ids);
}
