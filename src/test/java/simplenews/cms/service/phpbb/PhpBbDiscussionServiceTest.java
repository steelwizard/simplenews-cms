package simplenews.cms.service.phpbb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.repository.NewsRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PhpBbDiscussionServiceTest {

	private NewsRepository news;
	private FakeClient client;
	private PhpBbProperties properties;
	private PhpBbDiscussionService service;

	@BeforeEach
	void setUp() {
		news = mock(NewsRepository.class);
		client = new FakeClient();
		properties = new PhpBbProperties();
		properties.setEnabled(true);
		properties.setForumId(2);
		properties.setSiteUrl("http://localhost:8080");
		properties.setBaseUrl("http://localhost:8080/forum");
		service = new PhpBbDiscussionService();
		ReflectionTestUtils.setField(service, "newsRepository", news);
		ReflectionTestUtils.setField(service, "client", client);
		ReflectionTestUtils.setField(service, "properties", properties);
	}

	@Test
	void savingAPublishedArticleDoesNotOpenATopic() {
		News article = article(9L, "Welcome", "test", true, null);

		service.afterSave(article, null, null, null);

		assertEquals(0, client.creates);
		assertNull(article.getPhpbbTopicId());
	}

	@Test
	void aSecondSaveDoesNotCallTheForum() {
		News article = article(9L, "Welcome", "test", true, 40L);

		service.afterSave(article, "Welcome", "test", true);

		assertEquals(0, client.creates);
		assertEquals(0, client.updates);
	}

	@Test
	void aHiddenArticleDoesNotOpenATopic() {
		News article = article(9L, "Draft", "draft", false, null);
		when(news.findById(9L)).thenReturn(Optional.of(article));

		service.afterSave(article, null, null, null);

		assertEquals(0, client.creates);
		assertThrows(IllegalArgumentException.class, () -> service.open(9L));
	}

	@Test
	void theFirstVisitOpensOneTopic() {
		News article = article(9L, "Welcome", "test", true, null);
		when(news.findById(9L)).thenReturn(Optional.of(article));

		String url = service.open(9L);

		assertEquals(1, client.creates);
		assertEquals(40L, article.getPhpbbTopicId());
		assertEquals(0, article.getPhpbbReplyCount());
		assertEquals("http://localhost:8080/9-test.html", client.lastPermalink);
		assertEquals(2L, client.lastForumId);
		assertEquals("http://localhost:8080/forum/viewtopic.php?t=40", url);
		verify(news).save(article);
	}

	@Test
	void aLaterVisitUsesTheExistingTopic() {
		News article = article(9L, "Welcome", "test", true, 40L);
		when(news.findById(9L)).thenReturn(Optional.of(article));

		String url = service.open(9L);

		assertEquals(0, client.creates);
		assertEquals("http://localhost:8080/forum/viewtopic.php?t=40", url);
		verify(news, never()).save(article);
	}

	@Test
	void aForumOutageLeavesTheArticleWithoutATopic() {
		News article = article(9L, "Welcome", "test", true, null);
		when(news.findById(9L)).thenReturn(Optional.of(article));
		client.fail = true;

		assertThrows(PhpBbException.class, () -> service.open(9L));

		assertNull(article.getPhpbbTopicId());
		verify(news, never()).save(article);
	}

	@Test
	void renamingAnExistingTopicUpdatesIt() {
		News article = article(9L, "Renamed", "test", true, 40L);

		service.afterSave(article, "Welcome", "test", true);

		assertEquals(0, client.creates);
		assertEquals(1, client.updates);
		assertFalse(client.lastLocked);
		assertEquals("Renamed", client.lastTitle);
	}

	@Test
	void changingTheKeyUpdatesThePermalink() {
		News article = article(9L, "Welcome", "newer", true, 40L);

		service.afterSave(article, "Welcome", "test", true);

		assertEquals(1, client.updates);
		assertEquals("http://localhost:8080/9-newer.html", client.lastPermalink);
	}

	@Test
	void unpublishingLocksTheTopic() {
		News article = article(9L, "Welcome", "test", false, 40L);

		service.afterSave(article, "Welcome", "test", true);

		assertTrue(client.lastLocked);
	}

	@Test
	void publishingAgainUnlocksTheTopic() {
		News article = article(9L, "Welcome", "test", true, 40L);

		service.afterSave(article, "Welcome", "test", false);

		assertEquals(1, client.updates);
		assertFalse(client.lastLocked);
	}

	@Test
	void deletingLocksTheTopic() {
		News article = article(9L, "Welcome", "test", true, 40L);

		service.afterDelete(article);

		assertTrue(client.lastLocked);
	}

	@Test
	void deletingAnArticleWithoutATopicDoesNothing() {
		service.afterDelete(article(9L, "Welcome", "test", true, null));

		assertEquals(0, client.updates);
	}

	@Test
	void commentsCanBeSwitchedOff() {
		properties.setEnabled(false);
		News article = article(9L, "Welcome", "test", true, null);
		when(news.findById(9L)).thenReturn(Optional.of(article));

		service.afterSave(article, null, null, null);
		assertThrows(DiscussionClosedException.class, () -> service.open(9L));

		assertEquals(0, client.creates);
		assertEquals(0, client.updates);
	}

	@Test
	void aForumOutageWhileSavingDoesNotEscape() {
		News article = article(9L, "Renamed", "test", true, 40L);
		client.fail = true;

		service.afterSave(article, "Welcome", "test", true);

		assertEquals(40L, article.getPhpbbTopicId());
	}

	private static News article(long id, String title, String key, boolean visible, Long topicId) {
		News article = new News();
		article.setId(id);
		article.setTitle(title);
		article.setKey(key);
		article.setVisible(visible);
		article.setPhpbbTopicId(topicId);
		return article;
	}

	private static final class FakeClient implements PhpBbClient {
		int creates;
		int updates;
		boolean fail;
		boolean lastLocked;
		String lastPermalink;
		String lastTitle;
		long lastForumId;
		long nextId = 40;

		@Override
		public long createTopic(long newsId, String title, String permalink, long forumId) {
			if (fail) {
				throw new PhpBbException("down");
			}
			creates++;
			lastTitle = title;
			lastPermalink = permalink;
			lastForumId = forumId;
			return nextId++;
		}

		@Override
		public void updateTopic(long newsId, String title, String permalink, boolean locked) {
			if (fail) {
				throw new PhpBbException("down");
			}
			updates++;
			lastTitle = title;
			lastPermalink = permalink;
			lastLocked = locked;
		}
	}
}
