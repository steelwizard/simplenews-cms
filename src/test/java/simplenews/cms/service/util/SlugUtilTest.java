package simplenews.cms.service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlugUtilTest {

	// A missing title becomes an empty address instead of crashing.
	@Test
	void nullSourceBecomesEmpty() {
		assertEquals("", SlugUtil.slugify(null, 80));
	}

	// Spaces and capital letters become a lowercase address with dashes.
	@Test
	void wordsBecomeLowercaseDashes() {
		assertEquals("kind-of-blue", SlugUtil.slugify("  Kind of Blue  ", 80));
	}

	// Punctuation is dropped, and underscores are treated like spaces.
	@Test
	void punctuationAndUnderscoresAreRemoved() {
		assertEquals("hello-world", SlugUtil.slugify("Hello, World!", 80));
		assertEquals("my-page", SlugUtil.slugify("my_page", 80));
	}

	// Several spaces or dashes in a row collapse into one dash.
	@Test
	void repeatedSeparatorsCollapse() {
		assertEquals("a-b", SlugUtil.slugify("a   ---   b", 80));
	}

	// A long address is cut at the limit, and a trailing dash is removed.
	@Test
	void longSlugIsCutAndDoesNotEndWithADash() {
		assertEquals("abc", SlugUtil.slugify("abc-def", 4));
	}
}
