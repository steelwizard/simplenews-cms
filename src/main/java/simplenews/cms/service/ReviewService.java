package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.data.dto.ReviewFieldDisplayDto;
import simplenews.cms.data.dto.ReviewFieldDto;
import simplenews.cms.data.dto.ReviewJsonDto;
import simplenews.cms.data.dto.ReviewListPage;
import simplenews.cms.data.dto.ReviewTypeDto;
import simplenews.cms.data.dto.ReviewTypeJsonDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.entity.NewsFieldValue;
import simplenews.cms.data.entity.NewsFieldValueId;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.data.repository.NavigationItemRepository;
import simplenews.cms.data.repository.NewsFieldValueRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.data.repository.ReviewFieldRepository;
import simplenews.cms.data.repository.ReviewTypeRepository;
import simplenews.cms.service.util.SlugUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class ReviewService implements IReviewService {

	private static final Logger LOG = Logger.getLogger(ReviewService.class.getName());
	private static final String FIELD_PARAM_PREFIX = "field_";
	private static final int VALUE_MAX = 1000;

	@Autowired
	ReviewTypeRepository typeRepository;
	@Autowired
	ReviewFieldRepository fieldRepository;
	@Autowired
	NewsFieldValueRepository valueRepository;
	@Autowired
	NewsRepository newsRepository;
	@Autowired
	NavigationItemRepository navigationRepository;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	INewsService newsService;
	@Autowired
	INewsFileService newsFileService;

	@Override
	public List<ReviewType> getReviewTypes() {
		return typeRepository.findAllByOrderByTitleAsc();
	}

	@Override
	public ReviewType getReviewType(Long id) {
		if (id == null) return null;
		return typeRepository.findById(id).orElse(null);
	}

	@Override
	public ReviewType getReviewTypeByKey(String key) {
		if (key == null) return null;
		return typeRepository.findByKey(key).orElse(null);
	}

	@Override
	@CacheEvict(cacheNames = NavigationService.NAVIGATION_CACHE, allEntries = true)
	public ReviewType saveReviewType(ReviewTypeDto dto, String user) {
		String title = dto.getTitle() == null ? "" : dto.getTitle().strip();
		if (title.isEmpty()) throw new IllegalArgumentException("Title is required.");
		if (title.length() > 100) throw new IllegalArgumentException("Title is too long (max. 100 characters).");
		String key = SlugUtil.slugify(dto.getKey() == null || dto.getKey().isBlank() ? title : dto.getKey(), 50);
		if (key.isEmpty()) throw new IllegalArgumentException("Key must contain letters or digits.");
		if (key.equals("index")) throw new IllegalArgumentException("The key 'index' is reserved for /reviews/index.json.");

		ReviewType type;
		if (dto.getId() != null) {
			type = getReviewType(dto.getId());
			if (type == null) throw new IllegalArgumentException("Review type does not exist.");
		} else {
			type = new ReviewType();
			type.setCreator(user);
			type.setCreated(new Timestamp(System.currentTimeMillis()));
		}
		if (!key.equals(type.getKey()) && typeRepository.existsByKey(key)) {
			throw new IllegalArgumentException("A review type with the key '" + key + "' already exists.");
		}
		type.setKey(key);
		type.setTitle(title);
		type.setLastModified(new Timestamp(System.currentTimeMillis()));
		ReviewType saved = typeRepository.save(type);
		LOG.info(String.format("User %s saved review type %d %s", user, saved.getId(), saved.getKey()));
		return saved;
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = NavigationService.NAVIGATION_CACHE, allEntries = true)
	public void deleteReviewType(Long id) {
		ReviewType type = getReviewType(id);
		if (type == null) throw new IllegalArgumentException("Review type does not exist.");
		List<String> used = getCategoriesUsing(id);
		if (!used.isEmpty()) {
			throw new IllegalStateException("Still used by categories: " + String.join(", ", used));
		}
		valueRepository.deleteByReviewType(id);
		fieldRepository.deleteByReviewType(id);
		navigationRepository.deleteChildrenOfReviewType(id);
		navigationRepository.deleteByReviewType(id);
		typeRepository.deleteById(id);
		LOG.info("Deleted review type " + id);
	}

	@Override
	public List<String> getCategoriesUsing(Long reviewTypeId) {
		return categoryService.getAllCategories(false).stream()
				.filter(c -> Objects.equals(c.getReviewType(), reviewTypeId))
				.map(Category::getName)
				.toList();
	}

	@Override
	public List<ReviewField> getFields(Long reviewTypeId, boolean activeOnly) {
		if (reviewTypeId == null) return List.of();
		return fieldRepository.findByReviewTypeOrderByPositionAscIdAsc(reviewTypeId).stream()
				.filter(f -> !activeOnly || f.isActive())
				.toList();
	}

	@Override
	public Map<Long, List<ReviewField>> getActiveFieldsByType() {
		Map<Long, List<ReviewField>> result = new LinkedHashMap<>();
		for (ReviewType type : getReviewTypes()) {
			result.put(type.getId(), getFields(type.getId(), true));
		}
		return result;
	}

	@Override
	public ReviewField getField(Long id) {
		if (id == null) return null;
		return fieldRepository.findById(id).orElse(null);
	}

	@Override
	public ReviewField saveField(ReviewFieldDto dto) {
		if (getReviewType(dto.getReviewType()) == null) throw new IllegalArgumentException("Review type does not exist.");
		String label = dto.getLabel() == null ? "" : dto.getLabel().strip();
		if (label.isEmpty()) throw new IllegalArgumentException("Label is required.");
		if (label.length() > 100) throw new IllegalArgumentException("Label is too long (max. 100 characters).");
		if (dto.getFieldType() == null) throw new IllegalArgumentException("Choose a field type.");
		String key = SlugUtil.slugify(dto.getKey() == null || dto.getKey().isBlank() ? label : dto.getKey(), 50);
		if (key.isEmpty()) throw new IllegalArgumentException("Key must contain letters or digits.");

		ReviewField field;
		if (dto.getId() != null) {
			field = getField(dto.getId());
			if (field == null) throw new IllegalArgumentException("Field does not exist.");
			if (!field.getReviewType().equals(dto.getReviewType())) {
				throw new IllegalArgumentException("A field cannot be moved to another review type.");
			}
			if (field.getFieldType() != dto.getFieldType() && valueRepository.existsByFieldId(field.getId())) {
				throw new IllegalArgumentException("The type cannot be changed while reviews use this field. "
						+ "Archive it and create a new field instead.");
			}
		} else {
			field = new ReviewField();
			field.setReviewType(dto.getReviewType());
			field.setPosition(fieldRepository.findMaxPosition(dto.getReviewType()) + 1);
		}
		if (!key.equals(field.getKey()) && fieldRepository.existsByReviewTypeAndKey(dto.getReviewType(), key)) {
			throw new IllegalArgumentException("This review type already has a field with the key '" + key + "'.");
		}

		BigDecimal min = null;
		BigDecimal max = null;
		String choices = null;
		switch (dto.getFieldType()) {
			case RATING -> {
				min = dto.getMinValue() != null ? dto.getMinValue() : BigDecimal.ZERO;
				max = dto.getMaxValue() != null ? dto.getMaxValue() : BigDecimal.TEN;
				if (min.compareTo(max) >= 0) throw new IllegalArgumentException("Minimum must be lower than maximum.");
			}
			case PERCENT -> {
				min = dto.getMinValue() != null ? dto.getMinValue() : BigDecimal.ZERO;
				max = dto.getMaxValue() != null ? dto.getMaxValue() : new BigDecimal("100");
				if (min.compareTo(max) >= 0) throw new IllegalArgumentException("Minimum must be lower than maximum.");
			}
			case NUMBER -> {
				min = dto.getMinValue();
				max = dto.getMaxValue();
				if (min != null && max != null && min.compareTo(max) > 0) {
					throw new IllegalArgumentException("Minimum must not be higher than maximum.");
				}
			}
			case CHOICE -> {
				ReviewField probe = new ReviewField();
				probe.setChoices(dto.getChoices());
				List<String> list = probe.getChoiceList();
				if (list.isEmpty()) throw new IllegalArgumentException("Enter at least one choice, one per line.");
				choices = String.join("\n", list);
			}
			default -> { }
		}

		field.setKey(key);
		field.setLabel(label);
		field.setFieldType(dto.getFieldType());
		field.setMinValue(min);
		field.setMaxValue(max);
		field.setChoices(choices);
		field.setRequired(dto.isRequired());
		field.setShowInList(dto.isShowInList());
		field.setFilterable(dto.isFilterable());
		field.setActive(dto.isActive());
		return fieldRepository.save(field);
	}

	@Override
	@Transactional
	public void moveField(Long id, boolean up) {
		ReviewField field = getField(id);
		if (field == null) throw new IllegalArgumentException("Field does not exist.");
		List<ReviewField> fields = new ArrayList<>(fieldRepository.findByReviewTypeOrderByPositionAscIdAsc(field.getReviewType()));
		int index = -1;
		for (int i = 0; i < fields.size(); i++) {
			if (fields.get(i).getId().equals(id)) index = i;
		}
		int target = up ? index - 1 : index + 1;
		if (index < 0 || target < 0 || target >= fields.size()) return;
		fields.set(index, fields.get(target));
		fields.set(target, field);
		for (int i = 0; i < fields.size(); i++) {
			fields.get(i).setPosition(i);
		}
		fieldRepository.saveAll(fields);
	}

	@Override
	@Transactional
	public void deleteField(Long id) {
		if (getField(id) == null) throw new IllegalArgumentException("Field does not exist.");
		valueRepository.deleteByField(id);
		fieldRepository.deleteById(id);
		LOG.info("Deleted review field " + id);
	}

	@Override
	public boolean fieldHasValues(Long fieldId) {
		return valueRepository.existsByFieldId(fieldId);
	}

	@Override
	public Long getReviewTypeIdForCategory(String categoryName) {
		if (categoryName == null) return null;
		Category category = categoryService.getCategoryByName(categoryName);
		return category == null ? null : category.getReviewType();
	}

	@Override
	public Map<Long, String> getRawValues(Long newsId) {
		Map<Long, String> values = new HashMap<>();
		if (newsId == null) return values;
		valueRepository.findByNewsId(newsId).forEach(v -> values.put(v.getFieldId(), v.getValueText()));
		return values;
	}

	@Override
	public Map<Long, String> readValues(Map<String, String[]> parameters) {
		Map<Long, String> values = new HashMap<>();
		parameters.forEach((name, raw) -> {
			if (!name.startsWith(FIELD_PARAM_PREFIX) || raw == null || raw.length == 0) return;
			try {
				values.put(Long.parseLong(name.substring(FIELD_PARAM_PREFIX.length())), raw[0]);
			} catch (NumberFormatException ignored) {
				// not one of ours
			}
		});
		return values;
	}

	@Override
	public List<String> validateValues(Long reviewTypeId, Map<Long, String> rawValues) {
		List<String> errors = new ArrayList<>();
		for (ReviewField field : getFields(reviewTypeId, true)) {
			String raw = clean(rawValues.get(field.getId()));
			if (raw.isEmpty()) {
				if (field.isRequired()) errors.add(field.getLabel() + " is required.");
				continue;
			}
			try {
				normalize(field, raw);
			} catch (IllegalArgumentException e) {
				errors.add(field.getLabel() + ": " + e.getMessage());
			}
		}
		return errors;
	}

	@Override
	@Transactional
	public void saveValues(Long newsId, Long reviewTypeId, Map<Long, String> rawValues) {
		if (newsId == null || reviewTypeId == null) return;
		for (ReviewField field : getFields(reviewTypeId, true)) {
			NewsFieldValueId id = new NewsFieldValueId(newsId, field.getId());
			String raw = clean(rawValues.get(field.getId()));
			if (raw.isEmpty()) {
				if (valueRepository.existsById(id)) valueRepository.deleteById(id);
				continue;
			}
			Normalized normalized = normalize(field, raw);
			NewsFieldValue value = valueRepository.findById(id).orElseGet(NewsFieldValue::new);
			value.setNewsId(newsId);
			value.setFieldId(field.getId());
			value.setValueText(normalized.text);
			value.setValueNumber(normalized.number);
			valueRepository.save(value);
		}
	}

	@Override
	public Map<Long, List<ReviewFieldDisplayDto>> getDisplayValues(List<INewsDto> news, boolean listOnly) {
		Map<Long, List<ReviewFieldDisplayDto>> result = new HashMap<>();
		if (news == null || news.isEmpty()) return result;

		Map<String, Long> typeByCategory = new HashMap<>();
		categoryService.getAllCategories(false).forEach(c -> {
			if (c.getReviewType() != null) typeByCategory.put(c.getName(), c.getReviewType());
		});
		List<INewsDto> reviews = news.stream()
				.filter(n -> n.getId() != null && typeByCategory.containsKey(n.getCategory()))
				.toList();
		if (reviews.isEmpty()) return result;

		Set<Long> ids = reviews.stream().map(INewsDto::getId).collect(Collectors.toSet());
		Map<Long, Map<Long, String>> valuesByNews = new HashMap<>();
		valueRepository.findByNewsIdIn(ids).forEach(v ->
				valuesByNews.computeIfAbsent(v.getNewsId(), k -> new HashMap<>()).put(v.getFieldId(), v.getValueText()));

		Map<Long, List<ReviewField>> fieldsByType = new HashMap<>();
		Map<Long, ReviewType> types = new HashMap<>();
		for (INewsDto item : reviews) {
			Long typeId = typeByCategory.get(item.getCategory());
			ReviewType type = types.computeIfAbsent(typeId, this::getReviewType);
			if (type == null) continue;
			List<ReviewField> fields = fieldsByType.computeIfAbsent(typeId, t -> getFields(t, true));
			Map<Long, String> values = valuesByNews.getOrDefault(item.getId(), Map.of());
			List<ReviewFieldDisplayDto> display = new ArrayList<>();
			for (ReviewField field : fields) {
				if (listOnly && !field.isShowInList()) continue;
				String value = values.get(field.getId());
				if (value == null || value.isBlank()) continue;
				display.add(toDisplay(type, field, value));
			}
			if (!display.isEmpty()) result.put(item.getId(), display);
		}
		return result;
	}

	@Override
	public List<ReviewJsonDto> toJson(List<INewsDto> news) {
		List<ReviewJsonDto> result = new ArrayList<>();
		if (news == null || news.isEmpty()) return result;

		Map<String, Long> typeByCategory = new HashMap<>();
		categoryService.getAllCategories(false).forEach(c -> {
			if (c.getReviewType() != null) typeByCategory.put(c.getName(), c.getReviewType());
		});
		Set<Long> ids = news.stream().map(INewsDto::getId).collect(Collectors.toSet());
		Map<Long, Map<Long, NewsFieldValue>> valuesByNews = new HashMap<>();
		valueRepository.findByNewsIdIn(ids).forEach(v ->
				valuesByNews.computeIfAbsent(v.getNewsId(), k -> new HashMap<>()).put(v.getFieldId(), v));

		Map<Long, ReviewType> types = new HashMap<>();
		Map<Long, List<ReviewField>> fieldsByType = new HashMap<>();
		for (INewsDto item : news) {
			Long typeId = typeByCategory.get(item.getCategory());
			ReviewType type = typeId == null ? null : types.computeIfAbsent(typeId, this::getReviewType);
			Map<String, Object> fields = new LinkedHashMap<>();
			if (type != null) {
				Map<Long, NewsFieldValue> values = valuesByNews.getOrDefault(item.getId(), Map.of());
				for (ReviewField field : fieldsByType.computeIfAbsent(typeId, t -> getFields(t, true))) {
					NewsFieldValue value = values.get(field.getId());
					if (value == null || value.getValueText() == null) continue;
					boolean numeric = field.getFieldType() == ReviewFieldTypeEnum.NUMBER
							|| field.getFieldType() == ReviewFieldTypeEnum.RATING
							|| field.getFieldType() == ReviewFieldTypeEnum.PERCENT;
					fields.put(field.getKey(), numeric && value.getValueNumber() != null
							? value.getValueNumber() : value.getValueText());
				}
			}
			result.add(new ReviewJsonDto(item, type != null ? type.getKey() : null, fields));
		}
		return result;
	}

	@Override
	public ReviewListPage listReviews(String typeKey, Integer page, String sort, String fieldKey, String value, int limit) {
		ListedReviews listed = list(typeKey, page, sort, fieldKey, value, limit);
		if (listed == null) return null;
		String sortParam = listed.byRating() ? "rating" : null;
		String filterKey = listed.filterField() != null ? listed.filterField().getKey() : null;
		String title = listed.type().getTitle();
		if (listed.filterField() != null) {
			title += ": " + listed.filterField().getLabel() + " " + listed.filterValue();
		}
		return new ReviewListPage(
				listed.type(),
				title,
				listed.reviews(),
				getDisplayValues(listed.reviews(), true),
				listed.hasRating(),
				listed.byRating(),
				listed.filterField() != null,
				listUrl(listed.type(), 0, null, filterKey, listed.filterValue()),
				listUrl(listed.type(), 0, "rating", filterKey, listed.filterValue()),
				listUrl(listed.type(), 0, sortParam, null, null),
				listed.page() > 0 ? listUrl(listed.type(), listed.page() - 1, sortParam, filterKey, listed.filterValue()) : null,
				listed.hasNextPage() ? listUrl(listed.type(), listed.page() + 1, sortParam, filterKey, listed.filterValue()) : null,
				listed.page());
	}

	@Override
	public List<ReviewJsonDto> listReviewsJson(String typeKey, Integer page, String sort, String fieldKey, String value, int limit) {
		ListedReviews listed = list(typeKey, page, sort, fieldKey, value, limit);
		return listed == null ? null : toJson(listed.reviews());
	}

	@Override
	public ReviewJsonDto getPublishedReview(String typeKey, Long newsId) {
		ReviewType type = getReviewTypeByKey(typeKey);
		if (type == null || newsId == null) return null;
		INewsDto news = newsService.getNewsById(newsId);
		if (news == null || !Boolean.TRUE.equals(news.getVisible())
				|| !Objects.equals(getReviewTypeIdForCategory(news.getCategory()), type.getId())) {
			return null;
		}
		return toJson(List.of(news)).get(0);
	}

	@Override
	public List<ReviewTypeJsonDto> listReviewTypesJson() {
		return getReviewTypes().stream()
				.map(type -> new ReviewTypeJsonDto(type, getFields(type.getId(), true)))
				.toList();
	}

	private ListedReviews list(String typeKey, Integer p, String sort, String fieldKey, String value, int limit) {
		ReviewType type = getReviewTypeByKey(typeKey);
		if (type == null) return null;
		int page = p != null && p > 0 ? p : 0;
		boolean hasRating = getRatingField(type.getId()) != null;
		boolean byRating = "rating".equals(sort) && hasRating;
		ReviewField filterField = null;
		String filterValue = null;
		if (fieldKey != null && value != null && !value.isBlank()) {
			filterField = getFields(type.getId(), true).stream()
					.filter(f -> f.isFilterable() && f.getKey().equals(fieldKey))
					.findFirst().orElse(null);
			if (filterField != null) filterValue = value.strip();
		}
		List<INewsDto> reviews = findReviews(type, filterField, filterValue, byRating, limit + 1, limit * page);
		boolean hasNextPage = reviews.size() > limit;
		if (hasNextPage) reviews = reviews.subList(0, limit);
		return new ListedReviews(type, page, byRating, hasRating, filterField, filterValue, reviews, hasNextPage);
	}

	private List<INewsDto> findReviews(ReviewType type, ReviewField filterField, String filterValue,
									   boolean byRating, int limit, int offset) {
		ReviewField rating = getRatingField(type.getId());
		long ratingId = rating != null ? rating.getId() : -1L;
		boolean useFilter = filterField != null && filterValue != null && !filterValue.isBlank();
		long filterId = useFilter ? filterField.getId() : -1L;
		String value = useFilter ? filterValue.strip() : "";
		List<News> news = newsRepository.findReviews(type.getId(), ratingId, filterId, value, byRating && rating != null, limit, offset);
		var files = newsFileService.getByNewsIds(news.stream().map(News::getId).toList());
		return news.stream().map(n -> {
			NewsDto dto = new NewsDto(n);
			dto.setFiles(files.getOrDefault(n.getId(), List.of()));
			return (INewsDto) dto;
		}).toList();
	}

	private static String listUrl(ReviewType type, int page, String sort, String field, String value) {
		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/reviews/" + type.getKey() + ".html");
		if (page > 0) builder.queryParam("p", page);
		if (sort != null) builder.queryParam("sort", sort);
		if (field != null) builder.queryParam("field", field).queryParam("value", value);
		return builder.encode().build().toUriString();
	}

	private record ListedReviews(ReviewType type, int page, boolean byRating, boolean hasRating,
								 ReviewField filterField, String filterValue,
								 List<INewsDto> reviews, boolean hasNextPage) {}

	@Override
	public ReviewField getRatingField(Long reviewTypeId) {
		return getFields(reviewTypeId, true).stream()
				.filter(f -> f.getFieldType() == ReviewFieldTypeEnum.RATING)
				.findFirst().orElse(null);
	}

	private ReviewFieldDisplayDto toDisplay(ReviewType type, ReviewField field, String value) {
		String shown = value;
		Integer barPercent = null;
		if (field.getFieldType() == ReviewFieldTypeEnum.RATING && field.getMaxValue() != null) {
			shown = value + " / " + field.getMaxValue().stripTrailingZeros().toPlainString();
		}
		if (field.getFieldType() == ReviewFieldTypeEnum.PERCENT) {
			shown = value + "%";
			barPercent = barPercent(field, value);
		}
		if (field.getFieldType() == ReviewFieldTypeEnum.URL) {
			return new ReviewFieldDisplayDto(field.getLabel(), shown, value, true, barPercent);
		}
		if (field.isFilterable()) {
			String href = "/reviews/" + type.getKey() + ".html?field=" + field.getKey()
					+ "&value=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
			return new ReviewFieldDisplayDto(field.getLabel(), shown, href, false, barPercent);
		}
		return new ReviewFieldDisplayDto(field.getLabel(), shown, null, false, barPercent);
	}

	private Integer barPercent(ReviewField field, String value) {
		BigDecimal number;
		try {
			number = new BigDecimal(value.replace(',', '.'));
		} catch (NumberFormatException e) {
			return 0;
		}
		BigDecimal min = field.getMinValue() != null ? field.getMinValue() : BigDecimal.ZERO;
		BigDecimal max = field.getMaxValue() != null ? field.getMaxValue() : new BigDecimal("100");
		BigDecimal range = max.subtract(min);
		if (range.compareTo(BigDecimal.ZERO) <= 0) return 0;
		int pct = number.subtract(min)
				.divide(range, 4, RoundingMode.HALF_UP)
				.multiply(new BigDecimal("100"))
				.setScale(0, RoundingMode.HALF_UP)
				.intValue();
		return Math.max(0, Math.min(100, pct));
	}

	private Normalized normalize(ReviewField field, String raw) {
		if (raw.length() > VALUE_MAX) throw new IllegalArgumentException("is too long (max. " + VALUE_MAX + " characters).");
		switch (field.getFieldType()) {
			case NUMBER, RATING, PERCENT -> {
				BigDecimal number;
				try {
					number = new BigDecimal(raw.replace(',', '.'));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("must be a number.");
				}
				if (field.getMinValue() != null && number.compareTo(field.getMinValue()) < 0) {
					throw new IllegalArgumentException("must be at least " + field.getMinValue().stripTrailingZeros().toPlainString() + ".");
				}
				if (field.getMaxValue() != null && number.compareTo(field.getMaxValue()) > 0) {
					throw new IllegalArgumentException("must be at most " + field.getMaxValue().stripTrailingZeros().toPlainString() + ".");
				}
				return new Normalized(number.stripTrailingZeros().toPlainString(), number);
			}
			case DATE -> {
				try {
					return new Normalized(LocalDate.parse(raw).toString(), null);
				} catch (DateTimeParseException e) {
					throw new IllegalArgumentException("must be a date like 2026-10-21.");
				}
			}
			case URL -> {
				try {
					URI uri = URI.create(raw);
					String scheme = uri.getScheme();
					if (uri.getHost() == null || scheme == null
							|| !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
						throw new IllegalArgumentException();
					}
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("must be an address starting with http:// or https://.");
				}
				return new Normalized(raw, null);
			}
			case CHOICE -> {
				if (!field.getChoiceList().contains(raw)) throw new IllegalArgumentException("is not one of the choices.");
				return new Normalized(raw, null);
			}
			default -> {
				return new Normalized(raw, null);
			}
		}
	}

	private static String clean(String raw) {
		return raw == null ? "" : raw.strip();
	}

	private record Normalized(String text, BigDecimal number) {}
}
