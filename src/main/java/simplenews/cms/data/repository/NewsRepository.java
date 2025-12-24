package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simplenews.cms.data.entity.News;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface NewsRepository extends JpaRepository<News, Long> {
	@Query(value =
			" FROM News n " +
					" WHERE visible = true " +
					" ORDER BY lastModified desc " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findByVisibleTrueOrderByLastModifiedDesc(int limit, int offset);
	@Query(value =
			" FROM News n " +
					" ORDER BY lastModified desc " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findOrderByLastModifiedDesc(int limit, int offset);
	@Query(value =
			" FROM News n " +
					" WHERE n.category.id = :categoryId " +
					" AND visible = true " +
					" ORDER BY lastModified desc " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findByCategoryAndVisibleTrueOrderByLastModifiedDesc(Long categoryId, int limit, int offset);
	List<News> findByCategoryId(Long categoryId);
	@Query(value =
			" FROM News n " +
					" WHERE n.category.id in :categoryIds " +
					" AND n.creator = :creator" +
					" ORDER BY n.lastModified desc " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findAllByCreatorAndCategoryIdIn(@Param("creator")String creator,
											   @Param("categoryIds") Set<Long> categoryIds,
											   @Param("limit") int limit, @Param("offset") int offset);
	@Query(value =
			" FROM News n " +
					" WHERE n.category.id in :categoryIds " +
					" ORDER BY n.lastModified desc " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findAllByCategoryIdIn(@Param("categoryIds") Set<Long> categoryIds,
									 @Param("limit") int limit, @Param("offset") int offset);
	Optional<News> findByKey(String key);
	boolean existsByKey(String key);
}
