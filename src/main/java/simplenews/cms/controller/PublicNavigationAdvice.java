package simplenews.cms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import simplenews.cms.data.entity.NavigationMenuEnum;
import simplenews.cms.service.INavigationService;

@ControllerAdvice(assignableTypes = {NewsController.class, PageController.class, ReviewController.class})
public class PublicNavigationAdvice {

	@Autowired
	INavigationService navigationService;

	@ModelAttribute
	public void addMenus(Model model) {
		model.addAttribute("headerMenu", navigationService.getMenu(NavigationMenuEnum.HEADER));
		model.addAttribute("sidebarMenu", navigationService.getMenu(NavigationMenuEnum.SIDEBAR));
	}
}
