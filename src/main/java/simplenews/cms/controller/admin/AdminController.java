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
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INewsService;
import simplenews.cms.service.IPermissionService;
import simplenews.cms.service.IUserCategoryService;
import simplenews.cms.service.IUserService;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class AdminController {

	public static final Logger LOG = Logger.getLogger(AdminController.class.getName());
	public static final int ADMIN_RESULT_LIMIT = 50;

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
	public String getArticleCreate(Model model, HttpServletRequest httpServletRequest) {
		String user = httpServletRequest.getUserPrincipal().getName();
		NewsDto post = new NewsDto();

		UserDto userDto = userService.getByUsername(user);
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userDto.getUsername());
		List<ICategoryDto> categories = new ArrayList<>();
		categories = categoryService.findMany(categoryIds);

		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/post-create.html");
		model.addAttribute("categories", categories);

		return "admin/post-edit.html";
	}

	@PostMapping(path = "/post-create.html")
	public String postArticleCreate(Model model, @ModelAttribute NewsDto post, HttpServletRequest httpServletRequest) {


		String user = httpServletRequest.getUserPrincipal().getName();
		INewsDto created = new NewsDto();
		try {
			created = newsService.createNews(post, user);
		} catch (InsufficientPermissionException e) {
			e.printStackTrace();
		} catch (DuplicateKeyException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			LOG.info(e.toString());
		}

		UserDto userDto = userService.getByUsername(user);
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userDto.getUsername());
		List<ICategoryDto> categories = new ArrayList<>();
		categories = categoryService.findMany(categoryIds);

		model.addAttribute("action", "/_private/admin/post-create.html");
		model.addAttribute("post", created);
		model.addAttribute("categories", categories);

		return "admin/post-edit.html";
	}

	@GetMapping(path = "/post-edit.html")
	public String getArticleEdit(Model model, @PathParam("id") Long id) {
		INewsDto post = newsService.getNewsById(id);

		UserDto userDto = userService.getByUsername(userService.getCurrentUserName());
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userDto.getUsername());
		List<ICategoryDto> categories = new ArrayList<>();
		categories = categoryService.findMany(categoryIds);

		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/post-edit.html");
		model.addAttribute("categories", categories);

		return "admin/post-edit.html";
	}

	@PostMapping(path = "/post-edit.html")
	public String postArticleEdit(Model model,
												   @PathParam("action") Integer action,
												   NewsDto post,
												   HttpServletRequest httpServletRequest,
								  				   HttpServletResponse httpServletResponse) throws IOException {

		UserDto userDto = userService.getByUsername(userService.getCurrentUserName());
		List<Long> categoryIds = userCategoryService.getCategoryIdsByUser(userDto.getUsername());
		List<ICategoryDto> categories = new ArrayList<>();
		categories = categoryService.findMany(categoryIds);
		String user;
		INewsDto edited;
		switch (action) {
			case 0: // Save
				user = httpServletRequest.getUserPrincipal().getName();
				edited = new NewsDto();
				try {
					edited = newsService.updateNews(post.getId(), post, user);
				} catch (InsufficientPermissionException e) {
					e.printStackTrace();
				} catch (DuplicateKeyException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					LOG.info(e.toString());
				}

				categories = categoryService.getAllCategoryDtos(false);
				model.addAttribute("categories", categories);

				model.addAttribute("action", "/_private/admin/post-edit.html");
				model.addAttribute("post", edited);
				return "admin/post-edit.html";
			case 1: // Preview
				categories = categoryService.getAllCategoryDtos(false);
				model.addAttribute("categories", categories);

				model.addAttribute("action", "/_private/admin/post-edit.html");
				model.addAttribute("post", post);
				return "admin/post-edit.html";
			case 2: // Delete
				if(post.getId()==null) httpServletResponse.sendError(404);
				else httpServletResponse.sendRedirect("/_private/admin/post-delete.html?id="+post.getId());
				httpServletResponse.flushBuffer();
				break;
			default:
				throw new IllegalArgumentException();
		}
		return null;
	}
	@GetMapping(path = "/post-delete.html")
	public String postDelete(Model model, @RequestParam("id") Long id,
								  HttpServletRequest httpServletRequest,
								  HttpServletResponse res) {
		INewsDto post = newsService.getNewsById(id);
		if (post==null) {
			res.setStatus(404);
			return null;
		}
		model.addAttribute("post", post);
		model.addAttribute("action", "/_private/admin/post-delete.html");
		return "admin/post-delete.html";
	}

	@PostMapping(path = "/post-delete.html")
	public void postDeleteConfirm(Model model, @PathParam("id") Long id,
								  @RequestParam(value="del", required = false, defaultValue="false") boolean delete,
								  HttpServletRequest req,
								  HttpServletResponse res) throws IOException {
		LOG.info("post-delete called. Post ID="+id+" deleted by "+req.getRemoteUser()+" from "+req.getRemoteAddr());
		if(delete) {
			try {
				newsService.deleteNews(id);
			} catch (InsufficientPermissionException e) {
				res.sendError(403);
			}
		}
		res.sendRedirect("/_private/admin/post-overview.html");
	}

}
