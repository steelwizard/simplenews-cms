package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "review_field", uniqueConstraints = @UniqueConstraint(columnNames = {"review_type", "key"}))
public class ReviewField {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "review_type", nullable = false)
	private Long reviewType;

	@Column(name = "key", nullable = false, length = 50)
	private String key;

	@Column(name = "label", nullable = false, length = 100)
	private String label;

	@Column(name = "field_type", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private ReviewFieldTypeEnum fieldType;

	@Column(name = "min_value")
	private BigDecimal minValue;

	@Column(name = "max_value")
	private BigDecimal maxValue;

	@Column(name = "choices", columnDefinition = "text")
	private String choices;

	@Column(name = "required", nullable = false)
	private boolean required;

	@Column(name = "position", nullable = false)
	private int position;

	@Column(name = "show_in_list", nullable = false)
	private boolean showInList;

	@Column(name = "filterable", nullable = false)
	private boolean filterable;

	@Column(name = "active", nullable = false)
	private boolean active = true;

	public List<String> getChoiceList() {
		if (choices == null || choices.isBlank()) {
			return List.of();
		}
		return Arrays.stream(choices.split("\\r?\\n"))
				.map(String::strip)
				.filter(s -> !s.isEmpty())
				.toList();
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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
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
