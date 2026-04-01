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
@Table(name = "review_field_value")
public class ReviewFieldValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "review_content_id", nullable = false)
	private ReviewContent reviewContent;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "field_definition_id", nullable = false)
	private ReviewFieldDefinition fieldDefinition;

	@Column(name = "value")
	private String value;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public ReviewContent getReviewContent() { return reviewContent; }
	public void setReviewContent(ReviewContent reviewContent) { this.reviewContent = reviewContent; }

	public ReviewFieldDefinition getFieldDefinition() { return fieldDefinition; }
	public void setFieldDefinition(ReviewFieldDefinition fieldDefinition) { this.fieldDefinition = fieldDefinition; }

	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
}
