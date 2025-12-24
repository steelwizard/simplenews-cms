package simplenews.cms.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.IUserCategoryService;
import simplenews.cms.service.IUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class UserCategoryAdminController {

	private static final Logger LOG = Logger.getLogger(UserCategoryAdminController.class.getName());

	@Autowired
	IUserCategoryService userCategoryService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IUserService userService;

	@GetMapping(path = "/ucategory.html")
	public String getUserAdmin(Model model, @RequestParam(value = "u", required = true) String username) {
		List<ICategoryDto> allCategories = categoryService.getAllCategoryDtos(false);
		Map<ICategoryDto, Boolean> userCategoryMap = userCategoryService.getUserCategoryMap(userService.getByUsername(username).getId());

		model.addAttribute("username", username);
		model.addAttribute("userCategories", userCategoryMap);
		model.addAttribute("allCategories", allCategories);
		return "admin/user-categories.html";
	}

	@PostMapping(path = "/ucategory.html")
	public String postUserAdmin(Model model, @RequestParam(value = "u", required = true) String username,
								@RequestParam MultiValueMap<String, String> userCategories) {
		UserDto user = userService.getByUsername(username);
		List<ICategoryDto> allCategories = categoryService.getAllCategoryDtos(false);
		if(userCategories==null) LOG.info("No data!!");
		else userCategories.entrySet().forEach(e -> LOG.info("Key: "+e.getKey().toString()+" Value: "+e.getValue().toString()+"\n"));
		List<String> enabledIds = userCategories.get("enabled");
		List<Long> enabledIdsParsed = new ArrayList<>();
		enabledIds.forEach(id -> enabledIdsParsed.add(Long.parseLong(id)));
		LOG.info("enabled IDs :"+enabledIdsParsed.toString()+" for user "+username);

		userCategoryService.saveUserCategoryMap(username, enabledIdsParsed);

		Map<ICategoryDto, Boolean> updatedCategories = userCategoryService.getUserCategoryMap(user.getId());

		model.addAttribute("username", username);
		model.addAttribute("userCategories", updatedCategories);
		model.addAttribute("allCategories", allCategories);
		return "admin/user-categories.html";
	}
}
