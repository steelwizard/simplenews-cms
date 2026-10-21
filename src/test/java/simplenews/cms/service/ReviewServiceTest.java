package simplenews.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.repository.ReviewFieldRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

	private ReviewFieldRepository fields;
	private ReviewService service;

	@BeforeEach
	void setUp() {
		fields = mock(ReviewFieldRepository.class);
		service = new ReviewService();
		ReflectionTestUtils.setField(service, "fieldRepository", fields);
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
