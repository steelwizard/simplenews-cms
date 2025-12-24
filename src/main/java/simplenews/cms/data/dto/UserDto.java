package simplenews.cms.data.dto;

import simplenews.cms.data.entity.UserAccount;
import simplenews.cms.security.RoleEnum;

import java.sql.Timestamp;

public class UserDto {
	private Long id;
	private String username;
	private String password;
	private String displayName;
	private String email;
	//Default 0
	private Integer alg;
	private Integer wrongPasswordCount;
	private Boolean active;
	private Boolean passwordChangeRequired;
	private RoleEnum role;
	private Timestamp lastModified;
	private Timestamp created;

	public UserDto() {}

	public UserDto(Long id, String username, String password, String displayName, String email, Integer alg,
				   Integer wrongPasswordCount, Boolean active, Boolean passwordChangeRequired, RoleEnum role,
				   Timestamp lastModified, Timestamp created) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.displayName = displayName;
		this.email = email;
		this.alg = alg;
		this.wrongPasswordCount = wrongPasswordCount;
		this.active = active;
		this.passwordChangeRequired = passwordChangeRequired;
		this.role = role;
		this.lastModified = lastModified;
		this.created = created;
	}

	public UserDto(UserAccount user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.password = user.getPassword();
		this.displayName = user.getDisplayName();
		this.email = user.getEmail();
		this.alg = user.getAlg();
		this.wrongPasswordCount = user.getWrongPasswordCount();
		this.active = user.getActive();
		this.passwordChangeRequired = user.getPasswordChangeRequired();
		this.role = user.getRole();
		this.lastModified = user.getLastModified();
		this.created = user.getCreated();
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getAlg() {
		return alg;
	}

	public void setAlg(Integer alg) {
		this.alg = alg;
	}

	public Integer getWrongPasswordCount() {
		return wrongPasswordCount;
	}

	public void setWrongPasswordCount(Integer wrongPasswordCount) {
		this.wrongPasswordCount = wrongPasswordCount;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getPasswordChangeRequired() {
		return passwordChangeRequired;
	}

	public void setPasswordChangeRequired(Boolean passwordChangeRequired) {
		this.passwordChangeRequired = passwordChangeRequired;
	}

	public RoleEnum getRole() {
		return role;
	}

	public void setRole(RoleEnum role) {
		this.role = role;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
}
