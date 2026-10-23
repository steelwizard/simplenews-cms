package simplenews.cms.service.phpbb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookSecretsTest {

	@Test
	void aMatchingBearerTokenIsAccepted() {
		assertTrue(WebhookSecrets.bearerMatches("dev-webhook-key", "Bearer dev-webhook-key"));
	}

	@Test
	void anythingElseIsRejected() {
		assertFalse(WebhookSecrets.bearerMatches("dev-webhook-key", "Bearer other"));
		assertFalse(WebhookSecrets.bearerMatches("dev-webhook-key", "dev-webhook-key"));
		assertFalse(WebhookSecrets.bearerMatches("", "Bearer "));
		assertFalse(WebhookSecrets.bearerMatches("dev-webhook-key", null));
	}
}
