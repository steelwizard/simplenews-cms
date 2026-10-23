package simplenews.cms.service.phpbb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.repository.NewsRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class PhpBbDiscussionService {

	private static final Logger LOG = Logger.getLogger(PhpBbDiscussionService.class.getName());

	@Autowired
	PhpBbProperties properties;
	@Autowired
	PhpBbClient client;
	@Autowired
	NewsRepository newsRepository;

	public void afterSave(News news, String previousTitle, String previousKey, Boolean previousVisible) {
		if (!properties.isEnabled() || news.getId() == null || news.getPhpbbTopicId() == null) {
			return;
		}
		try {
			boolean visible = Boolean.TRUE.equals(news.getVisible());
			boolean wasVisible = Boolean.TRUE.equals(previousVisible);
			boolean titleChanged = previousTitle != null && !previousTitle.equals(news.getTitle());
			boolean keyChanged = previousKey != null && !previousKey.equals(news.getKey());
			boolean visibilityChanged = previousVisible != null && wasVisible != visible;
			if (!titleChanged && !keyChanged && !visibilityChanged) {
				return;
			}
			client.updateTopic(news.getId(), title(news), permalink(news), !visible);
		} catch (RuntimeException ex) {
			LOG.log(Level.WARNING, "phpBB discussion was not updated for article " + news.getId(), ex);
		}
	}

	/**
	 * Returns the public topic URL, creating the topic on the first visit.
	 * Publishing an article does not open a topic, so comments can stay off
	 * and a missing forum does not affect the rest of the site.
	 */
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public String open(long newsId) {
		if (!properties.isEnabled()) {
			throw new DiscussionClosedException();
		}
		News news = newsRepository.findById(newsId)
				.orElseThrow(() -> new IllegalArgumentException("Article doesn't exist"));
		if (!Boolean.TRUE.equals(news.getVisible())) {
			throw new IllegalArgumentException("Article doesn't exist");
		}
		if (news.getPhpbbTopicId() == null) {
			long topicId = client.createTopic(news.getId(), title(news), permalink(news), properties.getForumId());
			news.setPhpbbTopicId(topicId);
			news.setPhpbbReplyCount(0);
			newsRepository.save(news);
		}
		return topicUrl(news.getPhpbbTopicId());
	}

	public void afterDelete(News news) {
		if (!properties.isEnabled() || news.getPhpbbTopicId() == null) {
			return;
		}
		try {
			client.updateTopic(news.getId(), title(news), permalink(news), true);
		} catch (RuntimeException ex) {
			LOG.log(Level.WARNING, "phpBB discussion was not locked for article " + news.getId(), ex);
		}
	}

	@Transactional
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public boolean recordReplyCount(long topicId, int replyCount) {
		if (replyCount < 0) {
			throw new IllegalArgumentException("reply count");
		}
		return newsRepository.updateReplyCount(topicId, replyCount) > 0;
	}

	private String title(News news) {
		if (news.getTitle() == null || news.getTitle().isBlank()) {
			return "Article " + news.getId();
		}
		return news.getTitle();
	}

	private String topicUrl(long topicId) {
		String base = properties.publicBaseUrl();
		if (base.isEmpty()) {
			throw new PhpBbException("cms.phpbb.base-url is not set");
		}
		return base + "/viewtopic.php?t=" + topicId;
	}

	private String permalink(News news) {
		String base = properties.publicSiteUrl();
		if (base.isEmpty()) {
			throw new PhpBbException("cms.phpbb.site-url is not set");
		}
		return base + "/" + news.getId() + "-" + news.getKey() + ".html";
	}
}
