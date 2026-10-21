package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import simplenews.cms.data.dto.PageDto;
import simplenews.cms.service.IPageService;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;
import simplenews.cms.service.util.SanitizeUtil;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = {"/_private/admin", "/_private/admin/"})
public class PageAdminController {

	public static final Logger LOG = Logger.getLogger(PageAdminController.class.getName());
	public static final int PAGE_RESULT_LIMIT = 50;

	@Autowired
	IPageService pageService;

	@GetMapping("/page-overview.html")
	public String overview(Model model,
						   @RequestParam(name = "p", required = false) Integer p,
						   HttpServletResponse response) throws IOException {
		int pageNumber = p != null ? p : 0;
		List<PageDto> pages;
		try {
			pages = pageService.list(PAGE_RESULT_LIMIT + 1, PAGE_RESULT_LIMIT * pageNumber);
		} catch (InsufficientPermissionException e) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		boolean hasNextPage = false;
		if (pages.size() > PAGE_RESULT_LIMIT) {
			hasNextPage = true;
			pages = pages.subList(0, pages.size() - 1);
		}
		model.addAttribute("pages", pages);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("pageNumber", pageNumber);
		model.addAttribute("title", "Pages");
		return "admin/page-overview.html";
	}

	@GetMapping("/page-create.html")
	public String createForm(Model model) {
		PageDto page = new PageDto();
		page.setVisible(true);
		showEditor(model, page, "/_private/admin/page-create.html", null, false);
		return "admin/page-edit.html";
	}

	@PostMapping("/page-create.html")
	public String create(@RequestParam(name = "action", defaultValue = "0") int action,
						 @ModelAttribute PageDto page,
						 HttpServletRequest request,
						 Model model) {
		if (action == 1) {
			showEditor(model, page, "/_private/admin/page-create.html", previewKey(page), false);
			return "admin/page-edit.html";
		}
		if (action != 0) {
			throw new IllegalArgumentException();
		}
		String user = request.getUserPrincipal().getName();
		try {
			PageDto created = pageService.create(page, user);
			return "redirect:/_private/admin/page-edit.html?id=" + created.getId() + "&saved=true";
		} catch (DuplicateKeyException e) {
			showEditor(model, page, "/_private/admin/page-create.html", "A page with that address already exists.", false);
		} catch (InsufficientPermissionException e) {
			showEditor(model, page, "/_private/admin/page-create.html", "You do not have permission to create pages.", false);
		} catch (IllegalArgumentException e) {
			LOG.severe(e.toString());
			showEditor(model, page, "/_private/admin/page-create.html", e.getMessage(), false);
		}
		return "admin/page-edit.html";
	}

	@GetMapping("/page-edit.html")
	public String editForm(Model model,
						   @RequestParam("id") Long id,
						   @RequestParam(name = "saved", required = false) Boolean saved,
						   HttpServletResponse response) throws IOException {
		PageDto page = pageService.getById(id);
		if (page == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		showEditor(model, page, "/_private/admin/page-edit.html", null, Boolean.TRUE.equals(saved));
		return "admin/page-edit.html";
	}

	@PostMapping("/page-edit.html")
	public String edit(@RequestParam(name = "action", defaultValue = "0") int action,
					   @ModelAttribute PageDto page,
					   HttpServletRequest request,
					   HttpServletResponse response,
					   Model model) throws IOException {
		String actionPath = "/_private/admin/page-edit.html";
		if (action == 1) {
			showEditor(model, page, actionPath, previewKey(page), false);
			return "admin/page-edit.html";
		}
		if (action == 2) {
			if (page.getId() == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return null;
			}
			return "redirect:/_private/admin/page-delete.html?id=" + page.getId();
		}
		if (action != 0) {
			throw new IllegalArgumentException();
		}
		if (page.getId() == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		String user = request.getUserPrincipal().getName();
		try {
			PageDto updated = pageService.update(page.getId(), page, user);
			return "redirect:/_private/admin/page-edit.html?id=" + updated.getId() + "&saved=true";
		} catch (DuplicateKeyException e) {
			showEditor(model, page, actionPath, "A page with that address already exists.", false);
		} catch (InsufficientPermissionException e) {
			showEditor(model, page, actionPath, "You do not have permission to edit pages.", false);
		} catch (IllegalArgumentException e) {
			LOG.severe(e.toString());
			showEditor(model, page, actionPath, e.getMessage(), false);
		}
		return "admin/page-edit.html";
	}

	@GetMapping("/page-delete.html")
	public String deleteForm(Model model, @RequestParam("id") Long id, HttpServletResponse response) throws IOException {
		PageDto page = pageService.getById(id);
		if (page == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		model.addAttribute("page", page);
		model.addAttribute("title", "Delete page");
		return "admin/page-delete.html";
	}

	@PostMapping("/page-delete.html")
	public String delete(@RequestParam("id") Long id,
						 @RequestParam(value = "del", required = false, defaultValue = "false") boolean delete,
						 HttpServletRequest request,
						 HttpServletResponse response) throws IOException {
		LOG.info("page-delete called. Page ID=" + id + " deleted by " + request.getRemoteUser());
		if (delete) {
			try {
				pageService.delete(id);
			} catch (InsufficientPermissionException e) {
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return null;
			} catch (IllegalArgumentException e) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return null;
			}
		}
		return "redirect:/_private/admin/page-overview.html";
	}

	private String previewKey(PageDto page) {
		try {
			pageService.prepareKey(page);
			return null;
		} catch (IllegalArgumentException e) {
			return e.getMessage();
		}
	}

	private void showEditor(Model model, PageDto page, String action, String error, boolean saved) {
		String raw = page.getContent() == null ? "" : page.getContent();
		String title = (page.getTitle() == null || page.getTitle().isBlank()) ? "Page" : page.getTitle();
		model.addAttribute("page", page);
		model.addAttribute("action", action);
		model.addAttribute("previewContent", SanitizeUtil.sanitize(raw));
		model.addAttribute("error", error);
		model.addAttribute("saved", saved);
		model.addAttribute("title", title);
	}
}
