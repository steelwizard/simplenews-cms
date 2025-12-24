package simplenews.cms.data.dto;

import simplenews.cms.data.api.ICategoryDto;

import java.util.Objects;
import java.util.StringJoiner;

public class UserCategoryAdminDto {

	public UserCategoryAdminDto(ICategoryDto cateory, Boolean enabled) {
		this.cateory = cateory;
		this.enabled = enabled;
	}

	ICategoryDto cateory = new CategoryDto();
	Boolean enabled = false;

	public ICategoryDto getCategory() {
		return cateory;
	}

	public void setCategory(ICategoryDto category) {
		this.cateory = category;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserCategoryAdminDto dto = (UserCategoryAdminDto) o;
		return Objects.equals(cateory, dto.cateory) && Objects.equals(enabled, dto.enabled);
	}

	@Override
	public int hashCode() {
		return Objects.hash(cateory, enabled);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", UserCategoryAdminDto.class.getSimpleName() + "[", "]")
				.add("cateory=" + cateory)
				.add("enabled=" + enabled)
				.toString();
	}
}
