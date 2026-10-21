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
import org.springframework.web.util.UriComponentsBuilder;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewJsonDto;
import simplenews.cms.data.dto.ReviewTypeJsonDto;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INewsService;
import simplenews.cms.service.IReviewService;
import simplenews.cms.service.ISiteSettingService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/reviews")
public class ReviewController {

	public static final int RESULT_LIMIT = 10;

	@Autowired
	IReviewService reviewService;
	@Autowired
	INewsService newsService;
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
		ListQuery query = runQuery(type, p, sort, fieldKey, value);
		ReviewField filterField = query.filterField;

		String title = type.getTitle();
		if (filterField != null) title += ": " + filterField.getLabel() + " " + query.filterValue;
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(true);
		String host = req.getHeader("host");

		model.addAttribute("canonicalLink", (host == null ? "" : host) + "/reviews/" + type.getKey() + ".html");
		model.addAttribute("metaKeywords", siteSettingService.getSiteMetaKeywords());
		model.addAttribute("metaDescription", siteSettingService.getSiteMetaDescription());
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("title", title);
		model.addAttribute("reviewType", type);
		model.addAttribute("newslist", query.reviews);
		model.addAttribute("reviewValues", reviewService.getDisplayValues(query.reviews, true));
		String sortParam = query.byRating ? "rating" : null;
		String filterKey = filterField != null ? filterField.getKey() : null;
		int page = query.page;
		model.addAttribute("hasRating", reviewService.getRatingField(type.getId()) != null);
		model.addAttribute("byRating", query.byRating);
		model.addAttribute("filtered", filterField != null);
		model.addAttribute("newestUrl", listUrl(type, 0, null, filterKey, query.filterValue));
		model.addAttribute("ratingUrl", listUrl(type, 0, "rating", filterKey, query.filterValue));
		model.addAttribute("allUrl", listUrl(type, 0, sortParam, null, null));
		model.addAttribute("prevUrl", page > 0 ? listUrl(type, page - 1, sortParam, filterKey, query.filterValue) : null);
		model.addAttribute("nextUrl", query.hasNextPage ? listUrl(type, page + 1, sortParam, filterKey, query.filterValue) : null);
		model.addAttribute("page", page);
		return "reviews/index.html";
	}

	@GetMapping("index.json")
	public ResponseEntity<List<ReviewTypeJsonDto>> jsonTypes() {
		List<ReviewTypeJsonDto> types = reviewService.getReviewTypes().stream()
				.map(t -> new ReviewTypeJsonDto(t, reviewService.getFields(t.getId(), true)))
				.toList();
		return new ResponseEntity<>(types, HttpStatus.OK);
	}

	@GetMapping("{key:[a-z0-9\\-]+}.json")
	public ResponseEntity<List<ReviewJsonDto>> jsonList(@PathVariable String key,
														@RequestParam(name = "p", required = false) Integer p,
														@RequestParam(name = "sort", required = false) String sort,
														@RequestParam(name = "field", required = false) String fieldKey,
														@RequestParam(name = "value", required = false) String value,
														HttpServletResponse res) throws IOException {
		ReviewType type = reviewService.getReviewTypeByKey(key);
		if (type == null) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		ListQuery query = runQuery(type, p, sort, fieldKey, value);
		return new ResponseEntity<>(reviewService.toJson(query.reviews), HttpStatus.OK);
	}

	@GetMapping("{key:[a-z0-9\\-]+}/{id:[0-9]+}.json")
	public ResponseEntity<ReviewJsonDto> jsonOne(@PathVariable String key, @PathVariable Long id,
												 HttpServletResponse res) throws IOException {
		ReviewType type = reviewService.getReviewTypeByKey(key);
		INewsDto news = newsService.getNewsById(id);
		if (type == null || news == null || !Boolean.TRUE.equals(news.getVisible())
				|| !Objects.equals(reviewService.getReviewTypeIdForCategory(news.getCategory()), type.getId())) {
			res.sendError(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
		return new ResponseEntity<>(reviewService.toJson(List.of(news)).get(0), HttpStatus.OK);
	}

	private ListQuery runQuery(ReviewType type, Integer p, String sort, String fieldKey, String value) {
		ListQuery query = new ListQuery();
		query.page = p != null && p > 0 ? p : 0;
		query.byRating = "rating".equals(sort) && reviewService.getRatingField(type.getId()) != null;
		if (fieldKey != null && value != null && !value.isBlank()) {
			query.filterField = reviewService.getFields(type.getId(), true).stream()
					.filter(f -> f.isFilterable() && f.getKey().equals(fieldKey))
					.findFirst().orElse(null);
			if (query.filterField != null) query.filterValue = value.strip();
		}
		List<INewsDto> reviews = reviewService.findReviews(type, query.filterField, query.filterValue, query.byRating,
				RESULT_LIMIT + 1, RESULT_LIMIT * query.page);
		if (reviews.size() > RESULT_LIMIT) {
			query.hasNextPage = true;
			reviews = reviews.subList(0, RESULT_LIMIT);
		}
		query.reviews = reviews;
		return query;
	}

	private static String listUrl(ReviewType type, int page, String sort, String field, String value) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/reviews/" + type.getKey() + ".html");
		if (page > 0) builder.queryParam("p", page);
		if (sort != null) builder.queryParam("sort", sort);
		if (field != null) builder.queryParam("field", field).queryParam("value", value);
		return builder.encode().build().toUriString();
	}

	private static class ListQuery {
		int page;
		boolean byRating;
		ReviewField filterField;
		String filterValue;
		List<INewsDto> reviews;
		boolean hasNextPage;
	}
}
