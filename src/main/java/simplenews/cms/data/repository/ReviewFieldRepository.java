package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.ReviewField;

import java.util.List;
import java.util.Optional;

public interface ReviewFieldRepository extends JpaRepository<ReviewField, Long> {
	List<ReviewField> findByReviewTypeOrderByPositionAscIdAsc(Long reviewType);

	Optional<ReviewField> findByReviewTypeAndKey(Long reviewType, String key);

	boolean existsByReviewTypeAndKey(Long reviewType, String key);

	@Query("SELECT COALESCE(MAX(f.position), -1) FROM ReviewField f WHERE f.reviewType = :reviewType")
	int findMaxPosition(@Param("reviewType") Long reviewType);

	@Transactional
	@Modifying
	@Query("DELETE FROM ReviewField f WHERE f.reviewType = :reviewType")
	void deleteByReviewType(@Param("reviewType") Long reviewType);
}
