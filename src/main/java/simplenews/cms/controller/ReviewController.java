package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewContentDto;
import simplenews.cms.data.dto.ReviewFieldDefinitionDto;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IReviewFieldDefinitionService;
import simplenews.cms.service.IReviewService;
import simplenews.cms.service.ISiteSettingService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("review")
public class ReviewController {

	public static final int RESULT_LIMIT = 10;

	@Autowired
	private IReviewService reviewService;
	@Autowired
	private ICategoryService categoryService;
	@Autowired
	private IReviewFieldDefinitionService fieldDefinitionService;
	@Autowired
	private ISiteSettingService siteSettingService;

	@GetMapping("/")
	public RedirectView getDefaultPage() {
		RedirectView redirect = new RedirectView("/review/index.html");
		redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		return redirect;
	}

	@GetMapping("index.html")
	public String renderLatest(Model model, @RequestParam(name = "p", required = false) Integer p,
							   HttpServletRequest req) {
		int page = p != null ? p : 0;
		List<INewsDto> reviews = reviewService.getReviews(RESULT_LIMIT + 1, RESULT_LIMIT * page);
		boolean hasNextPage = false;
		if (reviews.size() > RESULT_LIMIT) {
			hasNextPage = true;
			reviews = reviews.subList(0, reviews.size() - 1);
		}
		List<ICategoryDto> categories = categoryService.getRootCategories(true);
		String canonicalLink = req.getHeader("host") + "/review/index.html" + (page > 1 ? "?p=" + page : "");

		model.addAttribute("canonicalLink", canonicalLink);
		model.addAttribute("permaLink", canonicalLink);
		model.addAttribute("metaKeywords", siteSettingService.getSiteMetaKeywords());
		model.addAttribute("metaDescription", siteSettingService.getSiteMetaDescription());
		model.addAttribute("categories", categories);
		model.addAttribute("title", siteSettingService.getSiteTitle());
		model.addAttribute("reviewlist", reviews);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "review/index.html";
	}

	@GetMapping("category/{name}.html")
	public String renderCategory(Model model, @PathVariable String name,
								 @RequestParam(name = "p", required = false) Integer p,
								 HttpServletRequest req) {
		int page = p != null ? p : 0;
		List<INewsDto> reviews = reviewService.getReviewsByCategoryName(name, RESULT_LIMIT + 1, RESULT_LIMIT * page);
		boolean hasNextPage = false;
		if (reviews.size() > RESULT_LIMIT) {
			hasNextPage = true;
			reviews = reviews.subList(0, reviews.size() - 1);
		}
		ICategoryDto category = categoryService.getCategoryDtoByName(name);
		String title = category != null ? category.getTitle() : "Not found";
		List<ICategoryDto> categories = categoryService.getRootCategories(true);
		String canonicalLink = req.getHeader("host") + "/review/category/" + name + ".html" + (page > 1 ? "?p=" + page : "");

		model.addAttribute("canonicalLink", canonicalLink);
		model.addAttribute("permaLink", canonicalLink);
		model.addAttribute("metaKeywords", category != null ? category.getMetaKeywords() : null);
		model.addAttribute("metaDescription", category != null ? category.getMetaDescription() : null);
		model.addAttribute("categories", categories);
		model.addAttribute("title", title);
		model.addAttribute("reviewlist", reviews);
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);
		return "review/index.html";
	}

	@GetMapping({"{id:[0-9]+}.html", "{id:[0-9]+}", "{id:[0-9]+}/"})
	public RedirectView renderOne(@PathVariable Long id, HttpServletResponse res) throws IOException {
		INewsDto review = reviewService.getReviewById(id);
		if (review == null) {
			res.sendError(404);
			return null;
		}
		return new RedirectView("/review/" + review.getId() + "-" + review.getKey() + ".html");
	}

	@GetMapping("{id:[0-9]+}-{key}.html")
	public String renderOneByKey(Model model, @PathVariable Long id, @PathVariable String key,
								 HttpServletRequest req, HttpServletResponse res) throws IOException {
		INewsDto review = reviewService.getReviewById(id);
		if (review == null) {
			res.sendError(404);
			return null;
		}
		if (!review.getKey().equals(key)) {
			res.sendRedirect("/review/" + review.getId() + "-" + review.getKey() + ".html");
			return null;
		}

		List<ICategoryDto> categories = categoryService.getRootCategories(true);
		List<ReviewFieldDefinitionDto> fieldDefinitions = Collections.emptyList();
		Map<String, String> customFieldValues = Collections.emptyMap();

		if (review.getCategory() != null) {
			ICategoryDto cat = categoryService.getCategoryDtoByName(review.getCategory());
			if (cat != null) {
				fieldDefinitions = fieldDefinitionService.getByCategory(cat.getId());
			}
		}
		if (review instanceof ReviewContentDto rcd && rcd.getCustomFields() != null) {
			customFieldValues = rcd.getCustomFields();
		}

		model.addAttribute("canonicalLink", req.getHeader("host") + "/review/" + review.getId() + "-" + review.getKey() + ".html");
		model.addAttribute("permaLink", req.getHeader("host") + "/review/" + review.getId());
		model.addAttribute("metaKeywords", review.getMetaKeywords());
		model.addAttribute("metaDescription", review.getMetaDescription());
		model.addAttribute("categories", categories);
		model.addAttribute("title", review.getTitle());
		model.addAttribute("post", review);
		model.addAttribute("fieldDefinitions", fieldDefinitions);
		model.addAttribute("customFieldValues", customFieldValues);
		return "review/article.html";
	}
}
