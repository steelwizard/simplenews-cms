package simplenews.cms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class NewsLegacyRedirectController {

	@GetMapping({"/news", "/news/", "/news/index.html"})
	public RedirectView index() {
		return permanent("/index.html");
	}

	@GetMapping("/news/index.json")
	public RedirectView indexJson() {
		return permanent("/index.json");
	}

	@GetMapping("/news/category/index.json")
	public RedirectView categoriesJson() {
		return permanent("/category/index.json");
	}

	@GetMapping("/news/category/{name}.html")
	public RedirectView category(@PathVariable String name) {
		return permanent("/category/" + name + ".html");
	}

	@GetMapping("/news/category/{name}.json")
	public RedirectView categoryJson(@PathVariable String name) {
		return permanent("/category/" + name + ".json");
	}

	@GetMapping({"/news/{id:[0-9]+}", "/news/{id:[0-9]+}/", "/news/{id:[0-9]+}.html"})
	public RedirectView byId(@PathVariable long id) {
		return permanent("/" + id + ".html");
	}

	@GetMapping("/news/{id:[0-9]+}-{key}.html")
	public RedirectView byKey(@PathVariable long id, @PathVariable String key) {
		return permanent("/" + id + "-" + key + ".html");
	}

	@GetMapping("/news/{id:[0-9]+}.json")
	public RedirectView byIdJson(@PathVariable long id) {
		return permanent("/" + id + ".json");
	}

	@GetMapping("/news/{id:[0-9]+}-{key}.json")
	public RedirectView byKeyJson(@PathVariable long id, @PathVariable String key) {
		return permanent("/" + id + "-" + key + ".json");
	}

	private static RedirectView permanent(String url) {
		RedirectView redirect = new RedirectView(url);
		redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		redirect.setContextRelative(true);
		redirect.setPropagateQueryParams(true);
		return redirect;
	}
}
