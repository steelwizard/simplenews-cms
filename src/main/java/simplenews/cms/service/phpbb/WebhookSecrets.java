package simplenews.cms.service.phpbb;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class WebhookSecrets {

	private WebhookSecrets() {
	}

	public static boolean bearerMatches(String expected, String authorization) {
		if (expected == null || expected.isEmpty() || authorization == null) {
			return false;
		}
		String prefix = "Bearer ";
		if (authorization.length() <= prefix.length()
				|| !authorization.regionMatches(true, 0, prefix, 0, prefix.length())) {
			return false;
		}
		byte[] given = authorization.substring(prefix.length()).getBytes(StandardCharsets.UTF_8);
		byte[] wanted = expected.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(wanted, given);
	}
}
