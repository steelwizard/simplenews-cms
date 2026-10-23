package simplenews.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.repository.NewsFieldValueRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.service.phpbb.PhpBbDiscussionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NewsServiceDiscussionTest {

	private NewsRepository news;
	private PhpBbDiscussionService discussion;
	private NewsService service;

	@BeforeEach
	void setUp() {
		news = mock(NewsRepository.class);
		discussion = mock(PhpBbDiscussionService.class);
		IUserService users = mock(IUserService.class);
		ICategoryService categories = mock(ICategoryService.class);
		IPermissionService permissions = mock(IPermissionService.class);
		INewsFileService files = mock(INewsFileService.class);
		Category category = new Category();
		category.setName("news");

		service = new NewsService();
		ReflectionTestUtils.setField(service, "newsRepository", news);
		ReflectionTestUtils.setField(service, "discussionService", discussion);
		ReflectionTestUtils.setField(service, "userService", users);
		ReflectionTestUtils.setField(service, "categoryService", categories);
		ReflectionTestUtils.setField(service, "permissionService", permissions);
		ReflectionTestUtils.setField(service, "newsFileService", files);
		ReflectionTestUtils.setField(service, "newsFieldValueRepository", mock(NewsFieldValueRepository.class));

		when(users.getByUsername("admin")).thenReturn(new UserDto());
		when(permissions.canCreate(any())).thenReturn(true);
		when(permissions.canEdit(any())).thenReturn(true);
		when(permissions.canDelete(any())).thenReturn(true);
		when(categories.getCategoryByName("news")).thenReturn(category);
		when(news.existsByKey(any())).thenReturn(false);
		when(news.save(any(News.class))).thenAnswer(call -> {
			News article = call.getArgument(0);
			if (article.getId() == null) {
				article.setId(9L);
			}
			return article;
		});
		when(files.getByNewsId(any())).thenReturn(List.of());
	}

	@Test
	void theArticleIsSavedWhenTheForumCannotBeReached() throws Exception {
		doThrow(new RuntimeException("forum down")).when(discussion).afterSave(any(), any(), any(), any());

		NewsDto saved = service.createNews(draft("Welcome", "test", true), "admin");

		assertEquals(9L, saved.getId());
		assertNull(saved.getPhpbbTopicId());
		verify(news).save(any(News.class));
	}

	@Test
	void savingRemembersThePreviousTitleForAnExistingTopic() throws Exception {
		News stored = new News();
		stored.setId(9L);
		stored.setTitle("Old");
		stored.setKey("test");
		stored.setVisible(true);
		stored.setCreated(new java.sql.Timestamp(System.currentTimeMillis()));
		stored.setLastModified(new java.sql.Timestamp(System.currentTimeMillis()));
		stored.setCategory(category());
		when(news.findById(9L)).thenReturn(Optional.of(stored));

		service.updateNews(9L, draft("New", "test", true), "admin");

		verify(discussion).afterSave(any(News.class), eq("Old"), eq("test"), eq(true));
	}

	private static NewsDto draft(String title, String key, boolean visible) {
		NewsDto dto = new NewsDto();
		dto.setTitle(title);
		dto.setKey(key);
		dto.setCategory("news");
		dto.setContent("Hello");
		dto.setVisible(visible);
		return dto;
	}

	private static Category category() {
		Category category = new Category();
		category.setName("news");
		return category;
	}
}
