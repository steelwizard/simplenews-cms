package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.springframework.context.annotation.Primary;

import java.util.Objects;
import java.util.StringJoiner;

@Entity(name = "user_category")
@Table(name = "user_category")
@IdClass(UserCategoryId.class)
public class UserCategory {

	public UserCategory() {}
	public UserCategory(Long userId, Long categoryId) {
		this.userId = userId;
		this.categoryId = categoryId;
	}

	@Id
	@Column(name="user_id")
	private Long userId;

	@Id
	@Column(name="category_id")
	private Long categoryId;

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

	@Override
	public String toString() {
		return new StringJoiner(", ", UserCategory.class.getSimpleName() + "[", "]")
				.add("userId=" + userId)
				.add("categoryId=" + categoryId)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserCategory category = (UserCategory) o;
		return Objects.equals(userId, category.userId) && Objects.equals(categoryId, category.categoryId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, categoryId);
	}
}
