package simplenews.cms.data.dto;


import simplenews.cms.data.entity.UserCategory;

public class UserCategoryDto {
	private Long userId;
	private Long categoryId;

	public UserCategoryDto() {}

	public UserCategoryDto(UserCategory userCategory) {
		this.userId = userCategory.getUserId();
		this.categoryId = userCategory.getCategoryId();
	}

	public UserCategoryDto(Long userId, Long categoryId) {
		this.userId = categoryId;
		this.categoryId = categoryId;
	}


	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
}
