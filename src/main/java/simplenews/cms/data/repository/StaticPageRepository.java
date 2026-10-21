package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simplenews.cms.data.entity.StaticPage;

import java.util.List;
import java.util.Optional;

public interface StaticPageRepository extends JpaRepository<StaticPage, Long> {
	Optional<StaticPage> findByKey(String key);

	boolean existsByKey(String key);

	List<StaticPage> findByVisibleTrueOrderByTitleAscIdAsc();

	@Query("FROM StaticPage p ORDER BY p.lastModified DESC LIMIT :limit OFFSET :offset")
	List<StaticPage> findOrderByLastModifiedDesc(@Param("limit") int limit, @Param("offset") int offset);
}
