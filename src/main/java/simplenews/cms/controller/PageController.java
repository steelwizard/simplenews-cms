package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.PageDto;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IPageService;
import simplenews.cms.service.IPermissionService;
import simplenews.cms.service.ISiteSettingService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/pages")
public class PageController {

	@Autowired
	IPageService pageService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	ISiteSettingService siteSettingService;
	@Autowired
	IPermissionService permissionService;

	@GetMapping("{key:[a-z0-9\\-]+}.html")
	public String render(@PathVariable String key, Model model,
						 HttpServletRequest request, HttpServletResponse response) throws IOException {
		PageDto page = pageService.getByKey(key);
		if (page == null || (!Boolean.TRUE.equals(page.getVisible()) && !canPreviewDrafts())) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}

		List<ICategoryDto> categories = categoryService.getRootCategories(true);
		String keywords = blankToSite(page.getMetaKeywords(), siteSettingService.getSiteMetaKeywords());
		String description = blankToSite(page.getMetaDescription(), siteSettingService.getSiteMetaDescription());
		String host = request.getHeader("host");
		String canonical = (host == null ? "" : host) + "/pages/" + page.getKey() + ".html";

		model.addAttribute("page", page);
		model.addAttribute("categories", categories);
		model.addAttribute("title", page.getTitle());
		model.addAttribute("siteTitle", siteSettingService.getSiteTitle());
		model.addAttribute("metaKeywords", keywords);
		model.addAttribute("metaDescription", description);
		model.addAttribute("canonicalLink", canonical);
		return "pages/page.html";
	}

	@GetMapping("index.json")
	public ResponseEntity<List<PageDto>> jsonIndex() {
		return new ResponseEntity<>(pageService.listVisible(), HttpStatus.OK);
	}

	@GetMapping("{key:[a-z0-9\\-]+}.json")
	public ResponseEntity<PageDto> jsonOne(@PathVariable String key, HttpServletResponse response) throws IOException {
		PageDto page = pageService.getByKey(key);
		if (page == null || !Boolean.TRUE.equals(page.getVisible())) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	@GetMapping({"{key:[a-z0-9\\-]+}", "{key:[a-z0-9\\-]+}/"})
	public RedirectView redirectBare(@PathVariable String key) {
		RedirectView redirect = new RedirectView("/pages/" + key + ".html");
		redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		return redirect;
	}

	private boolean canPreviewDrafts() {
		return permissionService.hasPermission(PermissionEnum.EDIT_ALL);
	}

	private static String blankToSite(String pageValue, String siteValue) {
		if (pageValue == null || pageValue.isBlank()) {
			return siteValue;
		}
		return pageValue;
	}
}
