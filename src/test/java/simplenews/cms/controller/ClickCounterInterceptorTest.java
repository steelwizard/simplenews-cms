package simplenews.cms.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClickCounterInterceptorTest {

	// A published article counts as a click.
	@Test
	void publicPageCounts() {
		assertTrue(ClickCounterInterceptor.counts("GET", 200, "/index.html"));
		assertTrue(ClickCounterInterceptor.counts("GET", 200, "/9-test.html"));
		assertTrue(ClickCounterInterceptor.counts("GET", 200, "/reviews/album.html"));
		assertTrue(ClickCounterInterceptor.counts("GET", 200, "/pages/about.html"));
	}

	// Admin, files, JSON, errors and the secret readout do not count.
	@Test
	void privateAndAssetsDoNotCount() {
		assertFalse(ClickCounterInterceptor.counts("GET", 200, "/_private/admin"));
		assertFalse(ClickCounterInterceptor.counts("GET", 200, "/css/style-dark.css"));
		assertFalse(ClickCounterInterceptor.counts("GET", 200, "/index.json"));
		assertFalse(ClickCounterInterceptor.counts("GET", 200, "/clicks/abc"));
		assertFalse(ClickCounterInterceptor.counts("GET", 404, "/missing.html"));
		assertFalse(ClickCounterInterceptor.counts("POST", 200, "/index.html"));
		assertFalse(ClickCounterInterceptor.counts("GET", 200, "/download/4"));
	}
}
