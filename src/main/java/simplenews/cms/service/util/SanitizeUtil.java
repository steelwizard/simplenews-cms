package simplenews.cms.service.util;

import org.owasp.html.AttributePolicy;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class SanitizeUtil {
	//TODO: This sanitizer library seems to have vulnerable dependency. Replace it they don't update it.
	private static final AttributePolicy POSITIVE_INT = new AttributePolicy() {
		public String apply(String elementName, String attributeName, String value) {
			int n = value.length();
			if (n == 0) return null;
			for (int i = 0; i < n; i++) {
				char ch = value.charAt(i);
				if (ch < '0' || ch > '9') return null;
			}
			return value;
		}
	};

	private static PolicyFactory sanitizer =
			Sanitizers.FORMATTING
				.and(Sanitizers.BLOCKS)
				.and(Sanitizers.IMAGES)
				.and(Sanitizers.TABLES)
				.and(Sanitizers.LINKS)
				.and(new HtmlPolicyBuilder()
						.allowElements("td", "th")
						.allowAttributes("colspan", "rowspan").matching(POSITIVE_INT).onElements("td", "th")
						.toFactory());

	public static String sanitize(String input) {
		return sanitizer.sanitize(input);
	}
}
