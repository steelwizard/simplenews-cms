package simplenews.cms.service;

import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewFieldDisplayDto;
import simplenews.cms.data.dto.ReviewFieldDto;
import simplenews.cms.data.dto.ReviewJsonDto;
import simplenews.cms.data.dto.ReviewListPage;
import simplenews.cms.data.dto.ReviewTypeDto;
import simplenews.cms.data.dto.ReviewTypeJsonDto;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewType;

import java.util.List;
import java.util.Map;

public interface IReviewService {
	List<ReviewType> getReviewTypes();

	ReviewType getReviewType(Long id);

	ReviewType getReviewTypeByKey(String key);

	ReviewType saveReviewType(ReviewTypeDto dto, String user);

	void deleteReviewType(Long id);

	List<String> getCategoriesUsing(Long reviewTypeId);

	List<ReviewField> getFields(Long reviewTypeId, boolean activeOnly);

	Map<Long, List<ReviewField>> getActiveFieldsByType();

	ReviewField getField(Long id);

	ReviewField saveField(ReviewFieldDto dto);

	void moveField(Long id, boolean up);

	void deleteField(Long id);

	boolean fieldHasValues(Long fieldId);

	Long getReviewTypeIdForCategory(String categoryName);

	Map<Long, String> getRawValues(Long newsId);

	Map<Long, String> readValues(Map<String, String[]> parameters);

	List<String> validateValues(Long reviewTypeId, Map<Long, String> rawValues);

	void saveValues(Long newsId, Long reviewTypeId, Map<Long, String> rawValues);

	Map<Long, List<ReviewFieldDisplayDto>> getDisplayValues(List<INewsDto> news, boolean listOnly);

	List<ReviewJsonDto> toJson(List<INewsDto> news);

	/**
	 * One page of published reviews for the type with this key.
	 * Returns null when no such type exists. A sort or filter that does not apply is ignored.
	 */
	ReviewListPage listReviews(String typeKey, Integer page, String sort, String fieldKey, String value, int limit);

	/** The same page as {@link #listReviews}, as JSON. Null when the type does not exist. */
	List<ReviewJsonDto> listReviewsJson(String typeKey, Integer page, String sort, String fieldKey, String value, int limit);

	/** One published review, or null when it is missing, hidden, or belongs to another type. */
	ReviewJsonDto getPublishedReview(String typeKey, Long newsId);

	List<ReviewTypeJsonDto> listReviewTypesJson();

	ReviewField getRatingField(Long reviewTypeId);
}
