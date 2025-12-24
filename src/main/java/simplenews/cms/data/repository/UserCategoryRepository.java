package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.UserCategory;
import simplenews.cms.data.entity.UserCategoryId;

import java.util.List;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UserCategoryId> {
	List<UserCategory> findAllByUserId(Long userId);
	List<UserCategory> findAllByCategoryId(Long categoryId);
	void deleteAllByUserId(Long userId);
}
