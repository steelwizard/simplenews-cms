package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_field_option")
public class ReviewFieldOption {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "field_definition_id", nullable = false)
	private ReviewFieldDefinition fieldDefinition;

	@Column(name = "value", nullable = false)
	private String value;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public ReviewFieldDefinition getFieldDefinition() { return fieldDefinition; }
	public void setFieldDefinition(ReviewFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }

	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }

	public int getOrderIndex() { return orderIndex; }
	public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
