package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.ReviewType;

import java.util.List;
import java.util.Optional;

public interface ReviewTypeRepository extends JpaRepository<ReviewType, Long> {
	Optional<ReviewType> findByKey(String key);

	boolean existsByKey(String key);

	List<ReviewType> findAllByOrderByTitleAsc();
}
