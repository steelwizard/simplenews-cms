package simplenews.cms.service;

import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewFieldDisplayDto;
import simplenews.cms.data.dto.ReviewFieldDto;
import simplenews.cms.data.dto.ReviewTypeDto;
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

	List<INewsDto> findReviews(ReviewType type, ReviewField filterField, String filterValue, boolean byRating, int limit, int offset);

	ReviewField getRatingField(Long reviewTypeId);
}
