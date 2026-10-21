package simplenews.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.dto.PageDto;
import simplenews.cms.data.entity.StaticPage;
import simplenews.cms.data.repository.NavigationItemRepository;
import simplenews.cms.data.repository.StaticPageRepository;
import simplenews.cms.security.PermissionEnum;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PageServiceTest {

	private StaticPageRepository pages;
	private IPermissionService permissions;
	private PageService service;

	@BeforeEach
	void setUp() {
		pages = mock(StaticPageRepository.class);
		permissions = mock(IPermissionService.class);
		service = new PageService();
		ReflectionTestUtils.setField(service, "pageRepository", pages);
		ReflectionTestUtils.setField(service, "permissionService", permissions);
		ReflectionTestUtils.setField(service, "navigationRepository", mock(NavigationItemRepository.class));
		ReflectionTestUtils.setField(service, "eventPublisher", mock(ApplicationEventPublisher.class));
		when(pages.save(any(StaticPage.class))).thenAnswer(call -> {
			StaticPage page = call.getArgument(0);
			page.setId(8L);
			return page;
		});
	}

	// A blank page key is built from the title.
	@Test
	void blankKeyComesFromTheTitle() {
		PageDto page = page("About Us", "  ");

		service.prepareKey(page);

		assertEquals("about-us", page.getKey());
	}

	// The address "index" is reserved, so a page cannot take it.
	@Test
	void indexKeyIsRejected() {
		PageDto page = page("Home", "Index");

		IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> service.prepareKey(page));

		assertTrue(error.getMessage().contains("index"));
	}

	// A title that has no usable letters still gets a generated address.
	@Test
	void titleWithNoLettersGetsAGeneratedKey() {
		PageDto page = page("!!!", "");

		service.prepareKey(page);

		assertTrue(page.getKey().startsWith("page-"));
	}

	// A page needs a title.
	@Test
	void blankTitleIsRejected() {
		when(permissions.hasPermission(PermissionEnum.EDIT_ALL)).thenReturn(true);

		assertThrows(IllegalArgumentException.class, () -> service.create(page("  ", "about"), "ada"));
		verify(pages, never()).save(any());
	}

	// Someone who is not an editor cannot create a page.
	@Test
	void createRequiresAnEditor() {
		when(permissions.hasPermission(PermissionEnum.EDIT_ALL)).thenReturn(false);

		assertThrows(InsufficientPermissionException.class, () -> service.create(page("About", "about"), "ada"));
		verify(pages, never()).save(any());
	}

	// Two pages cannot share the same address.
	@Test
	void duplicateKeyIsRejected() {
		when(permissions.hasPermission(PermissionEnum.EDIT_ALL)).thenReturn(true);
		when(pages.existsByKey("about")).thenReturn(true);

		assertThrows(DuplicateKeyException.class, () -> service.create(page("About", "about"), "ada"));
		verify(pages, never()).save(any());
	}

	// Page text is cleaned before it is stored, and empty meta fields are left empty.
	@Test
	void createSanitizesContent() throws Exception {
		when(permissions.hasPermission(PermissionEnum.EDIT_ALL)).thenReturn(true);
		PageDto page = page("About", "about");
		page.setContent("<p>Hello</p><script>alert(1)</script>");
		page.setVisible(true);
		page.setMetaKeywords("   ");
		page.setMetaDescription("Who we are");

		service.create(page, "ada");

		StaticPage saved = savedPage();
		assertEquals("about", saved.getKey());
		assertEquals("About", saved.getTitle());
		assertEquals("ada", saved.getCreator());
		assertTrue(saved.getVisible());
		assertTrue(saved.getContent().contains("Hello"));
		assertFalse(saved.getContent().toLowerCase().contains("<script"));
		assertNull(saved.getMetaKeywords());
		assertEquals("Who we are", saved.getMetaDescription());
	}

	private static PageDto page(String title, String key) {
		PageDto page = new PageDto();
		page.setTitle(title);
		page.setKey(key);
		page.setContent("<p>Text</p>");
		return page;
	}

	private StaticPage savedPage() {
		ArgumentCaptor<StaticPage> captor = ArgumentCaptor.forClass(StaticPage.class);
		verify(pages).save(captor.capture());
		return captor.getValue();
	}
}
