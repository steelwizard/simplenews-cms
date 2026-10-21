package simplenews.cms.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExcerptUtilTest {

	// A missing or empty post has nothing to shorten.
	@Test
	void blankContentStaysFull() {
		assertNull(ExcerptUtil.categoryExcerpt(null));
		assertNull(ExcerptUtil.categoryExcerpt("   "));
		assertNull(ExcerptUtil.categoryExcerpt("<p></p>"));
	}

	// A short post keeps its original HTML, so formatting is not stripped.
	@Test
	void shortPostIsNotExcerpted() {
		assertNull(ExcerptUtil.categoryExcerpt("<p>Hello <strong>there</strong></p>"));
	}

	// Tags and character entities do not count toward the 300 characters a reader sees.
	@Test
	void markupIsNotCounted() {
		String visible = "a".repeat(250);
		String html = "<p><strong>" + visible + "</strong></p>" + "&amp;".repeat(20);

		assertNull(ExcerptUtil.categoryExcerpt(html));
		assertEquals("Hello world", ExcerptUtil.toPlainText("<p>Hello</p><p>world</p>"));
		assertEquals("A & B", ExcerptUtil.toPlainText("A&nbsp;&amp;&nbsp;B"));
	}

	// Exactly 300 characters is still the whole post.
	@Test
	void threeHundredCharactersIsNotCut() {
		assertNull(ExcerptUtil.categoryExcerpt("a".repeat(300)));
	}

	// Longer text is cut at 300 characters and marked with an ellipsis.
	@Test
	void longerTextIsCutAtThreeHundredCharacters() {
		String excerpt = ExcerptUtil.categoryExcerpt("a".repeat(299) + "XY");

		assertEquals("a".repeat(299) + "X…", excerpt);
		assertFalse(excerpt.contains("Y"));
	}

	// Words that start after the cut, including words in a later paragraph, are left off the listing.
	@Test
	void textAfterTheCutIsDropped() {
		String html = "<p>" + "a".repeat(300) + "</p><p>tail of the post</p>";
		String excerpt = ExcerptUtil.categoryExcerpt(html);

		assertEquals("a".repeat(300) + "…", excerpt);
		assertFalse(excerpt.contains("tail"));
	}

	// An emoji is one character, and the cut does not slice it in half.
	@Test
	void cutDoesNotSplitAnEmoji() {
		String excerpt = ExcerptUtil.categoryExcerpt("🙂".repeat(301));

		assertTrue(excerpt.startsWith("🙂".repeat(300)));
		assertTrue(excerpt.endsWith("…"));
		assertEquals(301, excerpt.codePointCount(0, excerpt.length()));
	}
}
