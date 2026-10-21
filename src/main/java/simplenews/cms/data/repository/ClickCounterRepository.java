package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.ClickCounter;

public interface ClickCounterRepository extends JpaRepository<ClickCounter, Long> {

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "INSERT INTO click_counter (id, total, secret) VALUES (:id, 0, :secret) ON CONFLICT (id) DO NOTHING",
			nativeQuery = true)
	int insertIfAbsent(@Param("id") long id, @Param("secret") String secret);

	@Transactional
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query(value = "UPDATE click_counter SET total = total + 1 WHERE id = :id", nativeQuery = true)
	int increment(@Param("id") long id);
}
