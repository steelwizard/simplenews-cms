package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.dto.ReviewFieldDefinitionDto;
import simplenews.cms.data.entity.ReviewFieldDefinition;
import simplenews.cms.data.entity.ReviewFieldOption;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.ReviewFieldDefinitionRepository;

import java.util.List;

@Service
public class ReviewFieldDefinitionService implements IReviewFieldDefinitionService {

	@Autowired
	private ReviewFieldDefinitionRepository repository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Override
	public ReviewFieldDefinitionDto getById(Long id) {
		return repository.findById(id).map(ReviewFieldDefinitionDto::new).orElse(null);
	}

	@Override
	public List<ReviewFieldDefinitionDto> getByCategory(Long categoryId) {
		return repository.findByCategoryIdOrderByOrderIndex(categoryId)
				.stream()
				.map(ReviewFieldDefinitionDto::new)
				.toList();
	}

	@Override
	@Transactional
	public ReviewFieldDefinitionDto save(ReviewFieldDefinitionDto dto) {
		ReviewFieldDefinition entity = dto.getId() != null
				? repository.findById(dto.getId()).orElse(new ReviewFieldDefinition())
				: new ReviewFieldDefinition();
		entity.setCategory(categoryRepository.getReferenceById(dto.getCategoryId()));
		entity.setFieldKey(dto.getFieldKey());
		entity.setFieldLabel(dto.getFieldLabel());
		entity.setFieldType(dto.getFieldType());
		entity.setRequired(dto.isRequired());
		entity.setOrderIndex(dto.getOrderIndex());

		entity.getOptions().clear();
		if (dto.getOptions() != null && !dto.getOptions().isBlank()) {
			int idx = 0;
			for (String line : dto.getOptions().split("\n")) {
				String trimmed = line.trim();
				if (!trimmed.isEmpty()) {
					ReviewFieldOption opt = new ReviewFieldOption();
					opt.setFieldDefinition(entity);
					opt.setValue(trimmed);
					opt.setOrderIndex(idx++);
					entity.getOptions().add(opt);
				}
			}
		}
		return new ReviewFieldDefinitionDto(repository.save(entity));
	}

	@Override
	public void delete(Long id) {
		repository.deleteById(id);
	}
}
