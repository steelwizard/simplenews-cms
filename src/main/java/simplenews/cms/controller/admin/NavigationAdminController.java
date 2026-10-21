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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import simplenews.cms.data.dto.NavSyncResultDto;
import simplenews.cms.data.dto.NavigationItemDto;
import simplenews.cms.data.entity.NavigationItem;
import simplenews.cms.data.entity.NavigationMenuEnum;
import simplenews.cms.data.entity.NavigationTypeEnum;
import simplenews.cms.data.repository.StaticPageRepository;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INavigationService;
import simplenews.cms.service.IReviewService;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping(value = {"/_private/admin", "/_private/admin/"})
public class NavigationAdminController {

	@Autowired
	INavigationService navigationService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IReviewService reviewService;
	@Autowired
	StaticPageRepository pageRepository;

	@GetMapping("/navigation-overview.html")
	public String overview(Model model) {
		Map<NavigationMenuEnum, Object> menus = new LinkedHashMap<>();
		for (NavigationMenuEnum menu : NavigationMenuEnum.values()) {
			menus.put(menu, navigationService.getAdminRows(menu));
		}
		model.addAttribute("menus", menus);
		model.addAttribute("autoCategoryMenu", navigationService.getAutoCategoryMenu());
		model.addAttribute("autoPageMenu", navigationService.getAutoPageMenu());
		model.addAttribute("title", "Navigation");
		return "admin/navigation-overview.html";
	}

	@PostMapping("/navigation-settings.html")
	public String saveSettings(@RequestParam("autoCategories") String autoCategories,
							   @RequestParam("autoPages") String autoPages) {
		navigationService.setAutoCategoryMenu(parseMenu(autoCategories));
		navigationService.setAutoPageMenu(parseMenu(autoPages));
		return "redirect:/_private/admin/navigation-overview.html";
	}

	private static NavigationMenuEnum parseMenu(String value) {
		for (NavigationMenuEnum m : NavigationMenuEnum.values()) {
			if (m.name().equals(value)) return m;
		}
		return null;
	}

	@PostMapping("/navigation-sync.html")
	public String sync(RedirectAttributes redirectAttributes) {
		NavSyncResultDto result = navigationService.sync();
		redirectAttributes.addFlashAttribute("message", String.format(
				"Sync finished: %d categories and %d pages added, %d entries moved, %d broken entries removed.",
				result.addedCategories(), result.addedPages(), result.moved(), result.removed()));
		return "redirect:/_private/admin/navigation-overview.html";
	}

	@GetMapping("/navigation-create.html")
	public String createForm(Model model, @RequestParam(name = "menu", required = false) NavigationMenuEnum menu) {
		NavigationItemDto item = new NavigationItemDto();
		if (menu != null) item.setMenu(menu);
		showEditor(model, item, null);
		return "admin/navigation-edit.html";
	}

	@GetMapping("/navigation-edit.html")
	public String editForm(Model model, @RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		NavigationItem item = navigationService.getItem(id);
		if (item == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		showEditor(model, new NavigationItemDto(item), null);
		return "admin/navigation-edit.html";
	}

	@PostMapping("/navigation-edit.html")
	public String save(Model model, @ModelAttribute("item") NavigationItemDto item, HttpServletRequest req) {
		try {
			navigationService.save(item, req.getUserPrincipal().getName());
			return "redirect:/_private/admin/navigation-overview.html";
		} catch (IllegalArgumentException e) {
			showEditor(model, item, e.getMessage());
			return "admin/navigation-edit.html";
		}
	}

	@PostMapping("/navigation-move.html")
	public String move(@RequestParam("id") Long id, @RequestParam("dir") String dir) {
		navigationService.move(id, "up".equals(dir));
		return "redirect:/_private/admin/navigation-overview.html";
	}

	@PostMapping("/navigation-delete.html")
	public String delete(@RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		try {
			navigationService.delete(id);
		} catch (IllegalArgumentException e) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return "redirect:/_private/admin/navigation-overview.html";
	}

	private void showEditor(Model model, NavigationItemDto item, String error) {
		model.addAttribute("item", item);
		model.addAttribute("menus", NavigationMenuEnum.values());
		model.addAttribute("types", NavigationTypeEnum.values());
		model.addAttribute("parents", navigationService.getParentCandidates(item.getId()));
		model.addAttribute("pages", pageRepository.findAll());
		model.addAttribute("categoryList", categoryService.getAllCategories(false));
		model.addAttribute("reviewTypes", reviewService.getReviewTypes());
		model.addAttribute("error", error);
		model.addAttribute("title", item.getId() == null ? "New navigation entry" : "Edit navigation entry");
	}
}
