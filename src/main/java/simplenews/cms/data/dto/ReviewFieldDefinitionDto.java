package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ReviewFieldDefinition;
import simplenews.cms.data.entity.ReviewFieldOption;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;

import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ReviewFieldDefinitionDto {

	private Long id;
	private Long categoryId;
	private String fieldKey;
	private String fieldLabel;
	private ReviewFieldTypeEnum fieldType;
	// Newline-separated list of allowed values for SELECT type
	private String options;
	private boolean required;
	private int orderIndex;

	public ReviewFieldDefinitionDto() {}

	public ReviewFieldDefinitionDto(ReviewFieldDefinition entity) {
		this.id = entity.getId();
		this.categoryId = entity.getCategory().getId();
		this.fieldKey = entity.getFieldKey();
		this.fieldLabel = entity.getFieldLabel();
		this.fieldType = entity.getFieldType();
		this.options = entity.getOptions().stream()
				.sorted(Comparator.comparingInt(ReviewFieldOption::getOrderIndex))
				.map(ReviewFieldOption::getValue)
				.collect(Collectors.joining("\n"));
		this.required = entity.isRequired();
		this.orderIndex = entity.getOrderIndex();
	}

	/** Returns the options as a String array, split by newline. */
	public String[] getOptionsArray() {
		if (options == null || options.isBlank()) return new String[0];
		return Arrays.stream(options.split("\n"))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.toArray(String[]::new);
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getCategoryId() { return categoryId; }
	public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

	public String getFieldKey() { return fieldKey; }
	public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

	public String getFieldLabel() { return fieldLabel; }
	public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

	public ReviewFieldTypeEnum getFieldType() { return fieldType; }
	public void setFieldType(ReviewFieldTypeEnum fieldType) { this.fieldType = fieldType; }

	public String getOptions() { return options; }
	public void setOptions(String options) { this.options = options; }

	public boolean isRequired() { return required; }
	public void setRequired(boolean required) { this.required = required; }

	public int getOrderIndex() { return orderIndex; }
	public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
