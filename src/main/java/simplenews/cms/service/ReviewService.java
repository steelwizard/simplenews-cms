package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewContentDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.ReviewContent;
import simplenews.cms.data.entity.ReviewFieldValue;
import simplenews.cms.data.repository.ContentRepository;
import simplenews.cms.data.repository.ReviewFieldDefinitionRepository;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;
import simplenews.cms.service.util.SanitizeUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ReviewService implements IReviewService {

	private static final Logger LOG = Logger.getLogger(ReviewService.class.getName());

	@Autowired
	ContentRepository contentRepository;
	@Autowired
	IUserService userService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	IUserCategoryService userCategoryService;
	@Autowired
	ReviewFieldDefinitionRepository fieldDefinitionRepository;

	@Override
	@Cacheable("reviewsLatest")
	public List<INewsDto> getReviews(int limit, int offset) {
		List<ReviewContent> reviews = contentRepository.findAllVisibleReviews(limit, offset);
		List<INewsDto> result = new ArrayList<>();
		reviews.forEach(r -> result.add(new ReviewContentDto(r)));
		return result;
	}

	@Override
	public List<INewsDto> getAllReviewsAdmin(int limit, int offset) {
		List<ReviewContent> reviews = contentRepository.findAllReviewsAdmin(limit, offset);
		List<INewsDto> result = new ArrayList<>();
		reviews.forEach(r -> result.add(new ReviewContentDto(r)));
		return result;
	}

	@Override
	@Cacheable("reviews")
	public INewsDto getReviewById(Long id) {
		Optional<ReviewContent> optional = contentRepository.findById(id)
				.filter(c -> c instanceof ReviewContent)
				.map(c -> (ReviewContent) c);
		return optional.map(ReviewContentDto::new).orElse(null);
	}

	@Override
	@Cacheable("reviewsLatest")
	public List<INewsDto> getReviewsByCategoryName(String name, int limit, int offset) {
		var category = categoryService.getCategoryByName(name);
		List<INewsDto> result = new ArrayList<>();
		if (category != null) {
			contentRepository.findReviewsByCategoryVisible(category.getId(), limit, offset)
					.forEach(r -> result.add(new ReviewContentDto(r)));
		}
		return result;
	}

	@Override
	@Cacheable("reviewsLatest")
	public List<INewsDto> getReviewsByCategoryIds(List<Long> categoryIds, int limit, int offset) {
		List<INewsDto> result = new ArrayList<>();
		if (!categoryIds.isEmpty()) {
			contentRepository.findReviewsByCategoryIdsAdmin(new HashSet<>(categoryIds), limit, offset)
					.forEach(r -> result.add(new ReviewContentDto(r)));
		}
		return result;
	}

	@Override
	public List<INewsDto> getReviewsByCreatorAndCategories(String creator, List<Long> categoryIds, int limit, int offset) {
		List<INewsDto> result = new ArrayList<>();
		if (!categoryIds.isEmpty()) {
			contentRepository.findReviewsByCreatorAndCategoryIds(creator, new HashSet<>(categoryIds), limit, offset)
					.forEach(r -> result.add(new ReviewContentDto(r)));
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {"reviews", "reviewsLatest"}, allEntries = true)
	public INewsDto createReview(ReviewContentDto dto, String user) throws InsufficientPermissionException, DuplicateKeyException {
		if (userService.getByUsername(user) == null) throw new IllegalArgumentException("No user to create review");
		if (!permissionService.canCreate(dto)) throw new InsufficientPermissionException("Unauthorized review creation by " + user);

		processKey(dto);

		Category category = categoryService.getCategoryByName(dto.getCategory());
		ReviewContent entity = new ReviewContent();
		entity.setTitle(dto.getTitle());
		entity.setCategory(category);
		entity.setCreator(user);
		entity.setLastModifiedBy(user);
		entity.setContent(SanitizeUtil.sanitize(dto.getContent()));
		entity.setKey(dto.getKey());
		entity.setVisible(dto.getVisible());
		entity.setScore(dto.getScore());
		entity.setMetaKeywords(dto.getMetaKeywords());
		entity.setMetaDescription(dto.getMetaDescription());
		Timestamp now = new Timestamp(System.currentTimeMillis());
		entity.setCreated(now);
		entity.setLastModified(now);

		if (dto.getCustomFields() != null && category != null) {
			applyFieldValues(entity, dto.getCustomFields(), category.getId());
		}

		ReviewContent saved = (ReviewContent) contentRepository.save(entity);
		LOG.info(String.format("User %s created review %d %s: %s", user, saved.getId(), saved.getKey(), saved.getTitle()));
		return new ReviewContentDto(saved);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {"reviews", "reviewsLatest"}, allEntries = true)
	public INewsDto updateReview(Long id, ReviewContentDto dto, String user) throws InsufficientPermissionException, DuplicateKeyException {
		if (userService.getByUsername(user) == null) throw new IllegalArgumentException("No user to update review");
		if (!permissionService.canEdit(dto)) throw new InsufficientPermissionException("Unauthorized review edit by " + user);

		ReviewContent entity = (ReviewContent) contentRepository.findById(id)
				.filter(c -> c instanceof ReviewContent)
				.map(c -> (ReviewContent) c)
				.orElseThrow(() -> new IllegalArgumentException("Review not found: " + id));

		if (!dto.getKey().equals(entity.getKey()) && contentRepository.existsReviewByKey(dto.getKey())) {
			processKey(dto);
			throw new DuplicateKeyException("key already exists");
		}

		Category category = categoryService.getCategoryByName(dto.getCategory());
		entity.setTitle(dto.getTitle());
		entity.setCategory(category);
		entity.setLastModifiedBy(user);
		entity.setContent(SanitizeUtil.sanitize(dto.getContent()));
		entity.setKey(dto.getKey());
		entity.setVisible(dto.getVisible());
		entity.setScore(dto.getScore());
		entity.setMetaKeywords(dto.getMetaKeywords());
		entity.setMetaDescription(dto.getMetaDescription());
		entity.setLastModified(new Timestamp(System.currentTimeMillis()));

		entity.getFieldValues().clear();
		if (dto.getCustomFields() != null && category != null) {
			applyFieldValues(entity, dto.getCustomFields(), category.getId());
		}

		ReviewContent updated = (ReviewContent) contentRepository.save(entity);
		LOG.info(String.format("User %s updated review %d %s: %s", user, updated.getId(), updated.getKey(), updated.getTitle()));
		return new ReviewContentDto(updated);
	}

	@Override
	@CacheEvict(cacheNames = {"reviews", "reviewsLatest"}, allEntries = true)
	public void deleteReview(Long id) throws InsufficientPermissionException {
		INewsDto dto = getReviewById(id);
		if (dto == null) throw new IllegalArgumentException("Review not found: " + id);
		if (!permissionService.canDelete(dto)) {
			throw new InsufficientPermissionException("Unauthorized review deletion by " + userService.getCurrentUserName());
		}
		contentRepository.deleteById(id);
		LOG.info("deleted review " + id);
	}

	private void applyFieldValues(ReviewContent entity, Map<String, String> customFields, Long categoryId) {
		for (Map.Entry<String, String> entry : customFields.entrySet()) {
			fieldDefinitionRepository.findByCategoryIdAndFieldKey(categoryId, entry.getKey())
					.ifPresent(def -> {
						ReviewFieldValue fv = new ReviewFieldValue();
						fv.setReviewContent(entity);
						fv.setFieldDefinition(def);
						fv.setValue(entry.getValue());
						entity.getFieldValues().add(fv);
					});
		}
	}

	private void processKey(ReviewContentDto dto) {
		if (dto.getKey() == null || dto.getKey().isBlank()) {
			StringBuilder sb = new StringBuilder();
			if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
				String trimmed = dto.getTitle().strip().toLowerCase().replaceAll("\\s+", "-");
				if (trimmed.length() > 15) trimmed = trimmed.substring(0, 14);
				sb.append(trimmed);
			}
			if (!sb.toString().endsWith("-")) sb.append("-");
			sb.append(System.currentTimeMillis());
			dto.setKey(sb.toString());
		} else {
			dto.setKey(dto.getKey().strip().toLowerCase().replaceAll("\\s+", "-"));
		}
		if (contentRepository.existsReviewByKey(dto.getKey())) {
			LOG.log(Level.FINE, "review key " + dto.getKey() + " already exists");
			dto.setKey(dto.getKey() + "-" + System.currentTimeMillis());
		}
	}
}
