package simplenews.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewListPage;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.data.repository.NewsFieldValueRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.data.repository.ReviewFieldRepository;
import simplenews.cms.data.repository.ReviewTypeRepository;
import simplenews.cms.service.ICategoryService;
import simplenews.cms.service.INewsFileService;
import simplenews.cms.service.INewsService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

	private ReviewFieldRepository fields;
	private ReviewTypeRepository types;
	private NewsRepository news;
	private NewsFieldValueRepository values;
	private INewsFileService files;
	private ICategoryService categories;
	private INewsService articles;
	private ReviewService service;

	@BeforeEach
	void setUp() {
		fields = mock(ReviewFieldRepository.class);
		types = mock(ReviewTypeRepository.class);
		news = mock(NewsRepository.class);
		values = mock(NewsFieldValueRepository.class);
		files = mock(INewsFileService.class);
		categories = mock(ICategoryService.class);
		articles = mock(INewsService.class);
		service = new ReviewService();
		ReflectionTestUtils.setField(service, "fieldRepository", fields);
		ReflectionTestUtils.setField(service, "typeRepository", types);
		ReflectionTestUtils.setField(service, "newsRepository", news);
		ReflectionTestUtils.setField(service, "valueRepository", values);
		ReflectionTestUtils.setField(service, "newsFileService", files);
		ReflectionTestUtils.setField(service, "categoryService", categories);
		ReflectionTestUtils.setField(service, "newsService", articles);
		when(files.getByNewsIds(any())).thenReturn(Map.of());
		when(values.findByNewsIdIn(any())).thenReturn(List.of());
		when(categories.getAllCategories(false)).thenReturn(List.of());
	}

	// Form fields named field_12 are read as review answers, and other fields are ignored.
	@Test
	void formParametersBecomeReviewAnswers() {
		Map<Long, String> values = service.readValues(Map.of(
				"field_12", new String[] {"Jazz"},
				"title", new String[] {"Kind of Blue"},
				"field_nope", new String[] {"ignored"}
		));

		assertEquals(Map.of(12L, "Jazz"), values);
	}

	// A required review answer cannot be left blank.
	@Test
	void requiredAnswerCannotBeBlank() {
		stub(field(1L, "Artist", ReviewFieldTypeEnum.TEXT, true));

		List<String> errors = service.validateValues(7L, Map.of());

		assertEquals(List.of("Artist is required."), errors);
	}

	// An optional answer may be left blank.
	@Test
	void optionalAnswerMayBeBlank() {
		stub(field(1L, "Notes", ReviewFieldTypeEnum.TEXT, false));

		assertTrue(service.validateValues(7L, Map.of()).isEmpty());
	}

	// A score has to stay inside its minimum and maximum.
	@Test
	void ratingMustStayInsideItsRange() {
		ReviewField rating = field(2L, "Rating", ReviewFieldTypeEnum.RATING, true);
		rating.setMinValue(BigDecimal.ZERO);
		rating.setMaxValue(BigDecimal.TEN);
		stub(rating);

		List<String> errors = service.validateValues(7L, Map.of(2L, "11"));

		assertEquals(1, errors.size());
		assertTrue(errors.get(0).contains("at most 10"));
	}

	// A comma is accepted as a decimal point.
	@Test
	void commaIsAcceptedAsADecimalPoint() {
		ReviewField score = field(3L, "Score", ReviewFieldTypeEnum.PERCENT, false);
		score.setMinValue(BigDecimal.ZERO);
		score.setMaxValue(new BigDecimal("100"));
		stub(score);

		assertTrue(service.validateValues(7L, Map.of(3L, "81,5")).isEmpty());
	}

	// A review link has to start with http:// or https://.
	@Test
	void linkMustUseHttp() {
		stub(field(4L, "Website", ReviewFieldTypeEnum.URL, false));

		List<String> bad = service.validateValues(7L, Map.of(4L, "javascript:alert(1)"));
		List<String> good = service.validateValues(7L, Map.of(4L, "https://example.com"));

		assertEquals(1, bad.size());
		assertTrue(bad.get(0).contains("http://"));
		assertTrue(good.isEmpty());
	}

	// A choice answer has to be one of the listed choices.
	@Test
	void choiceMustBeOneOfTheOptions() {
		ReviewField genre = field(5L, "Genre", ReviewFieldTypeEnum.CHOICE, true);
		genre.setChoices("Jazz\nRock");
		stub(genre);

		assertTrue(service.validateValues(7L, Map.of(5L, "Pop")).get(0).contains("choices"));
		assertTrue(service.validateValues(7L, Map.of(5L, "Jazz")).isEmpty());
	}

	// A date answer has to look like a real calendar date.
	@Test
	void dateMustBeARealDate() {
		stub(field(6L, "Released", ReviewFieldTypeEnum.DATE, false));

		assertTrue(service.validateValues(7L, Map.of(6L, "2026-13-40")).get(0).contains("date"));
		assertTrue(service.validateValues(7L, Map.of(6L, "2026-10-21")).isEmpty());
	}

	// An unknown review type never reaches the article table.
	@Test
	void unknownReviewTypeIsMissing() {
		when(types.findByKey("missing")).thenReturn(Optional.empty());

		assertNull(service.listReviews("missing", null, null, null, null, 10));

		verifyNoInteractions(news);
	}

	// A field that cannot be filtered, and a rating sort with no rating field, are both ignored.
	@Test
	void unusedSortAndFilterAreDropped() {
		ReviewType albums = reviewType(3L, "albums", "Albums");
		when(types.findByKey("albums")).thenReturn(Optional.of(albums));
		ReviewField notes = field(9L, "Notes", ReviewFieldTypeEnum.TEXT, false);
		notes.setKey("notes");
		when(fields.findByReviewTypeOrderByPositionAscIdAsc(3L)).thenReturn(List.of(notes));
		when(news.findReviews(3L, -1L, -1L, "", false, 11, 0)).thenReturn(List.of());

		ReviewListPage page = service.listReviews("albums", 0, "rating", "notes", "  jazz  ", 10);

		assertEquals("Albums", page.title());
		assertFalse(page.byRating());
		assertFalse(page.filtered());
		assertEquals("/reviews/albums.html", page.newestUrl());
		assertNull(page.nextUrl());
		verify(news).findReviews(3L, -1L, -1L, "", false, 11, 0);
	}

	// A filterable field narrows the query, and one extra row means there is a next page.
	@Test
	void filterAndExtraRowBuildThePageLinks() {
		ReviewType albums = reviewType(3L, "albums", "Albums");
		when(types.findByKey("albums")).thenReturn(Optional.of(albums));
		ReviewField genre = field(5L, "Genre", ReviewFieldTypeEnum.CHOICE, false);
		genre.setKey("genre");
		genre.setFilterable(true);
		when(fields.findByReviewTypeOrderByPositionAscIdAsc(3L)).thenReturn(List.of(genre));
		when(news.findReviews(3L, -1L, 5L, "Jazz", false, 11, 10))
				.thenReturn(LongStream.rangeClosed(1, 11).mapToObj(ReviewServiceTest::article).toList());

		ReviewListPage page = service.listReviews("albums", 1, null, "genre", " Jazz ", 10);

		assertEquals("Albums: Genre Jazz", page.title());
		assertTrue(page.filtered());
		assertEquals(10, page.reviews().size());
		assertEquals("/reviews/albums.html?field=genre&value=Jazz", page.newestUrl());
		assertEquals("/reviews/albums.html?field=genre&value=Jazz", page.prevUrl());
		assertEquals("/reviews/albums.html?p=2&field=genre&value=Jazz", page.nextUrl());
	}

	// A hidden article, or one from another review type, is not published.
	@Test
	void hiddenOrForeignReviewIsNotPublished() {
		when(types.findByKey("albums")).thenReturn(Optional.of(reviewType(3L, "albums", "Albums")));
		INewsDto hidden = mock(INewsDto.class);
		when(hidden.getVisible()).thenReturn(false);
		when(articles.getNewsById(4L)).thenReturn(hidden);

		assertNull(service.getPublishedReview("albums", 4L));

		INewsDto other = mock(INewsDto.class);
		when(other.getVisible()).thenReturn(true);
		when(other.getCategory()).thenReturn("books");
		when(articles.getNewsById(5L)).thenReturn(other);
		when(categories.getCategoryByName("books")).thenReturn(null);

		assertNull(service.getPublishedReview("albums", 5L));
	}

	private static ReviewType reviewType(long id, String key, String title) {
		ReviewType type = new ReviewType();
		type.setId(id);
		type.setKey(key);
		type.setTitle(title);
		return type;
	}

	private static News article(long id) {
		Category category = new Category();
		category.setName("jazz");
		News article = new News();
		article.setId(id);
		article.setTitle("Album " + id);
		article.setKey("album-" + id);
		article.setContent("notes");
		article.setCategory(category);
		article.setCreator("editor");
		article.setCreated(new Timestamp(0));
		article.setLastModified(new Timestamp(0));
		article.setVisible(true);
		return article;
	}

	private void stub(ReviewField field) {
		when(fields.findByReviewTypeOrderByPositionAscIdAsc(7L)).thenReturn(List.of(field));
	}

	private static ReviewField field(long id, String label, ReviewFieldTypeEnum type, boolean required) {
		ReviewField field = new ReviewField();
		field.setId(id);
		field.setLabel(label);
		field.setFieldType(type);
		field.setRequired(required);
		field.setActive(true);
		return field;
	}
}
