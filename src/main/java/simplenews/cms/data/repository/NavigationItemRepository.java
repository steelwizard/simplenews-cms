package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.NavigationItem;
import simplenews.cms.data.entity.NavigationMenuEnum;
import simplenews.cms.data.entity.NavigationTypeEnum;

import java.util.List;

public interface NavigationItemRepository extends JpaRepository<NavigationItem, Long> {
	List<NavigationItem> findByMenuOrderByPositionAscIdAsc(NavigationMenuEnum menu);

	List<NavigationItem> findByParentOrderByPositionAscIdAsc(Long parent);

	boolean existsByMenuAndTypeAndPageId(NavigationMenuEnum menu, NavigationTypeEnum type, Long pageId);

	boolean existsByMenuAndTypeAndCategoryId(NavigationMenuEnum menu, NavigationTypeEnum type, Long categoryId);

	List<NavigationItem> findByMenuAndTypeAndCategoryIdAndParentIsNull(NavigationMenuEnum menu, NavigationTypeEnum type, Long categoryId);

	@Query("FROM NavigationItem n WHERE n.menu = :menu AND n.parent IS NULL ORDER BY n.position, n.id")
	List<NavigationItem> findRootItems(@Param("menu") NavigationMenuEnum menu);

	@Query("SELECT COALESCE(MAX(n.position), -1) FROM NavigationItem n WHERE n.menu = :menu AND "
			+ "((:parent IS NULL AND n.parent IS NULL) OR n.parent = :parent)")
	int findMaxPosition(@Param("menu") NavigationMenuEnum menu, @Param("parent") Long parent);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.parent IN (SELECT i.id FROM NavigationItem i WHERE i.pageId = :pageId)")
	void deleteChildrenOfPage(@Param("pageId") Long pageId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.pageId = :pageId")
	void deleteByPage(@Param("pageId") Long pageId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.parent IN (SELECT i.id FROM NavigationItem i WHERE i.categoryId = :categoryId)")
	void deleteChildrenOfCategory(@Param("categoryId") Long categoryId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.categoryId = :categoryId")
	void deleteByCategory(@Param("categoryId") Long categoryId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.parent IN (SELECT i.id FROM NavigationItem i WHERE i.reviewTypeId = :reviewTypeId)")
	void deleteChildrenOfReviewType(@Param("reviewTypeId") Long reviewTypeId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.reviewTypeId = :reviewTypeId")
	void deleteByReviewType(@Param("reviewTypeId") Long reviewTypeId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NavigationItem n WHERE n.parent = :parent")
	void deleteByParent(@Param("parent") Long parent);
}
