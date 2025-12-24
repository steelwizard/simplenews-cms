package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.CategoryDto;
import simplenews.cms.service.ICategoryService;

import java.util.List;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class CategoryAdminController {

	@Autowired
	ICategoryService categoryService;

	@GetMapping("/category-overview.html")
	public String getCategoryEdit(Model model) {
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(false);
		model.addAttribute("categories", categoryDtos);
		return "admin/category-overview.html";
	}

	@GetMapping("/category-edit.html")
	public String getCategoryEdit(Model model, @PathParam("id") Long id) {
		ICategoryDto categoryDto = categoryService.getCategoryDtoById(id);
		List<ICategoryDto> categoryDtos = categoryService.getAllCategoryDtos(false);
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("category", categoryDto);
		return "admin/category-edit.html";
	}

	@PostMapping("/category-edit.html")
	public RedirectView postSaveCategory(Model model,
								   @ModelAttribute CategoryDto category,
								   HttpServletRequest httpServletRequest,
								   @PathParam("id") Long id) {

		String user = httpServletRequest.getUserPrincipal().getName();
		category.setId(id);
		ICategoryDto categoryDto = categoryService.saveCategory(category, user);
		List<ICategoryDto> categoryDtos = categoryService.getAllCategoryDtos(false);
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("category", categoryDto);
		RedirectView redirect = new RedirectView("/_private/admin/category-overview.html");
		return redirect;
	}

	@GetMapping("/category-create.html")
	public String getCategoryCreate(Model model) {
		CategoryDto categoryDto = new CategoryDto();
		List<ICategoryDto> categoryDtos = categoryService.getAllCategoryDtos(false);
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("category", categoryDto);
		return "admin/category-edit.html";
	}

	@GetMapping("/category-delete.html")
	public String getCategoryDelete(Model model, @PathParam("id") Long id) {
		ICategoryDto categoryDto = categoryService.getCategoryDtoById(id);
		model.addAttribute("category", categoryDto);
		return "admin/category-delete.html";
	}

	@PostMapping("/category-delete.html")
	public RedirectView postCategoryDelete(Model model, @PathParam("id") Long id) {
		categoryService.deleteCategory(id);
		RedirectView redirect = new RedirectView("/_private/admin/category-overview.html");
		return redirect;
	}
}
