package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	@Modifying
	@Query("UPDATE News n SET n.phpbbReplyCount = :count WHERE n.phpbbTopicId = :topicId")
	int updateReplyCount(@Param("topicId") Long topicId, @Param("count") int count);

	/**
	 * Pass -1 as filterField and ratingField when unused; null parameters cannot be typed by Postgres here.
	 */
	@Query(nativeQuery = true, value =
			" SELECT n.* FROM news n " +
					" JOIN category c ON c.id = n.category " +
					" LEFT JOIN news_field_value r ON r.news_id = n.id AND r.field_id = :ratingField " +
					" WHERE c.review_type = :reviewType AND n.visible = true " +
					" AND (:filterField = -1 OR EXISTS (SELECT 1 FROM news_field_value f " +
					"      WHERE f.news_id = n.id AND f.field_id = :filterField " +
					"      AND lower(f.value_text) = lower(:filterValue))) " +
					" ORDER BY CASE WHEN :byRating THEN r.value_number END DESC NULLS LAST, n.last_modified DESC " +
					" LIMIT :limit OFFSET :offset ")
	List<News> findReviews(@Param("reviewType") long reviewType,
						   @Param("ratingField") long ratingField,
						   @Param("filterField") long filterField,
						   @Param("filterValue") String filterValue,
						   @Param("byRating") boolean byRating,
						   @Param("limit") int limit,
						   @Param("offset") int offset);
}
