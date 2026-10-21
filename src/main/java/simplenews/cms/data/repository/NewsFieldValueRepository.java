package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.NewsFieldValue;
import simplenews.cms.data.entity.NewsFieldValueId;

import java.util.Collection;
import java.util.List;

public interface NewsFieldValueRepository extends JpaRepository<NewsFieldValue, NewsFieldValueId> {
	List<NewsFieldValue> findByNewsId(Long newsId);

	List<NewsFieldValue> findByNewsIdIn(Collection<Long> newsIds);

	boolean existsByFieldId(Long fieldId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NewsFieldValue v WHERE v.newsId = :newsId")
	void deleteByNews(@Param("newsId") Long newsId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NewsFieldValue v WHERE v.fieldId = :fieldId")
	void deleteByField(@Param("fieldId") Long fieldId);

	@Transactional
	@Modifying
	@Query("DELETE FROM NewsFieldValue v WHERE v.fieldId IN (SELECT f.id FROM ReviewField f WHERE f.reviewType = :reviewType)")
	void deleteByReviewType(@Param("reviewType") Long reviewType);
}
