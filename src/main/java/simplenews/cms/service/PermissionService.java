package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.UserCategory;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.UserAccountRepository;
import simplenews.cms.data.repository.UserCategoryRepository;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.security.RoleEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class PermissionService implements IPermissionService {
	private static final Logger LOG = Logger.getLogger(PermissionService.class.getName());
	@Autowired
	UserAccountRepository userAccountRepository;
	@Autowired
	IUserService userService;
	@Autowired
	UserCategoryRepository userCategoryRepository;
	@Autowired
	CategoryRepository categoryRepository;

	@Override
	public List<PermissionEnum> getUserPermissions() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		List<PermissionEnum> permissions =new ArrayList<>();
		Set<String> roles = authentication.getAuthorities().stream()
				.map(r -> r.getAuthority()).collect(Collectors.toSet());
		for (String role : roles) {
//			LOG.info(authentication.getName() + "Role: "+role);
			switch(role) {
				case "ROLE_"+ RoleEnum.Names.ADMIN:
					permissions.add(PermissionEnum.CREATE_ALL);
					permissions.add(PermissionEnum.CREATE_ALL);
					permissions.add(PermissionEnum.EDIT_ALL);
					permissions.add(PermissionEnum.DELETE_ALL);
					permissions.add(PermissionEnum.PUBLISH_ALL);
					permissions.add(PermissionEnum.SYSTEM);
				case "ROLE_"+RoleEnum.Names.EDITOR:
					permissions.add(PermissionEnum.CREATE_ALL);
					permissions.add(PermissionEnum.EDIT_ALL);
					permissions.add(PermissionEnum.DELETE_ALL);
					permissions.add(PermissionEnum.PUBLISH_ALL);
				case "ROLE_"+RoleEnum.Names.USER:
					permissions.add(PermissionEnum.CREATE);
					permissions.add(PermissionEnum.EDIT_OWN);
					permissions.add(PermissionEnum.DELETE_OWN);
					permissions.add(PermissionEnum.PUBLISH_OWN);
				default: break;
			}
		}
		return permissions;
	}

	@Override
	public boolean hasPermission(PermissionEnum permission) {
		List<PermissionEnum> permissions = getUserPermissions();
		return permissions.contains(permission);
	}

	@Override
	public boolean canCreate(INewsDto newsDto) {
		List<PermissionEnum> permissions = getUserPermissions();
		Category category = categoryRepository.findByName(newsDto.getCategory());
		UserDto userDto = userService.getByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Long> userCategories = getCategoryIdsByUser(userDto.getUsername());
		boolean hasPermission = false;
		if(userService.checkRoleAtLeast(RoleEnum.ADMIN, userDto.getRole())) hasPermission = true;
		else if(permissions.contains(PermissionEnum.CREATE_ALL)) hasPermission = true;
		else if(permissions.contains(PermissionEnum.CREATE)
				&& userCategories.contains(category.getId())) hasPermission = true;
		return hasPermission;
	}
	@Override
	public boolean canEdit(INewsDto newsDto) {
		List<PermissionEnum> permissions = getUserPermissions();
		Category category = categoryRepository.findByName(newsDto.getCategory());
		UserDto userDto = userService.getByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Long> userCategories = getCategoryIdsByUser(userDto.getUsername());
		boolean hasPermission = false;
		if(userService.checkRoleAtLeast(RoleEnum.ADMIN, userDto.getRole())) hasPermission = true;
		else if(permissions.contains(PermissionEnum.EDIT_ALL)) hasPermission = true;
		else if(permissions.contains(PermissionEnum.EDIT_OWN)
				&& userCategories.contains(category.getId())
				&& newsDto.getCreator().equals(userDto.getUsername())) hasPermission = true;
		return hasPermission;
	}
	@Override
	public boolean canDelete(INewsDto newsDto) {
		List<PermissionEnum> permissions = getUserPermissions();
		Category category = categoryRepository.findByName(newsDto.getCategory());
		UserDto userDto = userService.getByUsername(SecurityContextHolder.getContext().getAuthentication().getName());
		List<Long> userCategories = getCategoryIdsByUser(userDto.getUsername());
		boolean hasPermission = false;
		if(userService.checkRoleAtLeast(RoleEnum.ADMIN, userDto.getRole())) hasPermission = true;
		else if(permissions.contains(PermissionEnum.DELETE_ALL)) hasPermission = true;
		else if(permissions.contains(PermissionEnum.DELETE_OWN)
				&& userCategories.contains(category.getId())
				&& newsDto.getCreator().equals(userDto.getUsername())) hasPermission = true;
		return hasPermission;
	}

	private List<Long> getCategoryIdsByUser(String username) {
		// the method in UserCategoryService can't be used to avoid circular references
		UserDto user = userService.getByUsername(username);
		List<UserCategory> userCategories = userCategoryRepository.findAllByUserId(user.getId());
		List<Long> categoryIds = userCategories.stream().map(uc -> uc.getCategoryId()).collect(Collectors.toList());
		return categoryIds;
	}
}
