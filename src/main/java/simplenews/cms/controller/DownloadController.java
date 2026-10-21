package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import simplenews.cms.data.dto.UploadFileDto;
import simplenews.cms.service.INewsFileService;
import simplenews.cms.service.IUploadFileService;
import simplenews.cms.service.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class DownloadController {

	@Autowired
	INewsFileService newsFileService;
	@Autowired
	IUploadFileService uploadFileService;

	@GetMapping("/download/{id}")
	public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
		UploadFileDto file = newsFileService.getDownload(id);
		File disk = file == null ? null : uploadFileService.getDiskFile(file.getId());
		if (disk == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String filename = file.getFilename() == null ? "download" : file.getFilename().replace("\"", "'");
		response.setContentType(FileUtil.getMediaType(file.getExtension()).toString());
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
		response.setContentLengthLong(disk.length());
		try (InputStream in = new FileInputStream(disk)) {
			IOUtils.copy(in, response.getOutputStream());
			response.flushBuffer();
		}
	}
}
