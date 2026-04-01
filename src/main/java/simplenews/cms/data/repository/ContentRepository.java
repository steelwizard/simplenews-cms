package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simplenews.cms.data.entity.ArticleContent;
import simplenews.cms.data.entity.Content;
import simplenews.cms.data.entity.ReviewContent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ContentRepository extends JpaRepository<Content, Long> {

	// --- Article tree queries ---

	@Query("FROM ArticleContent a WHERE a.parent IS NULL ORDER BY a.orderIndex")
	List<ArticleContent> findRootArticles();

	@Query("FROM ArticleContent a WHERE a.parent.id = :parentId ORDER BY a.orderIndex")
	List<ArticleContent> findArticlesByParent(@Param("parentId") Long parentId);

	@Query("FROM ArticleContent a WHERE a.visible = true ORDER BY a.orderIndex")
	List<ArticleContent> findAllVisibleArticles();

	// --- Review queries ---

	@Query("FROM ReviewContent r WHERE r.category.id = :categoryId AND r.visible = true ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findReviewsByCategoryVisible(@Param("categoryId") Long categoryId,
													  @Param("limit") int limit,
													  @Param("offset") int offset);

	@Query("FROM ReviewContent r WHERE r.category.id = :categoryId ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findReviewsByCategory(@Param("categoryId") Long categoryId,
											   @Param("limit") int limit,
											   @Param("offset") int offset);

	@Query("FROM ReviewContent r WHERE r.visible = true ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findAllVisibleReviews(@Param("limit") int limit, @Param("offset") int offset);

	@Query("FROM ReviewContent r ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findAllReviewsAdmin(@Param("limit") int limit, @Param("offset") int offset);

	@Query("FROM ReviewContent r WHERE r.category.id IN :categoryIds ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findReviewsByCategoryIdsAdmin(@Param("categoryIds") Set<Long> categoryIds,
													   @Param("limit") int limit, @Param("offset") int offset);

	@Query("FROM ReviewContent r WHERE r.creator = :creator AND r.category.id IN :categoryIds ORDER BY r.lastModified DESC LIMIT :limit OFFSET :offset")
	List<ReviewContent> findReviewsByCreatorAndCategoryIds(@Param("creator") String creator,
														   @Param("categoryIds") Set<Long> categoryIds,
														   @Param("limit") int limit, @Param("offset") int offset);

	@Query("FROM ReviewContent r WHERE r.key = :key")
	Optional<ReviewContent> findReviewByKey(@Param("key") String key);

	@Query("SELECT COUNT(r) > 0 FROM ReviewContent r WHERE r.key = :key")
	boolean existsReviewByKey(@Param("key") String key);
}
