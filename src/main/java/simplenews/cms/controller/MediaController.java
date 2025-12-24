package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import simplenews.cms.service.IUploadFileService;
import simplenews.cms.service.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Controller
@RequestMapping(path = "/media/")
public class MediaController {

	private static final Logger LOG = Logger.getLogger(MediaController.class.getName());

	@Autowired
	IUploadFileService uploadFileService;

	@GetMapping("/{year}/{month}/{hash}/{filename}")
	public @ResponseBody ResponseEntity<?> getMediaFile(@PathVariable("year") String year,
														@PathVariable("month") String month,
														@PathVariable("hash") String hash,
														@PathVariable("filename") String filename,
														HttpServletResponse response) {

		File file = null;
		Path path = Paths.get(year, month, hash, filename);
		file = uploadFileService.getFile(path.toString());
		if (file == null || !file.exists() || !file.isFile()) {
			try {
				response.sendError(404);
				return null;
			} catch (IOException ioe) {
				LOG.severe(ioe.toString());
				return null;
			}
		}
		String contentType = FileUtil.getMediaType(FileUtil.getFileExtension(file.getName())).toString();
		response.setContentType(contentType);

		try(InputStream i = new FileInputStream(uploadFileService.getFile(path.toString()))) {
			IOUtils.copy(i, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ResponseEntity.ok().build();
	}

}
