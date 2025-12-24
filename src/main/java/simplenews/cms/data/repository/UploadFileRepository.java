package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import simplenews.cms.data.entity.UploadFile;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
	@Query(value =
			" FROM UploadFile ORDER BY created desc LIMIT :limit OFFSET :offset")
	List<UploadFile> getUploadFilesPaginated(Integer limit, Integer offset);
	@Query(value =
			" FROM UploadFile uf "
					+"WHERE uf.creator = :username "
					+"ORDER BY created desc LIMIT :limit OFFSET :offset ")
	List<UploadFile> getUploadFilesPaginatedForUser(Integer limit, Integer offset, String username);

	UploadFile findByHashAndExtension(String hash, String extension);
}
