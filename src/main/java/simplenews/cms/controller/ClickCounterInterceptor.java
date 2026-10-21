package simplenews.cms.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import simplenews.cms.service.IClickCounterService;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Counts a click for each successful public HTML page. Admin, files, JSON and the secret
 * readout are left out.
 */
@Component
public class ClickCounterInterceptor implements HandlerInterceptor {

	private static final Logger LOG = Logger.getLogger(ClickCounterInterceptor.class.getName());
	private static final Set<String> SKIPPED_EXTENSIONS = Set.of(
			"json", "css", "js", "png", "jpg", "jpeg", "gif", "ico", "svg", "woff", "woff2", "map");

	@Autowired
	IClickCounterService clickCounterService;

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
								Object handler, Exception ex) {
		if (!counts(request.getMethod(), response.getStatus(), path(request))) return;
		try {
			clickCounterService.recordClick();
		} catch (RuntimeException e) {
			LOG.log(Level.WARNING, "Could not count click", e);
		}
	}

	static boolean counts(String method, int status, String path) {
		if (!"GET".equals(method) || status != 200 || path == null || path.isEmpty()) return false;
		if (path.startsWith("/_private") || path.startsWith("/css/") || path.startsWith("/js/")
				|| path.startsWith("/media/") || path.startsWith("/download/")
				|| path.equals("/clicks") || path.startsWith("/clicks/")
				|| path.equals("/login") || path.equals("/logout") || path.equals("/error")
				|| path.startsWith("/actuator")) {
			return false;
		}
		int dot = path.lastIndexOf('.');
		int slash = path.lastIndexOf('/');
		if (dot > slash) {
			String extension = path.substring(dot + 1).toLowerCase();
			if (SKIPPED_EXTENSIONS.contains(extension)) return false;
		}
		return true;
	}

	private static String path(HttpServletRequest request) {
		String path = request.getServletPath();
		if (path == null || path.isEmpty()) path = request.getRequestURI();
		return path;
	}
}
