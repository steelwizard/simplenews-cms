package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INewsFileService;
import simplenews.cms.service.INewsService;
import simplenews.cms.service.IPermissionService;
import simplenews.cms.service.IReviewService;
import simplenews.cms.service.IUserCategoryService;
import simplenews.cms.service.IUserService;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;
import simplenews.cms.service.util.SanitizeUtil;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class AdminController {

	public static final Logger LOG = Logger.getLogger(AdminController.class.getName());
	public static final int ADMIN_RESULT_LIMIT = 50;

	private static final String CREATE_ACTION = "/_private/admin/post-create.html";
	private static final String EDIT_ACTION = "/_private/admin/post-edit.html";
	private static final int ACTION_SAVE = 0;
	private static final int ACTION_PREVIEW = 1;
	private static final int ACTION_DELETE = 2;

	@Autowired
	INewsService newsService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	IUserCategoryService userCategoryService;
	@Autowired
	IUserService userService;
	@Autowired
	IReviewService reviewService;
	@Autowired
	INewsFileService newsFileService;

	@GetMapping
	public String getAdmin(){
		return "admin/admin.html";
	}
	@GetMapping(path = "/post-overview.html")
	public String getArticleOverview(Model model, @RequestParam(name = "p", required = false) Integer p,
									 HttpServletRequest request){
		int page = p != null ? p : 0;
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		LOG.info(username+" requesting post-overview.html");
		List<INewsDto> newslist = new ArrayList<>();
		if(permissionService.hasPermission(PermissionEnum.SYSTEM)) {
			newslist = newsService.getAllNewsAdmin(ADMIN_RESULT_LIMIT+1, ADMIN_RESULT_LIMIT*page);
		} else if(permissionService.hasPermission(PermissionEnum.EDIT_ALL)) {
			List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(username);
			newslist = newsService.getNewsByCategoryIds(categoryIds, ADMIN_RESULT_LIMIT+1, ADMIN_RESULT_LIMIT*page);
		}
		else if (permissionService.hasPermission(PermissionEnum.EDIT_OWN)) {
			List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(username);
			newslist = newsService.getNewsByCreatorAndCategories(username, categoryIds, ADMIN_RESULT_LIMIT+1, ADMIN_RESULT_LIMIT*page);
		}
		boolean hasNextPage = false;
		if (newslist.size() > ADMIN_RESULT_LIMIT) {
			hasNextPage = true;
			newslist = newslist.subList(0, newslist.size() - 1);
		}
		model.addAttribute("newslist", newslist);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "admin/post-overview.html";
	}

	@GetMapping(path = "/post-create.html")
	public String getArticleCreate(Model model) {
		NewsDto post = new NewsDto();
		post.setVisible(true);
		return renderEditor(model, CREATE_ACTION, post, Map.of(), List.of(), false);
	}

	@PostMapping(path = "/post-create.html")
	public String postArticleCreate(Model model,
									@RequestParam(value = "action", defaultValue = "0") int action,
									@ModelAttribute NewsDto post,
									@RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
									HttpServletRequest httpServletRequest) {
		String user = httpServletRequest.getUserPrincipal().getName();
		Map<Long, String> reviewValues = reviewService.readValues(httpServletRequest.getParameterMap());

		if (action == ACTION_DELETE) {
			return "redirect:/_private/admin/post-overview.html";
		}
		if (action == ACTION_PREVIEW) {
			post.setCreator(user);
			return renderEditor(model, CREATE_ACTION, post, reviewValues, List.of(), true);
		}

		Long reviewTypeId = reviewService.getReviewTypeIdForCategory(post.getCategory());
		List<String> errors = new ArrayList<>(reviewService.validateValues(reviewTypeId, reviewValues));
		if (errors.isEmpty()) {
			try {
				INewsDto created = newsService.createNews(post, user);
				reviewService.saveValues(created.getId(), reviewTypeId, reviewValues);
				saveAttachments(created.getId(), attachments, List.of(), user, errors);
				if (!errors.isEmpty()) {
					return renderEditor(model, EDIT_ACTION, newsService.getNewsById(created.getId()),
							reviewValues, errors, false);
				}
				return "redirect:/_private/admin/post-edit.html?id=" + created.getId();
			} catch (InsufficientPermissionException e) {
				LOG.warning(e.getMessage());
				errors.add("You are not allowed to post in this category.");
			} catch (DuplicateKeyException e) {
				errors.add("The key is already used by another post.");
			} catch (IllegalArgumentException e) {
				LOG.severe(e.toString());
				errors.add(e.getMessage());
			}
		}
		return renderEditor(model, CREATE_ACTION, post, reviewValues, errors, false);
	}

	@GetMapping(path = "/post-edit.html")
	public String getArticleEdit(Model model, @RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		INewsDto post = newsService.getNewsById(id);
		if (post == null) {
			res.sendError(404);
			return null;
		}
		return renderEditor(model, EDIT_ACTION, post, reviewService.getRawValues(id), List.of(), false);
	}

	@PostMapping(path = "/post-edit.html")
	public String postArticleEdit(Model model,
								  @RequestParam(value = "action", defaultValue = "0") int action,
								  @ModelAttribute NewsDto post,
								  @RequestParam(value = "attachments", required = false) MultipartFile[] attachments,
								  @RequestParam(value = "removeFile", required = false) List<Long> removeFile,
								  HttpServletRequest httpServletRequest,
								  HttpServletResponse httpServletResponse) throws IOException {

		INewsDto stored = post.getId() != null ? newsService.getNewsById(post.getId()) : null;
		if (stored == null) {
			httpServletResponse.sendError(404);
			return null;
		}
		if (action == ACTION_DELETE) {
			return "redirect:/_private/admin/post-delete.html?id=" + stored.getId();
		}

		post.setCreator(stored.getCreator());
		post.setCreated(stored.getCreated());
		Map<Long, String> reviewValues = reviewService.readValues(httpServletRequest.getParameterMap());
		if (action == ACTION_PREVIEW) {
			return renderEditor(model, EDIT_ACTION, post, reviewValues, List.of(), true);
		}

		String user = httpServletRequest.getUserPrincipal().getName();
		Long reviewTypeId = reviewService.getReviewTypeIdForCategory(post.getCategory());
		List<String> errors = new ArrayList<>(reviewService.validateValues(reviewTypeId, reviewValues));
		INewsDto edited = post;
		if (errors.isEmpty()) {
			try {
				edited = newsService.updateNews(stored.getId(), post, user);
				reviewService.saveValues(edited.getId(), reviewTypeId, reviewValues);
				saveAttachments(edited.getId(), attachments, removeFile, user, errors);
				if (errors.isEmpty()) {
					edited = newsService.getNewsById(edited.getId());
				}
			} catch (InsufficientPermissionException e) {
				LOG.warning(e.getMessage());
				errors.add("You are not allowed to edit this post or move it to this category.");
			} catch (DuplicateKeyException e) {
				errors.add("The key is already used by another post.");
			} catch (IllegalArgumentException e) {
				LOG.severe(e.toString());
				errors.add(e.getMessage());
			}
		}
		return renderEditor(model, EDIT_ACTION, edited, reviewValues, errors, false);
	}

	private String renderEditor(Model model, String action, INewsDto post, Map<Long, String> values,
								List<String> errors, boolean preview) {
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userService.getCurrentUserName());
		List<ICategoryDto> categories = categoryService.findMany(categoryIds);

		model.addAttribute("post", post);
		model.addAttribute("action", action);
		model.addAttribute("categories", categories);
		model.addAttribute("preview", preview);
		model.addAttribute("previewContent", post.getContent() != null ? SanitizeUtil.sanitize(post.getContent()) : "");
		model.addAttribute("attachments", post.getId() == null ? List.of() : newsFileService.getByNewsId(post.getId()));
		addReviewEditor(model, values, errors);
		return "admin/post-edit.html";
	}

	private void saveAttachments(Long newsId, MultipartFile[] attachments,
								 List<Long> removeFile, String user, List<String> errors) {
		try {
			newsFileService.removeFiles(newsId, removeFile);
			newsFileService.addFiles(newsId, attachments, user);
		} catch (RuntimeException e) {
			LOG.warning(e.toString());
			errors.add(e.getMessage() != null ? e.getMessage() : "Could not store the file.");
		}
	}

	private void addReviewEditor(Model model, Map<Long, String> values, List<String> errors) {
		model.addAttribute("reviewTypes", reviewService.getReviewTypes());
		model.addAttribute("reviewFields", reviewService.getActiveFieldsByType());
		model.addAttribute("fieldValues", values);
		model.addAttribute("reviewErrors", errors);
	}

	@GetMapping(path = "/post-delete.html")
	public String postDelete(Model model, @RequestParam("id") Long id, HttpServletResponse res) throws IOException {
		INewsDto post = newsService.getNewsById(id);
		if (post == null) {
			res.sendError(404);
			return null;
		}
		model.addAttribute("post", post);
		model.addAttribute("previewContent", post.getContent() != null ? SanitizeUtil.sanitize(post.getContent()) : "");
		return "admin/post-delete.html";
	}

	@PostMapping(path = "/post-delete.html")
	public String postDeleteConfirm(@RequestParam("id") Long id,
									@RequestParam(value="del", required = false, defaultValue="false") boolean delete,
									HttpServletRequest req,
									HttpServletResponse res) throws IOException {
		LOG.info("post-delete called. Post ID="+id+" by "+req.getRemoteUser()+" from "+req.getRemoteAddr());
		if(delete) {
			try {
				newsService.deleteNews(id);
			} catch (InsufficientPermissionException e) {
				LOG.warning(e.getMessage());
				res.sendError(403);
				return null;
			} catch (IllegalArgumentException e) {
				res.sendError(404);
				return null;
			}
		}
		return "redirect:/_private/admin/post-overview.html";
	}

}
