package simplenews.cms.service;

import simplenews.cms.data.dto.ClickCounterSnapshot;

public interface IClickCounterService {

	/** Count one public page open. */
	void recordClick();

	/** The total and the secret. Creates the counter on first use. */
	ClickCounterSnapshot snapshot();

	/** The total when the secret matches, otherwise null. */
	Long totalForSecret(String secret);
}
