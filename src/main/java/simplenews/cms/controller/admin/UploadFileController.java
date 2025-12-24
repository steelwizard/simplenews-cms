package simplenews.cms.controller.admin;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.controller.NewsController;
import simplenews.cms.data.dto.UploadFileDto;
import simplenews.cms.service.UploadFileService;

import java.util.List;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class UploadFileController {

	private static final int RESULT_LIMIT = 10;

	@Autowired
	UploadFileService uploadFileService;

	@GetMapping("/upload-files.html")
	public String getUploadFiles(Model model,  @RequestParam(name = "p", required = false) Integer p) {
		int page = p != null ? p : 0;
		List<UploadFileDto> uploads = uploadFileService.getUploadFiles(RESULT_LIMIT+1, RESULT_LIMIT*page);
		boolean hasNextPage = false;
		if (uploads.size() > RESULT_LIMIT) {
			hasNextPage = true;
			uploads = uploads.subList(0, uploads.size() - 1);
		}
		model.addAttribute("uploadFiles", uploads);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "admin/upload-files.html";
	}

	@PostMapping("upload.html")
	public String postUploadfile(Model model,
								 HttpServletRequest httpServletRequest,
								 @RequestParam("file") MultipartFile file) {
		String user = httpServletRequest.getUserPrincipal().getName();
		uploadFileService.upload(file, user);
		int page = 0;
		List<UploadFileDto> uploads = uploadFileService.getUploadFiles(RESULT_LIMIT+1, 0);
		boolean hasNextPage = false;
		if (uploads.size() > RESULT_LIMIT) {
			hasNextPage = true;
			uploads = uploads.subList(0, uploads.size() - 1);
		}
		model.addAttribute("uploadFiles", uploads);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "admin/upload-files.html";
	}
	@GetMapping("edit-upload.html")
	public String getEditUpload(Model model, @PathParam("id") Long id) {
		return "admin/edit-upload.html";
	}


}
