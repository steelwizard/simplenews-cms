package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.NavTree;

import java.util.List;
import java.util.Optional;

public interface NavTreeRepository extends JpaRepository<NavTree, Long> {

	Optional<NavTree> findByHandle(String handle);
}
