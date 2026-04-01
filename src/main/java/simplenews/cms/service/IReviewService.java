package simplenews.cms.service;

import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.ReviewContentDto;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import java.util.List;

public interface IReviewService {
	List<INewsDto> getReviews(int limit, int offset);
	List<INewsDto> getAllReviewsAdmin(int limit, int offset);
	INewsDto getReviewById(Long id);
	List<INewsDto> getReviewsByCategoryName(String name, int limit, int offset);
	List<INewsDto> getReviewsByCategoryIds(List<Long> categoryIds, int limit, int offset);
	List<INewsDto> getReviewsByCreatorAndCategories(String creator, List<Long> categoryIds, int limit, int offset);
	INewsDto createReview(ReviewContentDto dto, String user) throws InsufficientPermissionException, DuplicateKeyException;
	INewsDto updateReview(Long id, ReviewContentDto dto, String user) throws InsufficientPermissionException, DuplicateKeyException;
	void deleteReview(Long id) throws InsufficientPermissionException;
}
