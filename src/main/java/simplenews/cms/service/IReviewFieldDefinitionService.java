package simplenews.cms.service;

import simplenews.cms.data.dto.ReviewFieldDefinitionDto;

import java.util.List;

public interface IReviewFieldDefinitionService {

	List<ReviewFieldDefinitionDto> getByCategory(Long categoryId);

	ReviewFieldDefinitionDto getById(Long id);

	ReviewFieldDefinitionDto save(ReviewFieldDefinitionDto dto);

	void delete(Long id);
}
