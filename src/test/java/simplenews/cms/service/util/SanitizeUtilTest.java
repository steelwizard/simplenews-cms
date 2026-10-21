package simplenews.cms.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanitizeUtilTest {

	// Ordinary formatting such as paragraphs and bold text is kept.
	@Test
	void keepsSimpleFormatting() {
		String clean = SanitizeUtil.sanitize("<p>Hello <strong>there</strong></p>");

		assertTrue(clean.contains("<p>"));
		assertTrue(clean.contains("<strong>there</strong>"));
	}

	// Scripts are removed so a post cannot run code in the reader's browser.
	@Test
	void removesScriptTags() {
		String clean = SanitizeUtil.sanitize("<p>Hi</p><script>alert(1)</script>");

		assertTrue(clean.contains("Hi"));
		assertFalse(clean.toLowerCase().contains("<script"));
	}

	// Colors and classes are removed because the editor is not allowed to store them.
	@Test
	void removesStylesAndClasses() {
		String clean = SanitizeUtil.sanitize("<p style=\"color:red\" class=\"x\">styled</p>");

		assertFalse(clean.contains("style="));
		assertFalse(clean.contains("class="));
		assertTrue(clean.contains("styled"));
	}

	// A normal link is kept, and a javascript link is not.
	@Test
	void keepsHttpLinksAndDropsJavascriptLinks() {
		String safe = SanitizeUtil.sanitize("<a href=\"https://example.com\">site</a>");
		String unsafe = SanitizeUtil.sanitize("<a href=\"javascript:alert(1)\">bad</a>");

		assertTrue(safe.contains("https://example.com"));
		assertFalse(unsafe.toLowerCase().contains("javascript:"));
	}

	// A table cell may span a whole number of columns, but not an arbitrary value.
	@Test
	void keepsNumericColspanOnly() {
		String kept = SanitizeUtil.sanitize("<table><tr><td colspan=\"2\">wide</td></tr></table>");
		String dropped = SanitizeUtil.sanitize("<table><tr><td colspan=\"2em\">wide</td></tr></table>");

		assertTrue(kept.contains("colspan"));
		assertFalse(dropped.contains("colspan"));
		assertTrue(dropped.contains("wide"));
	}
}
