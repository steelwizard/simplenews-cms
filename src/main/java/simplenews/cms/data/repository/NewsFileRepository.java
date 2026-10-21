package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simplenews.cms.data.entity.NewsFile;

import java.util.Collection;
import java.util.List;

public interface NewsFileRepository extends JpaRepository<NewsFile, Long> {

	List<NewsFile> findByNewsIdOrderByPositionAscIdAsc(Long newsId);

	List<NewsFile> findByNewsIdInOrderByNewsIdAscPositionAscIdAsc(Collection<Long> newsIds);

	List<NewsFile> findByFileId(Long fileId);

	void deleteByNewsId(Long newsId);

	void deleteByNewsIdAndFileIdIn(Long newsId, Collection<Long> fileIds);

	void deleteByFileId(Long fileId);

	@Query("SELECT COALESCE(MAX(nf.position), -1) FROM NewsFile nf WHERE nf.newsId = :newsId")
	int findMaxPosition(@Param("newsId") Long newsId);
}
