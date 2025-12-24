package simplenews.cms.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			.formLogin(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth
					// Specific Admin-only routes
					.requestMatchers("/_private/admin/user-*", "/_private/admin/site-settings*", "/actuator/*")
					.hasRole(RoleEnum.ADMIN.name())
					.requestMatchers("/_private/admin/category*")
					.hasAnyRole(RoleEnum.ADMIN.name(), RoleEnum.EDITOR.name())
					// General Admin area access
					.requestMatchers("/_private/admin", "/_private/admin/**").hasAnyRole(
							RoleEnum.ADMIN.name(),
							RoleEnum.EDITOR.name(),
							RoleEnum.USER.name())
					// Public routes
					.requestMatchers("/**").permitAll()
					.anyRequest().authenticated());

		return http.build();
	}
}
