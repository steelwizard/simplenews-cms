package simplenews.cms.service.util;

import java.util.Locale;

public class SlugUtil {

	/**
	 * Lowercase letters, digits and single dashes only. May return an empty string.
	 */
	public static String slugify(String source, int maxLength) {
		if (source == null) {
			return "";
		}
		String slug = source.strip().toLowerCase(Locale.ROOT);
		slug = slug.replaceAll("[\\s_]+", "-");
		slug = slug.replaceAll("[^a-z0-9\\-]", "");
		slug = slug.replaceAll("-{2,}", "-");
		slug = slug.replaceAll("^-|-$", "");
		if (slug.length() > maxLength) {
			slug = slug.substring(0, maxLength).replaceAll("-$", "");
		}
		return slug;
	}
}
