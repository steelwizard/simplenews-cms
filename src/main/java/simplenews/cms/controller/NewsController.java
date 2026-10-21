package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INewsService;
import simplenews.cms.service.IReviewService;

import jakarta.servlet.http.HttpServletResponse;
import simplenews.cms.service.ISiteSettingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("news")
public class NewsController {

	public static final int RESULT_LIMIT = 10;

	@Autowired
	private INewsService newsService;
	@Autowired
	private ICategoryService categoryService;
	@Autowired
	ISiteSettingService siteSettingService;
	@Autowired
	IReviewService reviewService;

	@GetMapping("/")
	public RedirectView getDefaultPage() {
		return redirectToIndex();
	}

	@GetMapping("index.html")
	public String renderLatest(Model model, @RequestParam(name = "p", required = false) Integer p, HttpServletRequest req) {
		int page = p != null ? p : 0;
		List<INewsDto> newslist = newsService.getNews(RESULT_LIMIT+1, RESULT_LIMIT*page);
		boolean hasNextPage = false;
		if (newslist.size() > NewsController.RESULT_LIMIT) {
			hasNextPage = true;
			newslist = newslist.subList(0, newslist.size() - 1);
		}
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(true);

		StringBuilder canonicalLink = new StringBuilder(req.getHeader("host"));
		canonicalLink.append("/news/index.html");
		if (page > 1) canonicalLink.append("?p="+page);

		model.addAttribute("canonicalLink", canonicalLink.toString());
		model.addAttribute("permaLink", canonicalLink.toString());
		model.addAttribute("metaKeywords", siteSettingService.getSiteMetaKeywords());
		model.addAttribute("metaDescription", siteSettingService.getSiteMetaDescription());
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("title", siteSettingService.getSiteTitle());
		model.addAttribute("newslist", newslist);
		model.addAttribute("reviewValues", reviewService.getDisplayValues(newslist, true));
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);


