package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.ReviewFieldDefinitionDto;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IReviewFieldDefinitionService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/_private/admin")
public class ReviewFieldDefinitionAdminController {

	@Autowired
	IReviewFieldDefinitionService fieldDefinitionService;

	@Autowired
	ICategoryService categoryService;

	@GetMapping("/review-field-overview.html")
	public String getOverview(Model model, @RequestParam("categoryId") Long categoryId, HttpServletResponse res) {
		ICategoryDto category = categoryService.getCategoryDtoById(categoryId);
		if (category == null) {
			res.setStatus(404);
			return null;
		}
		List<ReviewFieldDefinitionDto> fields = fieldDefinitionService.getByCategory(categoryId);
		model.addAttribute("category", category);
		model.addAttribute("fields", fields);
		return "admin/review-field-overview.html";
	}

	@GetMapping("/review-field-create.html")
	public String getCreate(Model model, @RequestParam("categoryId") Long categoryId, HttpServletResponse res) {
		ICategoryDto category = categoryService.getCategoryDtoById(categoryId);
		if (category == null) {
			res.setStatus(404);
			return null;
		}
		ReviewFieldDefinitionDto field = new ReviewFieldDefinitionDto();
		field.setCategoryId(categoryId);
		model.addAttribute("category", category);
		model.addAttribute("field", field);
		model.addAttribute("fieldTypes", ReviewFieldTypeEnum.values());
		model.addAttribute("action", "/_private/admin/review-field-create.html");
		return "admin/review-field-edit.html";
	}

	@PostMapping("/review-field-create.html")
	public void postCreate(@ModelAttribute ReviewFieldDefinitionDto field, HttpServletResponse res) throws IOException {
		fieldDefinitionService.save(field);
		res.sendRedirect("/_private/admin/review-field-overview.html?categoryId=" + field.getCategoryId());
	}

	@GetMapping("/review-field-edit.html")
	public String getEdit(Model model, @PathParam("id") Long id, HttpServletResponse res) {
		ReviewFieldDefinitionDto field = fieldDefinitionService.getById(id);
		if (field == null) {
			res.setStatus(404);
			return null;
		}
		ICategoryDto category = categoryService.getCategoryDtoById(field.getCategoryId());
		model.addAttribute("category", category);
		model.addAttribute("field", field);
		model.addAttribute("fieldTypes", ReviewFieldTypeEnum.values());
		model.addAttribute("action", "/_private/admin/review-field-edit.html");
		return "admin/review-field-edit.html";
	}

	@PostMapping("/review-field-edit.html")
	public void postEdit(@ModelAttribute ReviewFieldDefinitionDto field, HttpServletResponse res) throws IOException {
		fieldDefinitionService.save(field);
		res.sendRedirect("/_private/admin/review-field-overview.html?categoryId=" + field.getCategoryId());
	}

	@GetMapping("/review-field-delete.html")
	public String getDelete(Model model, @PathParam("id") Long id, HttpServletResponse res) {
		ReviewFieldDefinitionDto field = fieldDefinitionService.getById(id);
		if (field == null) {
			res.setStatus(404);
			return null;
		}
		ICategoryDto category = categoryService.getCategoryDtoById(field.getCategoryId());
		model.addAttribute("field", field);
		model.addAttribute("category", category);
		return "admin/review-field-delete.html";
	}

	@PostMapping("/review-field-delete.html")
	public void postDelete(@PathParam("id") Long id,
						   @RequestParam("categoryId") Long categoryId,
						   @RequestParam(value = "del", required = false, defaultValue = "false") boolean delete,
						   HttpServletResponse res) throws IOException {
		if (delete) {
			fieldDefinitionService.delete(id);
		}
		res.sendRedirect("/_private/admin/review-field-overview.html?categoryId=" + categoryId);
	}
}
