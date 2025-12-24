package simplenews.cms.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.dto.UserEditDto;
import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.IUserService;
import simplenews.cms.service.UserService;
import simplenews.cms.service.exception.PasswordConfirmNotEqualException;
import simplenews.cms.service.exception.UserNotFoundException;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class ProfileController {
	private static final Logger LOG = Logger.getLogger(ProfileController.class.getName());
	@Autowired
	IUserService userService;

	@GetMapping("profile.html")
	public String getProfilePage(Model model, HttpServletRequest httpServletRequest) {
		UserDto user = userService.getByUsername(httpServletRequest.getUserPrincipal().getName());
		Long id = user.getId();
		Boolean isNewUser = false;
		UserEditDto userEditDto = new UserEditDto(userService.getById(id));

		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", userEditDto);
		model.addAttribute("action", "/_private/admin/profile.html");
		model.addAttribute("isNewUser", isNewUser);

		return "admin/profile.html";
	}

	@PostMapping("profile.html")
	public String postProfilePage(@ModelAttribute UserEditDto userEditDto, Model model) {
		Boolean isNewUser = false;
		UserDto updatedUserDto = new UserDto();
		StringBuilder errorStrb = new StringBuilder("");
		StringBuilder successStrb = new StringBuilder("");
		userEditDto.setRole(null);
		Map<ICategoryDto, Boolean> userCategories = new HashMap<>();
		try {
			updatedUserDto = userService.updateUser(userEditDto);
			LOG.info(String.format("updated User %s with id %d", updatedUserDto.getUsername(), updatedUserDto.getId()));
		} catch (PasswordConfirmNotEqualException e) {
			userEditDto.setPassword("");
			userEditDto.setPasswordConfirm("");
			LOG.info(e.toString());
			errorStrb.append("Error: Passwords didn't match.\n");
		} catch (UserNotFoundException e) {
			LOG.info(e.toString());
			errorStrb.append("Error: User not found");
		}

		if (errorStrb.toString().isEmpty()) {
			successStrb.append("successfully edited profile \n");
		}


		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", userEditDto);
		model.addAttribute("error", errorStrb.toString());
		model.addAttribute("success", successStrb.toString());
		model.addAttribute("action", "/_private/admin/user-edit.html");
		model.addAttribute("isNewUser", isNewUser);

		return "admin/profile.html";
	}
}

