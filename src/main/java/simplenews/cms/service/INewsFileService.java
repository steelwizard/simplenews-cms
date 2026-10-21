package simplenews.cms.service;

import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.dto.DownloadFileDto;
import simplenews.cms.data.dto.UploadFileDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface INewsFileService {

	List<DownloadFileDto> getByNewsId(Long newsId);

	Map<Long, List<DownloadFileDto>> getByNewsIds(Collection<Long> newsIds);

	void addFiles(Long newsId, MultipartFile[] files, String user);

	void removeFiles(Long newsId, Collection<Long> fileIds);

	void deleteByNews(Long newsId);

	UploadFileDto getDownload(Long fileId);
}
