package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.NavNode;

import java.util.List;

public interface NavNodeRepository extends JpaRepository<NavNode, Long> {

	List<NavNode> findByTreeIdOrderByOrderIndex(Long treeId);
}
