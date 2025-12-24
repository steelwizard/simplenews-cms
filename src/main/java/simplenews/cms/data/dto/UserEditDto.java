package simplenews.cms.data.dto;

import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.security.RoleEnum;

import java.util.HashMap;
import java.util.Map;

public class UserEditDto {

	private String username = "";
	private String password = "";
	private String passwordConfirm = "";
	private String email = "";
	private String displayName = "";
	private RoleEnum role;
	private Map<ICategoryDto, Boolean> userCategories = new HashMap<>();

	public UserEditDto() {}

	public UserEditDto(UserDto userDto) {
		username = userDto.getUsername();
		email = userDto.getEmail();
		displayName = userDto.getDisplayName();
		role = userDto.getRole();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public RoleEnum getRole() {
		return role;
	}

	public void setRole(RoleEnum role) {
		this.role = role;
	}

	public Map<ICategoryDto, Boolean> getUserCategories() {
		return userCategories;
	}

	public void setUserCategories(Map<ICategoryDto, Boolean> userCategories) {
		this.userCategories = userCategories;
	}
}
