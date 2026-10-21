package simplenews.cms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class IndexController {

	@GetMapping
	public RedirectView homePage() {
		RedirectView redirect = new RedirectView("/index.html");
		redirect.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
		return redirect;
	}

}
