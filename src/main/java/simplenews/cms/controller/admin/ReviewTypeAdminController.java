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
import simplenews.cms.data.dto.ReviewFieldDto;
import simplenews.cms.data.dto.ReviewTypeDto;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.service.IReviewService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = {"/_private/admin", "/_private/admin/"})
public class ReviewTypeAdminController {

	@Autowired
	IReviewService reviewService;

	@GetMapping("/review-type-overview.html")
	public String overview(Model model, @RequestParam(name = "error", required = false) String error) {
		Map<ReviewType, List<String>> types = new LinkedHashMap<>();
		reviewService.getReviewTypes().forEach(t -> types.put(t, reviewService.getCategoriesUsing(t.getId())));
		model.addAttribute("types", types);
		model.addAttribute("error", error);
		model.addAttribute("title", "Review types");
		return "admin/review-type-overview.html";
	}

	@GetMapping("/review-type-create.html")
	public String createForm(Model model) {
		showTypeEditor(model, new ReviewTypeDto(), null);
		return "admin/review-type-edit.html";
	}

	@GetMapping("/review-type-edit.html")
	public String editForm(Model model, @RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		ReviewType type = reviewService.getReviewType(id);
		if (type == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		showTypeEditor(model, new ReviewTypeDto(type), null);
		return "admin/review-type-edit.html";
	}

	@PostMapping("/review-type-edit.html")
	public String saveType(Model model, @ModelAttribute("reviewType") ReviewTypeDto dto, HttpServletRequest req) {
		try {
			ReviewType saved = reviewService.saveReviewType(dto, req.getUserPrincipal().getName());
			return "redirect:/_private/admin/review-type-edit.html?id=" + saved.getId();
		} catch (IllegalArgumentException e) {
			showTypeEditor(model, dto, e.getMessage());
			return "admin/review-type-edit.html";
		}
	}

	@PostMapping("/review-type-delete.html")
	public String deleteType(@RequestParam("id") Long id) {
		try {
			reviewService.deleteReviewType(id);
		} catch (IllegalArgumentException | IllegalStateException e) {
			return "redirect:/_private/admin/review-type-overview.html?error="
					+ URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
		}
		return "redirect:/_private/admin/review-type-overview.html";
	}

	@GetMapping("/review-field-create.html")
	public String createFieldForm(Model model, @RequestParam("type") Long typeId, HttpServletResponse res) throws IOException {
		ReviewType type = reviewService.getReviewType(typeId);
		if (type == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		ReviewFieldDto field = new ReviewFieldDto();
		field.setReviewType(typeId);
		showFieldEditor(model, field, type, null);
		return "admin/review-field-edit.html";
	}

	@GetMapping("/review-field-edit.html")
	public String editFieldForm(Model model, @RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		ReviewField field = reviewService.getField(id);
		if (field == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		showFieldEditor(model, new ReviewFieldDto(field), reviewService.getReviewType(field.getReviewType()), null);
		return "admin/review-field-edit.html";
	}

	@PostMapping("/review-field-edit.html")
	public String saveField(Model model, @ModelAttribute("field") ReviewFieldDto dto) {
		try {
			ReviewField saved = reviewService.saveField(dto);
			return "redirect:/_private/admin/review-type-edit.html?id=" + saved.getReviewType();
		} catch (IllegalArgumentException e) {
			showFieldEditor(model, dto, reviewService.getReviewType(dto.getReviewType()), e.getMessage());
			return "admin/review-field-edit.html";
		}
	}

	@PostMapping("/review-field-move.html")
	public String moveField(@RequestParam("id") Long id, @RequestParam("dir") String dir) {
		ReviewField field = reviewService.getField(id);
		if (field == null) return "redirect:/_private/admin/review-type-overview.html";
		reviewService.moveField(id, "up".equals(dir));
		return "redirect:/_private/admin/review-type-edit.html?id=" + field.getReviewType();
	}

	@PostMapping("/review-field-delete.html")
	public String deleteField(@RequestParam("id") Long id) {
		ReviewField field = reviewService.getField(id);
		if (field == null) return "redirect:/_private/admin/review-type-overview.html";
		reviewService.deleteField(id);
		return "redirect:/_private/admin/review-type-edit.html?id=" + field.getReviewType();
	}

	private void showTypeEditor(Model model, ReviewTypeDto dto, String error) {
		model.addAttribute("reviewType", dto);
		model.addAttribute("fields", dto.getId() == null ? List.of() : reviewService.getFields(dto.getId(), false));
		model.addAttribute("categoriesUsing", dto.getId() == null ? List.of() : reviewService.getCategoriesUsing(dto.getId()));
		model.addAttribute("error", error);
		model.addAttribute("title", dto.getId() == null ? "New review type" : "Review type: " + dto.getTitle());
	}

	private void showFieldEditor(Model model, ReviewFieldDto dto, ReviewType type, String error) {
		model.addAttribute("field", dto);
		model.addAttribute("reviewType", type);
		model.addAttribute("fieldTypes", ReviewFieldTypeEnum.values());
		model.addAttribute("hasValues", dto.getId() != null && reviewService.fieldHasValues(dto.getId()));
		model.addAttribute("error", error);
		model.addAttribute("title", dto.getId() == null ? "New field" : "Field: " + dto.getLabel());
	}
}
