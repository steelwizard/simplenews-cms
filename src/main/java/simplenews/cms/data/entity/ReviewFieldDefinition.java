package simplenews.cms.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_field_definition")
public class ReviewFieldDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(name = "field_key", nullable = false)
	private String fieldKey;

	@Column(name = "field_label", nullable = false)
	private String fieldLabel;

	@Column(name = "field_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private ReviewFieldTypeEnum fieldType;

	// For SELECT type: allowed values stored in review_field_option
	@OneToMany(mappedBy = "fieldDefinition", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@OrderBy("orderIndex ASC")
	private List<ReviewFieldOption> options = new ArrayList<>();

	@Column(name = "required", nullable = false)
	private boolean required;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Category getCategory() { return category; }
	public void setCategory(Category category) { this.category = category; }

	public String getFieldKey() { return fieldKey; }
	public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

	public String getFieldLabel() { return fieldLabel; }
	public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

	public ReviewFieldTypeEnum getFieldType() { return fieldType; }
	public void setFieldType(ReviewFieldTypeEnum fieldType) { this.fieldType = fieldType; }

	public List<ReviewFieldOption> getOptions() { return options; }
	public void setOptions(List<ReviewFieldOption> options) { this.options = options; }

	public boolean isRequired() { return required; }
	public void setRequired(boolean required) { this.required = required; }

	public int getOrderIndex() { return orderIndex; }
	public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
