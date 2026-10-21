package simplenews.cms.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.dto.UserEditDto;
import simplenews.cms.data.entity.UserAccount;
import simplenews.cms.data.repository.UserAccountRepository;
import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.exception.PasswordConfirmNotEqualException;
import simplenews.cms.service.exception.UserAlreadyExistsException;
import simplenews.cms.service.exception.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

	private final PasswordEncoder encoder = new BCryptPasswordEncoder();
	private UserAccountRepository users;
	private UserService service;

	@BeforeEach
	void setUp() {
		users = mock(UserAccountRepository.class);
		service = new UserService();
		ReflectionTestUtils.setField(service, "userAccountRepository", users);
		ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("admin", "unused"));
		when(users.save(any(UserAccount.class))).thenAnswer(call -> call.getArgument(0));
	}

	@AfterEach
	void clearLogin() {
		SecurityContextHolder.clearContext();
	}

	// An editor is not an admin, but an admin counts as an editor.
	@Test
	void roleCheckUsesTheRoleLadder() {
		assertFalse(service.checkRoleAtLeast(RoleEnum.ADMIN, RoleEnum.EDITOR));
		assertTrue(service.checkRoleAtLeast(RoleEnum.EDITOR, RoleEnum.ADMIN));
	}

	// A new account stores a hashed password and starts as a registered user.
	@Test
	void newUserStoresHashedPassword() throws Exception {
		UserEditDto form = form("ada", "secret-1", "secret-1");

		service.registerNewUserAccount(form);

		UserAccount saved = savedUser();
		assertEquals("ada", saved.getUsername());
		assertEquals(RoleEnum.REGISTERED, saved.getRole());
		assertTrue(saved.getActive());
		assertFalse(saved.getPassword().equals("secret-1"));
		assertTrue(encoder.matches("secret-1", saved.getPassword()));
	}

	// Creating a user fails when the two password boxes do not match.
	@Test
	void newUserRejectsMismatchedPasswords() {
		UserEditDto form = form("ada", "secret-1", "other");

		assertThrows(PasswordConfirmNotEqualException.class, () -> service.registerNewUserAccount(form));
		verify(users, never()).save(any());
	}

	// Creating a user fails when that username is already taken.
	@Test
	void newUserRejectsDuplicateUsername() {
		when(users.existsByUsername("ada")).thenReturn(true);
		UserEditDto form = form("ada", "secret-1", "secret-1");

		assertThrows(UserAlreadyExistsException.class, () -> service.registerNewUserAccount(form));
		verify(users, never()).save(any());
	}

	// Saving a profile with an empty password leaves the old password in place.
	@Test
	void blankPasswordDoesNotReplaceTheOldOne() throws Exception {
		UserAccount existing = existing("ada", "old-secret");
		when(users.getByUsername("ada")).thenReturn(existing);
		UserEditDto form = form("ada", "", "");
		form.setDisplayName("Ada Lovelace");

		service.updateUser(form);

		UserAccount saved = savedUser();
		assertEquals(existing.getPassword(), saved.getPassword());
		assertTrue(encoder.matches("old-secret", saved.getPassword()));
		assertEquals("Ada Lovelace", saved.getDisplayName());
	}

	// A matching new password replaces the old one and is stored hashed.
	@Test
	void matchingPasswordReplacesTheOldOne() throws Exception {
		when(users.getByUsername("ada")).thenReturn(existing("ada", "old-secret"));
		UserEditDto form = form("ada", "new-secret", "new-secret");

		service.updateUser(form);

		UserAccount saved = savedUser();
		assertTrue(encoder.matches("new-secret", saved.getPassword()));
		assertFalse(encoder.matches("old-secret", saved.getPassword()));
	}

	// Changing a password fails when the confirmation does not match, and nothing is saved.
	@Test
	void passwordChangeRejectsAMismatch() {
		when(users.getByUsername("ada")).thenReturn(existing("ada", "old-secret"));
		UserEditDto form = form("ada", "new-secret", "nope");

		assertThrows(PasswordConfirmNotEqualException.class, () -> service.updateUser(form));
		verify(users, never()).save(any());
	}

	// Editing someone who does not exist fails.
	@Test
	void missingUserCannotBeUpdated() {
		when(users.getByUsername("ghost")).thenReturn(null);

		assertThrows(UserNotFoundException.class, () -> service.updateUser(form("ghost", "", "")));
	}

	private static UserEditDto form(String username, String password, String confirm) {
		UserEditDto form = new UserEditDto();
		form.setUsername(username);
		form.setPassword(password);
		form.setPasswordConfirm(confirm);
		form.setEmail(username + "@example.com");
		form.setDisplayName(username);
		return form;
	}

	private static UserAccount existing(String username, String rawPassword) {
		UserAccount account = new UserAccount();
		account.setId(4L);
		account.setUsername(username);
		account.setPassword(new BCryptPasswordEncoder().encode(rawPassword));
		account.setEmail(username + "@example.com");
		account.setDisplayName(username);
		account.setRole(RoleEnum.USER);
		account.setActive(true);
		return account;
	}

	private UserAccount savedUser() {
		ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
		verify(users).save(captor.capture());
		return captor.getValue();
	}
}
