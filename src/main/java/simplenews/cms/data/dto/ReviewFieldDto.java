package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;

import java.math.BigDecimal;

public class ReviewFieldDto {
	private Long id;
	private Long reviewType;
	private String key;
	private String label;
	private ReviewFieldTypeEnum fieldType = ReviewFieldTypeEnum.TEXT;
	private BigDecimal minValue;
	private BigDecimal maxValue;
	private String choices;
	private boolean required;
	private boolean showInList;
	private boolean filterable;
	private boolean active = true;

	public ReviewFieldDto() {}

	public ReviewFieldDto(ReviewField field) {
		this.id = field.getId();
		this.reviewType = field.getReviewType();
		this.key = field.getKey();
		this.label = field.getLabel();
		this.fieldType = field.getFieldType();
		this.minValue = field.getMinValue();
		this.maxValue = field.getMaxValue();
		this.choices = field.getChoices();
		this.required = field.isRequired();
		this.showInList = field.isShowInList();
		this.filterable = field.isFilterable();
		this.active = field.isActive();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getReviewType() {
		return reviewType;
	}

	public void setReviewType(Long reviewType) {
		this.reviewType = reviewType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public ReviewFieldTypeEnum getFieldType() {
		return fieldType;
	}

	public void setFieldType(ReviewFieldTypeEnum fieldType) {
		this.fieldType = fieldType;
	}

	public BigDecimal getMinValue() {
		return minValue;
	}

	public void setMinValue(BigDecimal minValue) {
		this.minValue = minValue;
	}

	public BigDecimal getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(BigDecimal maxValue) {
		this.maxValue = maxValue;
	}

	public String getChoices() {
		return choices;
	}

	public void setChoices(String choices) {
		this.choices = choices;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isShowInList() {
		return showInList;
	}

	public void setShowInList(boolean showInList) {
		this.showInList = showInList;
	}

	public boolean isFilterable() {
		return filterable;
	}

	public void setFilterable(boolean filterable) {
		this.filterable = filterable;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
