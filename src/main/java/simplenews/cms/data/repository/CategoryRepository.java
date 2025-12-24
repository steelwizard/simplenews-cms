package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simplenews.cms.data.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	Category findByName(String name);

	@Query("FROM Category WHERE parent IS NULL")
	List<Category> findRootCategories();

	List<Category> findAllByIdIn(List<Long> ids);

	List<Category> findByParent(Long parent);

	@Modifying
	@Query("UPDATE Category SET parent = null WHERE parent = :parent")
	void makeRootCategoryIfChildOf(@Param("parent") Long parent);
}
