package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewType;
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
		ReviewType type = reviewService.getReviewTypeByKey(key);
		if (type == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		int page = p != null && p > 0 ? p : 0;
		boolean byRating = "rating".equals(sort) && reviewService.getRatingField(type.getId()) != null;

		ReviewField filterField = null;
		if (fieldKey != null && value != null && !value.isBlank()) {
			filterField = reviewService.getFields(type.getId(), true).stream()
					.filter(f -> f.isFilterable() && f.getKey().equals(fieldKey))
					.findFirst().orElse(null);
		}

		List<INewsDto> reviews = reviewService.findReviews(type, filterField, value, byRating,
				RESULT_LIMIT + 1, RESULT_LIMIT * page);
		boolean hasNextPage = false;
		if (reviews.size() > RESULT_LIMIT) {
			hasNextPage = true;
			reviews = reviews.subList(0, RESULT_LIMIT);
		}

		String title = type.getTitle();
		if (filterField != null) title += ": " + filterField.getLabel() + " " + value.strip();
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(true);
		String host = req.getHeader("host");

		model.addAttribute("canonicalLink", (host == null ? "" : host) + "/reviews/" + type.getKey() + ".html");
		model.addAttribute("metaKeywords", siteSettingService.getSiteMetaKeywords());
		model.addAttribute("metaDescription", siteSettingService.getSiteMetaDescription());
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("title", title);
		model.addAttribute("reviewType", type);
		model.addAttribute("newslist", reviews);
		model.addAttribute("reviewValues", reviewService.getDisplayValues(reviews, true));
		String sortParam = byRating ? "rating" : null;
		String filterKey = filterField != null ? filterField.getKey() : null;
		String filterValue = filterField != null ? value.strip() : null;
		model.addAttribute("hasRating", reviewService.getRatingField(type.getId()) != null);
		model.addAttribute("byRating", byRating);
		model.addAttribute("filtered", filterField != null);
		model.addAttribute("newestUrl", listUrl(type, 0, null, filterKey, filterValue));
		model.addAttribute("ratingUrl", listUrl(type, 0, "rating", filterKey, filterValue));
		model.addAttribute("allUrl", listUrl(type, 0, sortParam, null, null));
		model.addAttribute("prevUrl", page > 0 ? listUrl(type, page - 1, sortParam, filterKey, filterValue) : null);
		model.addAttribute("nextUrl", hasNextPage ? listUrl(type, page + 1, sortParam, filterKey, filterValue) : null);
		model.addAttribute("page", page);
		return "reviews/index.html";
	}

	private static String listUrl(ReviewType type, int page, String sort, String field, String value) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/reviews/" + type.getKey() + ".html");
		if (page > 0) builder.queryParam("p", page);
		if (sort != null) builder.queryParam("sort", sort);
		if (field != null) builder.queryParam("field", field).queryParam("value", value);
		return builder.encode().build().toUriString();
	}
}
