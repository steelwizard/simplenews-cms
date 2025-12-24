package simplenews.cms.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.dto.CategoryDto;
import simplenews.cms.data.dto.UserCategoryDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.dto.UserEditDto;
import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.IUserCategoryService;
import simplenews.cms.service.IUserService;
import simplenews.cms.service.exception.PasswordConfirmNotEqualException;
import simplenews.cms.service.exception.UserAlreadyExistsException;
import simplenews.cms.service.exception.UserNotFoundException;

import jakarta.websocket.server.PathParam;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping(value = {"/_private/admin","/_private/admin/"})
public class UserAdminController {

	private static final Logger LOG = Logger.getLogger(UserAdminController.class.getName());

	@Autowired
	IUserService userService;

	@GetMapping(path = "/user-overview.html")
	public String getUserAdmin(Model model) {
		model.addAttribute("userlist", userService.getAllUsers());
		return "admin/user-overview.html";
	}

	@GetMapping(path = "/user-edit.html")
	public String getUserEdit(Model model, @PathParam("id") Long id) {
		Boolean isNewUser = false;
		UserEditDto userEditDto = new UserEditDto(userService.getById(id));

		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", userEditDto);
		model.addAttribute("action", "/_private/admin/user-edit.html");
		model.addAttribute("isNewUser", isNewUser);
		return "admin/user-edit.html";
	}

	@PostMapping(path = "/user-edit.html")
	public String postUserEdit(@ModelAttribute UserEditDto userEditDto, Model model) {
		Boolean isNewUser = false;
		UserDto updatedUserDto;
		StringBuilder errorStrb = new StringBuilder("");
		StringBuilder successStrb = new StringBuilder("");
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
			successStrb.append("Successfully edited user\n");
		}


		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", userEditDto);
		model.addAttribute("error", errorStrb.toString());
		model.addAttribute("success", successStrb.toString());
		model.addAttribute("action", "/_private/admin/user-edit.html");
		model.addAttribute("isNewUser", isNewUser);

		return "admin/user-edit.html";
	}


	@GetMapping(path = "/user-create.html")
	public String getUserCreate(Model model) {
		Boolean isNewUser = true;
		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", new UserEditDto());
		model.addAttribute("action", "/_private/admin/user-create.html");
		model.addAttribute("isNewUser", isNewUser);
		return "admin/user-edit.html";
	}

	@PostMapping(path = "/user-create.html")
	public String postUserCreate(@ModelAttribute UserEditDto userEditDto, Model model) {
		UserDto createdUserDto;
		StringBuilder errorStrb = new StringBuilder("");
		StringBuilder successStrb = new StringBuilder("");
		Boolean isNewUser = false;
		try {
			createdUserDto = userService.registerNewUserAccount(userEditDto);
		} catch (UserAlreadyExistsException e) {
			userEditDto.setUsername("");
			LOG.info(e.toString());
			errorStrb.append("Error: Username already exists.\n");
		} catch (PasswordConfirmNotEqualException e) {
			userEditDto.setPassword("");
			userEditDto.setPasswordConfirm("");
			LOG.info(e.toString());
			errorStrb.append("Error: Passwords didn't match.\n");
		}

		if (errorStrb.toString().isEmpty()) {
			successStrb.append("Successfully created user\n");
		}
		model.addAttribute("roles", EnumSet.allOf(RoleEnum.class));
		model.addAttribute("user", userEditDto);
		model.addAttribute("error", errorStrb.toString());
		model.addAttribute("success", successStrb.toString());
		model.addAttribute("action", "/_private/admin/user-create.html");
		model.addAttribute("isNewUser", isNewUser);

		return "admin/user-edit.html";
	}
}
