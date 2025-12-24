package simplenews.cms.service;

import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.UserCategoryAdminDto;
import simplenews.cms.data.dto.UserCategoryDto;

import java.util.List;
import java.util.Map;

public interface IUserCategoryService {
	List<Long> getCategoryIdsByUser(String username);

	List<UserCategoryDto> getUserCategories(Long userId);

	List<ICategoryDto> getCategoryDtosForUser(String username);

	Map<ICategoryDto, Boolean> getUserCategoryMap(Long userId);

	List<UserCategoryAdminDto> getUserCategoryDtos(String username);

	Map<ICategoryDto, Boolean> getEmptyCategoryMap();

	void saveUserCategoryMap(String username, List<Long> categoryIds);
}
