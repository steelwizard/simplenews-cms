package simplenews.cms.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import simplenews.cms.data.dto.ClickCounterSnapshot;
import simplenews.cms.service.IClickCounterService;

@ControllerAdvice(basePackages = "simplenews.cms.controller.admin")
public class ClickCounterAdminAdvice {

	@Autowired
	IClickCounterService clickCounterService;

	@ModelAttribute
	public void addClickCounter(Model model) {
		ClickCounterSnapshot snapshot = clickCounterService.snapshot();
		model.addAttribute("clickCount", snapshot.total());
		model.addAttribute("clickSecret", snapshot.secret());
	}
}
