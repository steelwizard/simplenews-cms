package simplenews.cms.service;

import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.exception.PasswordConfirmNotEqualException;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.dto.UserEditDto;
import simplenews.cms.service.exception.UserAlreadyExistsException;
import simplenews.cms.service.exception.UserNotFoundException;

import java.util.List;

public interface IUserService {
	UserDto registerNewUserAccount(UserEditDto userEditDto) throws PasswordConfirmNotEqualException, UserAlreadyExistsException;

	UserDto updateUser(UserEditDto userEditDto) throws PasswordConfirmNotEqualException, UserNotFoundException;

	UserDto getByUsername(String username);

	UserDto getById(Long id);

	List<UserDto> getAllUsers();

	public boolean checkRoleAtLeast(RoleEnum roleRequired, RoleEnum userRole);

	String getCurrentUserName();
}
