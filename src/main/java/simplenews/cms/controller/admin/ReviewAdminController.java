package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewContentDto;
import simplenews.cms.data.dto.ReviewFieldDefinitionDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.CategoryTypeEnum;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IPermissionService;
import simplenews.cms.service.IReviewFieldDefinitionService;
import simplenews.cms.service.IReviewService;
import simplenews.cms.service.IUserCategoryService;
import simplenews.cms.service.IUserService;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/_private/admin")
public class ReviewAdminController {

	private static final Logger LOG = Logger.getLogger(ReviewAdminController.class.getName());
	private static final int ADMIN_RESULT_LIMIT = 50;

	@Autowired
	IReviewService reviewService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	IUserCategoryService userCategoryService;
	@Autowired
	IUserService userService;
	@Autowired
	IReviewFieldDefinitionService fieldDefinitionService;

	@GetMapping("/review-overview.html")
	public String getOverview(Model model, @RequestParam(name = "p", required = false) Integer p) {
		int page = p != null ? p : 0;
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		List<INewsDto> reviews = new ArrayList<>();
		if (permissionService.hasPermission(PermissionEnum.SYSTEM)) {
			reviews = reviewService.getAllReviewsAdmin(ADMIN_RESULT_LIMIT + 1, ADMIN_RESULT_LIMIT * page);
		} else if (permissionService.hasPermission(PermissionEnum.EDIT_ALL)) {
			List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(username);
			reviews = reviewService.getReviewsByCategoryIds(categoryIds, ADMIN_RESULT_LIMIT + 1, ADMIN_RESULT_LIMIT * page);
		} else if (permissionService.hasPermission(PermissionEnum.EDIT_OWN)) {
			List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(username);
			reviews = reviewService.getReviewsByCreatorAndCategories(username, categoryIds, ADMIN_RESULT_LIMIT + 1, ADMIN_RESULT_LIMIT * page);
		}
		boolean hasNextPage = false;
		if (reviews.size() > ADMIN_RESULT_LIMIT) {
			hasNextPage = true;
			reviews = reviews.subList(0, reviews.size() - 1);
		}
		model.addAttribute("reviewlist", reviews);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "admin/review-overview.html";
	}

	@GetMapping("/review-create.html")
	public String getCreate(Model model, HttpServletRequest req) {
		String user = req.getUserPrincipal().getName();
		ReviewContentDto post = new ReviewContentDto();
		post.setVisible(true);
		List<ICategoryDto> categories = getReviewCategories(user);
		List<ReviewFieldDefinitionDto> fieldDefinitions = new ArrayList<>();
		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/review-create.html");
		model.addAttribute("categories", categories);
		model.addAttribute("fieldDefinitions", fieldDefinitions);
		return "admin/review-edit.html";
	}

	@PostMapping("/review-create.html")
	public String postCreate(Model model, @ModelAttribute ReviewContentDto post, HttpServletRequest req) {
		String user = req.getUserPrincipal().getName();
		INewsDto created = new ReviewContentDto();
		try {
			created = reviewService.createReview(post, user);
		} catch (InsufficientPermissionException | DuplicateKeyException | IllegalArgumentException e) {
			LOG.severe(e.toString());
		}
		List<ICategoryDto> categories = getReviewCategories(user);
		List<ReviewFieldDefinitionDto> fieldDefinitions = getFieldDefinitions(created.getCategory());
		model.addAttribute("post", created);
		model.addAttribute("action", "/_private/admin/review-create.html");
		model.addAttribute("categories", categories);
		model.addAttribute("fieldDefinitions", fieldDefinitions);
		return "admin/review-edit.html";
	}

	@GetMapping("/review-edit.html")
	public String getEdit(Model model, @PathParam("id") Long id) {
		INewsDto post = reviewService.getReviewById(id);
		String user = userService.getCurrentUserName();
		List<ICategoryDto> categories = getReviewCategories(user);
		List<ReviewFieldDefinitionDto> fieldDefinitions = getFieldDefinitions(post != null ? post.getCategory() : null);
		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/review-edit.html");
		model.addAttribute("categories", categories);
		model.addAttribute("fieldDefinitions", fieldDefinitions);
		return "admin/review-edit.html";
	}

	@PostMapping("/review-edit.html")
	public String postEdit(Model model, @PathParam("action") Integer action,
						   @ModelAttribute ReviewContentDto post,
						   HttpServletRequest req, HttpServletResponse res) throws IOException {
		String user = userService.getCurrentUserName();
		List<ICategoryDto> categories = getReviewCategories(user);

		switch (action) {
			case 0: // Save
				INewsDto edited = new ReviewContentDto();
				try {
					edited = reviewService.updateReview(post.getId(), post, req.getUserPrincipal().getName());
				} catch (InsufficientPermissionException | DuplicateKeyException | IllegalArgumentException e) {
					LOG.severe(e.toString());
				}
				model.addAttribute("post", edited);
				model.addAttribute("action", "/_private/admin/review-edit.html");
				model.addAttribute("categories", categories);
				model.addAttribute("fieldDefinitions", getFieldDefinitions(edited.getCategory()));
				return "admin/review-edit.html";
			case 1: // Preview
				model.addAttribute("post", post);
				model.addAttribute("action", "/_private/admin/review-edit.html");
				model.addAttribute("categories", categories);
				model.addAttribute("fieldDefinitions", getFieldDefinitions(post.getCategory()));
				return "admin/review-edit.html";
			case 2: // Delete
				if (post.getId() == null) res.sendError(404);
				else res.sendRedirect("/_private/admin/review-delete.html?id=" + post.getId());
				res.flushBuffer();
				return null;
			default:
				throw new IllegalArgumentException("Unknown action: " + action);
		}
	}

	@GetMapping("/review-delete.html")
	public String getDelete(Model model, @RequestParam("id") Long id, HttpServletResponse res) {
		INewsDto post = reviewService.getReviewById(id);
		if (post == null) {
			res.setStatus(404);
			return null;
		}
		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/review-delete.html");
		return "admin/review-delete.html";
	}

	@PostMapping("/review-delete.html")
	public void postDeleteConfirm(@PathParam("id") Long id,
								  @RequestParam(value = "del", required = false, defaultValue = "false") boolean delete,
								  HttpServletRequest req, HttpServletResponse res) throws IOException {
		LOG.info("review-delete called. Review ID=" + id + " by " + req.getRemoteUser());
		if (delete) {
			try {
				reviewService.deleteReview(id);
			} catch (InsufficientPermissionException e) {
				res.sendError(403);
				return;
			}
		}
		res.sendRedirect("/_private/admin/review-overview.html");
	}

	private List<ICategoryDto> getReviewCategories(String user) {
		UserDto userDto = userService.getByUsername(user);
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userDto.getUsername());
		return categoryService.findMany(categoryIds).stream()
				.filter(c -> c.getType() == CategoryTypeEnum.REVIEW)
				.toList();
	}

	private List<ReviewFieldDefinitionDto> getFieldDefinitions(String categoryName) {
		if (categoryName == null) return new ArrayList<>();
		ICategoryDto cat = categoryService.getCategoryDtoByName(categoryName);
		if (cat == null) return new ArrayList<>();
		return fieldDefinitionService.getByCategory(cat.getId());
	}
}
