package simplenews.cms.service;

import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.entity.UploadFile;

import java.io.File;
public interface IUploadFileService {
	UploadFile upload(MultipartFile upload, String user);
	File getFile(String filename);
}
