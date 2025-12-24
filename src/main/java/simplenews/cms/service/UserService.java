package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import simplenews.cms.service.exception.PasswordConfirmNotEqualException;
import simplenews.cms.data.repository.UserAccountRepository;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.dto.UserEditDto;
import simplenews.cms.data.entity.UserAccount;
import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.exception.UserAlreadyExistsException;
import simplenews.cms.service.exception.UserNotFoundException;

import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService {

	private static final Logger LOG = Logger.getLogger(UserService.class.getName());
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private UserAccountRepository userAccountRepository;

	public UserDto registerNewUserAccount(UserEditDto userEditDto) throws PasswordConfirmNotEqualException,
			UserAlreadyExistsException {
		if (userAccountRepository.existsByUsername(userEditDto.getUsername())) {
			throw new UserAlreadyExistsException(userEditDto.getUsername());
		}
		if (!userEditDto.getPassword().equals(userEditDto.getPasswordConfirm())) {
			throw new PasswordConfirmNotEqualException();
		}
		// TODO: Password length check
		UserAccount userAccount = new UserAccount();

		userAccount.setUsername(userEditDto.getUsername());
		userAccount.setPassword(passwordEncoder.encode(userEditDto.getPassword()));
		userAccount.setDisplayName(userEditDto.getDisplayName());
		userAccount.setEmail(userEditDto.getEmail());
		userAccount.setRole(RoleEnum.REGISTERED);
		userAccount.setActive(true);
		userAccount.setAlg(0);
		userAccount.setCreated(new Timestamp(System.currentTimeMillis()));
		userAccount.setLastModified(new Timestamp(System.currentTimeMillis()));

		LOG.info("user "+userAccount.getId()+" was updated by"+getCurrentUserName());
		return new UserDto(userAccountRepository.save(userAccount));
	}

	public UserDto updateUser(UserEditDto userEditDto) throws PasswordConfirmNotEqualException, UserNotFoundException {
		UserAccount userAccount = userAccountRepository.getByUsername(userEditDto.getUsername());
		if (userAccount == null) {
			throw new UserNotFoundException("Trying to update nonexistent user "+userEditDto.getUsername());
		}
		// TODO: check if email already exists
		if (userEditDto.getPassword() != null && !userEditDto.getPassword().isEmpty()) {
			// TODO: Password length check
			if (!userEditDto.getPassword().equals(userEditDto.getPasswordConfirm())) {
				throw new PasswordConfirmNotEqualException();
			}
			userAccount.setPassword(passwordEncoder.encode(userEditDto.getPassword()));
		}

		if (userEditDto.getEmail() != null && !userEditDto.getEmail().isEmpty() && !userEditDto.equals(userAccount.getEmail())) {
			userAccount.setEmail(userEditDto.getEmail());
		}
		if (userEditDto.getDisplayName() != null && !userEditDto.getDisplayName().isEmpty() && !userEditDto.getDisplayName().equals(userAccount.getDisplayName())) {
			userAccount.setDisplayName(userEditDto.getDisplayName());
		}
		if (userEditDto.getRole() != null && userEditDto.getRole() != userAccount.getRole()) {
			userAccount.setRole(userEditDto.getRole());
		}
		UserAccount save = userAccountRepository.save(userAccount);

		LOG.info("user "+save.getId()+" was updated by "+getCurrentUserName());
		return new UserDto(save);
	}

	public UserDto getByUsername(String username) {
		UserAccount userAccount = userAccountRepository.getByUsername(username);
		return new UserDto(userAccount);
	}

	public UserDto getById(Long id) {
		UserAccount userAccount = userAccountRepository.getReferenceById(id);
		return new UserDto(userAccount);
	}

	public List<UserDto> getAllUsers() {
		return userAccountRepository.findAll().stream()
				.map(userAccount -> {
					return new UserDto(userAccount);
				}).collect(Collectors.toList());
	}

	public boolean checkRoleAtLeast(RoleEnum roleRequired, RoleEnum userRole) {
		return userRole.getOrder()>=roleRequired.getOrder();
	}

	@Override
	public String getCurrentUserName() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUserName = authentication.getName();
			return currentUserName;
		}
		return "Anonymous";
	}
}
