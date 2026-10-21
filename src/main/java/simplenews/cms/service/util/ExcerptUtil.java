package simplenews.cms.service.util;

import org.springframework.web.util.HtmlUtils;

/**
 * Short text for a category listing. Markup is not counted: a reader sees 300 characters, then a read-more link.
 */
public final class ExcerptUtil {

	public static final int CATEGORY_EXCERPT_LENGTH = 300;

	private ExcerptUtil() {
	}

	/**
	 * @return the first {@link #CATEGORY_EXCERPT_LENGTH} characters of visible text, plus an ellipsis,
	 * or null when the post is already short enough to show in full
	 */
	public static String categoryExcerpt(String html) {
		String text = toPlainText(html);
		if (text.codePointCount(0, text.length()) <= CATEGORY_EXCERPT_LENGTH) {
			return null;
		}
		int end = text.offsetByCodePoints(0, CATEGORY_EXCERPT_LENGTH);
		return text.substring(0, end).stripTrailing() + "…";
	}

	static String toPlainText(String html) {
		if (html == null || html.isBlank()) {
			return "";
		}
		String text = html.replaceAll("<[^>]+>", " ");
		text = HtmlUtils.htmlUnescape(text);
		text = text.replace('\u00A0', ' ');
		return text.replaceAll("\\s+", " ").strip();
	}
}