		return "news/index.html";
	}

	@GetMapping("category/{name}.html")
	public String renderCategory(Model model, @PathVariable String name,
								 @RequestParam(name = "p", required = false)
								 Integer p, HttpServletRequest req) {
		int page = p != null ? p : 0;
		List<INewsDto> newslist = newsService.getNewsByCategoryName(name, RESULT_LIMIT+1, RESULT_LIMIT*page);
		boolean hasNextPage = false;
		if (newslist.size() > RESULT_LIMIT) {
			hasNextPage = true;
			newslist = newslist.subList(0, newslist.size() - 1);
		}
		ICategoryDto category = categoryService.getCategoryDtoByName(name);
		String title = category != null ? category.getTitle() : "Not found";
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(true);

		StringBuilder canonicalLink = new StringBuilder(req.getHeader("host"));
		canonicalLink.append("/category/");
		canonicalLink.append(category.getName());
		canonicalLink.append(".html");
		if (page > 1) canonicalLink.append("?p="+page);

		model.addAttribute("canonicalLink", canonicalLink.toString());
		model.addAttribute("permaLink", canonicalLink.toString());
		model.addAttribute("metaKeywords", category.getMetaKeywords());
		model.addAttribute("metaDescription", category.getMetaDescription());
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("title", title);
		model.addAttribute("newslist", newslist);
		model.addAttribute("reviewValues", reviewService.getDisplayValues(newslist, true));
		model.addAttribute("hasNextPage", hasNextPage);
		model.addAttribute("page", page);

		return "news/index.html";
	}

	@GetMapping({"{id:[0-9]+}.html","{id:[0-9]+}","{id:[0-9]+}/"})
	public RedirectView renderOne(Model model, @PathVariable Long id, HttpServletResponse res) throws IOException {
		List<INewsDto> newslist = new ArrayList<>();
		INewsDto news = newsService.getNewsById(id);
		if (news == null) {
			res.sendError(404);
			return null;
		}

		return new RedirectView("/news/"+news.getId()+"-"+news.getKey()+".html");
	}

	@GetMapping("{id:[0-9]+}-{key}.html")
	public String renderOneByKey(Model model, @PathVariable Long id,
								 @PathVariable String key, HttpServletRequest req,
								 HttpServletResponse res) throws IOException {
		List<INewsDto> newslist = new ArrayList<>();
		INewsDto news = newsService.getNewsById(id);
		if (news == null) {
			res.sendError(404);
			return null;
		}
		if (!news.getKey().equals(key)) {
			res.sendRedirect("/news/"+news.getId()+"-"+news.getKey()+".html");
			return null;
		}
		newslist.add(news);
		List<ICategoryDto> categoryDtos = categoryService.getRootCategories(true);

		model.addAttribute("canonicalLink", req.getHeader("host")+"/news/"+news.getId()+"-"+news.getKey()+".html");
		model.addAttribute("permaLink", req.getHeader("host")+"/news/"+news.getId());
		model.addAttribute("metaKeywords", news.getMetaKeywords());
		model.addAttribute("metaDescription", news.getMetaDescription());
		model.addAttribute("categories", categoryDtos);
		model.addAttribute("title", newslist.get(0).getTitle());
		model.addAttribute("newslist", newslist);
		model.addAttribute("reviewValues", reviewService.getDisplayValues(newslist, false));
		model.addAttribute("page", -1);

		return "news/article.html";
	}

	@GetMapping("index.json")
	public ResponseEntity<List<INewsDto>> jsonLatest(@RequestParam(name = "p", required = false) Integer p) {
		int page = p != null ? p : 0;
		List<INewsDto> newslist = newsService.getNews(RESULT_LIMIT+1, RESULT_LIMIT*page);
		boolean hasNextPage = false;
		if (newslist.size() > RESULT_LIMIT) {
			hasNextPage = true;
			newslist = newslist.subList(0, newslist.size() - 1);
		}
		return new ResponseEntity<>(newslist, HttpStatus.OK);
	}


	@GetMapping("category/{name}.json")
	public  ResponseEntity<List<INewsDto>> jsonCategory(@PathVariable String name, @RequestParam(name = "p", required = false)
														Integer p) {
		int page = p != null ? p : 0;
		List<INewsDto> newslist = newsService.getNewsByCategoryName(name, RESULT_LIMIT+1, RESULT_LIMIT*page);
		boolean hasNextPage = false;
		if (newslist.size() > RESULT_LIMIT) {
			hasNextPage = true;
			newslist = newslist.subList(0, newslist.size() - 1);
		}
		return new ResponseEntity<>(newslist, HttpStatus.OK);
	}

	@GetMapping("category/index.json")
	public  ResponseEntity<List<ICategoryDto>> jsonCategoryIndex() {
		List<ICategoryDto> categories = categoryService.getAllCategoryDtos(true);
		return new ResponseEntity<>(categories, HttpStatus.OK);
	}

	@GetMapping("{id:[0-9]+}.json")
	public ResponseEntity<INewsDto> jsonOne(@PathVariable Long id, HttpServletResponse res) throws IOException {
		INewsDto news = newsService.getNewsById(id);
		if (news == null) {
			res.sendError(404);
			return null;
		}
		return new ResponseEntity<>(news, HttpStatus.OK);
	}

	@GetMapping("{id:[0-9]+}-{key}.json")
	public ResponseEntity<INewsDto> jsonOneByKey(@PathVariable Long id, @PathVariable String key,
												 HttpServletResponse res) throws IOException {
		INewsDto news = newsService.getNewsById(id);
		if (news == null) {
			res.sendError(404);
			return null;
		}
		if (!news.getKey().equals(key)) {
			res.sendRedirect("/news/"+news.getId()+"-"+news.getKey()+".json");
			return null;
		}
		return new ResponseEntity<>(news, HttpStatus.OK);
	}
	@GetMapping
	public RedirectView redirectToIndex() {
		RedirectView redirect = new RedirectView("/news/index.html");
		redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		return redirect;
	}
}
