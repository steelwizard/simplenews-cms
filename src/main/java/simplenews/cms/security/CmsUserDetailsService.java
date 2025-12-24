package simplenews.cms.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.UserAccount;
import simplenews.cms.service.IUserService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CmsUserDetailsService implements UserDetailsService {

	@Autowired
	IUserService userService;

	private static final Logger LOG = Logger.getLogger(CmsUserDetailsService.class.getName());

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserDto userAccount = null;
		userAccount = userService.getByUsername(username);

		if (userAccount != null) {
			org.springframework.security.core.userdetails.User userDetails =
					new org.springframework.security.core.userdetails.User(
							userAccount.getUsername(),
							userAccount.getPassword(),
							getAuthorities(Collections.singletonList(userAccount.getRole().toString()))
					);

			LOG.log(Level.FINE, userAccount.getUsername()+": "+userAccount.getRole().toString());
			return userDetails;
		}
		return null;
	}
	private static List<GrantedAuthority> getAuthorities (List<String> roles) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority("ROLE_"+role));
		}
		return authorities;
	}
}
