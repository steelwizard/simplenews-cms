package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.api.ICategory;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.UserCategoryAdminDto;
import simplenews.cms.data.dto.UserCategoryDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.UserCategory;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.UserCategoryRepository;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.security.RoleEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserCategoryService implements IUserCategoryService {

	private static final Logger LOG = Logger.getLogger(UserCategoryService.class.getName());

	@Autowired
	UserCategoryRepository userCategoryRepository;
	@Autowired
	IUserService userService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	CategoryRepository categoryRepository;

	@Override
	public List<Long> getCategoryIdsByUser(String username) {
		UserDto user = userService.getByUsername(username);
		List<UserCategory> userCategories;
		List<Long> categoryIds;
		if(userService.checkRoleAtLeast(RoleEnum.ADMIN, user.getRole())) {
			userCategories = getAdminUserCategories(user);
			categoryIds = userCategories.stream().map(uc -> uc.getCategoryId()).collect(Collectors.toList());
		} else {
			userCategories = userCategoryRepository.findAllByUserId(user.getId());
			categoryIds = userCategories.stream().map(uc -> uc.getCategoryId()).collect(Collectors.toList());
		}
		return categoryIds;
	}

	@Override
	public List<UserCategoryDto> getUserCategories(Long userId) {
		List<UserCategory> userCategories;
		List<UserCategoryDto> userCategoryDtos = new ArrayList<>();
		UserDto user = userService.getById(userId);
		if(permissionService.hasPermission(PermissionEnum.CREATE)
				&& permissionService.hasPermission(PermissionEnum.EDIT_ALL)) {
			List<ICategoryDto> categories = categoryService.getAllCategoryDtos(false);
			for (ICategoryDto c : categories) userCategoryDtos.add(new UserCategoryDto(userId, c.getId()));
		} else if(permissionService.hasPermission(PermissionEnum.CREATE) && permissionService.hasPermission(PermissionEnum.EDIT_OWN)) {
			userCategories = userCategoryRepository.findAllByUserId(user.getId());
			for (UserCategory uc : userCategories) userCategoryDtos.add(new UserCategoryDto(uc));
		}
		return userCategoryDtos;
	}

	@Override
	public List<ICategoryDto> getCategoryDtosForUser(String username) {
		List<UserCategoryDto> userCategoryDtos = getUserCategories(userService.getByUsername(username).getId());
		List<Long> categoryIds = new ArrayList<>();
		userCategoryDtos.forEach(u -> categoryIds.add(u.getCategoryId()));
		List<ICategoryDto> categories = categoryService.getCategoryDtosByIds(categoryIds);
		return categories;
	}

	@Override
	public Map<ICategoryDto, Boolean> getUserCategoryMap(Long userId) {
		Map<ICategoryDto, Boolean> categoryMap = new HashMap();
		UserDto user = userService.getById(userId);
		List<UserCategory> userCategories = userCategoryRepository.findAllByUserId(user.getId());
		List<Long> userCategoryIds = userCategories.stream().map(UserCategory::getCategoryId).toList();

		LOG.log(Level.FINE, "user Categories: ");
		userCategories.forEach(uc -> LOG.log(Level.FINE, uc.toString()));

		List<UserCategoryDto> userCategoryDtos = new ArrayList<>();
		for (UserCategory uc : userCategories) userCategoryDtos.add(new UserCategoryDto(uc));
		List<ICategoryDto> categories = categoryService.getAllCategoryDtos(false);
		for (ICategoryDto category: categories) {
			if (userCategoryIds.contains(category.getId())) categoryMap.put(category, true);
			else categoryMap.put(category, false);
		}
		return categoryMap;
	}

	@Override
	public List<UserCategoryAdminDto> getUserCategoryDtos(String username) {
		UserDto user = userService.getByUsername(username);
		Map<ICategoryDto, Boolean> map = getUserCategoryMap(user.getId());
		List<UserCategoryAdminDto> dtos = map.entrySet().stream().map(c -> {
			return new UserCategoryAdminDto(c.getKey(), c.getValue());
		}).collect(Collectors.toList());
		return dtos;
	}

	@Override
	public Map<ICategoryDto, Boolean> getEmptyCategoryMap() {
		Map<ICategoryDto, Boolean> categoryMap = new HashMap();
		List<ICategoryDto> categories = categoryService.getAllCategoryDtos(false);
		for (ICategoryDto category: categories) {
			categoryMap.put(category, false);
		}
		return categoryMap;
	}

	@Override
	@Transactional
	public void saveUserCategoryMap(String username, List<Long> categoryIds) {
		UserDto user = userService.getByUsername(username);
		userCategoryRepository.deleteAllByUserId(user.getId());
		Map<ICategoryDto, Boolean> categories = getEmptyCategoryMap();
		Set<ICategoryDto> categorySet = categories.keySet();
		List<UserCategory> userCategories = new ArrayList<>();
		categorySet.forEach(c->{
			if(categoryIds.contains(c.getId())) {
				userCategories.add(new UserCategory(user.getId(), c.getId()));
			}
		});
		userCategoryRepository.saveAll(userCategories);
	}

	public List<UserCategory> getAdminUserCategories(UserDto user) {
		List<Category> categories = categoryService.getAllCategories(false);
		List<UserCategory> userCategories = new ArrayList<>();
		categories.forEach(c -> userCategories.add(new UserCategory(user.getId(), c.getId())));
		return userCategories;
	}
}
