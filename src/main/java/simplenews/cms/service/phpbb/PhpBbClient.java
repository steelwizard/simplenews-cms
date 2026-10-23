package simplenews.cms.service.phpbb;

public interface PhpBbClient {

	/**
	 * Opens a topic for this article, or returns the topic id if one already exists.
	 */
	long createTopic(long newsId, String title, String permalink, long forumId);

	void updateTopic(long newsId, String title, String permalink, boolean locked);
}
