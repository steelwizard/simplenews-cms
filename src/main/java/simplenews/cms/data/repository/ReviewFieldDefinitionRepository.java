package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.ReviewFieldDefinition;

import java.util.List;
import java.util.Optional;

public interface ReviewFieldDefinitionRepository extends JpaRepository<ReviewFieldDefinition, Long> {

	List<ReviewFieldDefinition> findByCategoryIdOrderByOrderIndex(Long categoryId);

	Optional<ReviewFieldDefinition> findByCategoryIdAndFieldKey(Long categoryId, String fieldKey);

	void deleteByCategoryId(Long categoryId);
}
