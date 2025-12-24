package simplenews.cms.service.util;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class SanitizeUtil {
	//TODO: This sanitizer library seems to have vulnerable dependency. Replace it they don't update it.
	private static PolicyFactory sanitizer =
			Sanitizers.FORMATTING
				.and(Sanitizers.BLOCKS)
				.and(Sanitizers.IMAGES)
				.and(Sanitizers.TABLES)
				.and(Sanitizers.LINKS);

	public static String sanitize(String input) {
		return sanitizer.sanitize(input);
	}
}
