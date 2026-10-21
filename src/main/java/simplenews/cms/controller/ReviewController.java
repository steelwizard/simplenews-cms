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
import org.springframework.web.bind.annotation.RequestParam;
import simplenews.cms.data.dto.ReviewJsonDto;
import simplenews.cms.data.dto.ReviewListPage;
import simplenews.cms.data.dto.ReviewTypeJsonDto;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IReviewService;
import simplenews.cms.service.ISiteSettingService;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

	public static final int RESULT_LIMIT = 10;

	@Autowired
	IReviewService reviewService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	ISiteSettingService siteSettingService;

	@GetMapping("{key:[a-z0-9\\-]+}.html")
	public String renderList(Model model, @PathVariable String key,
							 @RequestParam(name = "p", required = false) Integer p,
							 @RequestParam(name = "sort", required = false) String sort,
							 @RequestParam(name = "field", required = false) String fieldKey,
							 @RequestParam(name = "value", required = false) String value,
							 HttpServletRequest req, HttpServletResponse res) throws IOException {
		ReviewListPage page = reviewService.listReviews(key, p, sort, fieldKey, value, RESULT_LIMIT);
		if (page == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		String host = req.getHeader("host");
		model.addAttribute("canonicalLink", (host == null ? "" : host) + "/reviews/" + page.type().getKey() + ".html");
		model.addAttribute("metaKeywords", siteSettingService.getSiteMetaKeywords());
		model.addAttribute("metaDescription", siteSettingService.getSiteMetaDescription());
		model.addAttribute("categories", categoryService.getRootCategories(true));
		model.addAttribute("title", page.title());
		model.addAttribute("reviewType", page.type());
		model.addAttribute("newslist", page.reviews());
		model.addAttribute("reviewValues", page.displayValues());
		model.addAttribute("hasRating", page.hasRating());
		model.addAttribute("byRating", page.byRating());
		model.addAttribute("filtered", page.filtered());
		model.addAttribute("newestUrl", page.newestUrl());
		model.addAttribute("ratingUrl", page.ratingUrl());
		model.addAttribute("allUrl", page.allUrl());
		model.addAttribute("prevUrl", page.prevUrl());
		model.addAttribute("nextUrl", page.nextUrl());
		model.addAttribute("page", page.page());
		return "reviews/index.html";
	}

	@GetMapping("index.json")
	public ResponseEntity<List<ReviewTypeJsonDto>> jsonTypes() {
		return new ResponseEntity<>(reviewService.listReviewTypesJson(), HttpStatus.OK);
	}

	@GetMapping("{key:[a-z0-9\\-]+}.json")
	public ResponseEntity<List<ReviewJsonDto>> jsonList(@PathVariable String key,
														@RequestParam(name = "p", required = false) Integer p,
														@RequestParam(name = "sort", required = false) String sort,
														@RequestParam(name = "field", required = false) String fieldKey,
														@RequestParam(name = "value", required = false) String value,
														HttpServletResponse res) throws IOException {
		List<ReviewJsonDto> reviews = reviewService.listReviewsJson(key, p, sort, fieldKey, value, RESULT_LIMIT);
		if (reviews == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return new ResponseEntity<>(reviews, HttpStatus.OK);
	}

	@GetMapping("{key:[a-z0-9\\-]+}/{id:[0-9]+}.json")
	public ResponseEntity<ReviewJsonDto> jsonOne(@PathVariable String key, @PathVariable Long id,
												 HttpServletResponse res) throws IOException {
		ReviewJsonDto review = reviewService.getPublishedReview(key, id);
		if (review == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return new ResponseEntity<>(review, HttpStatus.OK);
	}
}
