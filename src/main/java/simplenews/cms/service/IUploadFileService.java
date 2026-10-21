package simplenews.cms.service;

import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.dto.UploadFileDto;
import simplenews.cms.data.entity.UploadFile;

import java.io.File;
import java.util.List;

public interface IUploadFileService {
	UploadFileDto getById(Long id);

	UploadFile upload(MultipartFile upload, String user);
	File getFile(String filename);
	File getDiskFile(Long id);
	List<UploadFileDto> getUploadFiles(int limit, int offset);
	boolean deleteById(Long id);
}
