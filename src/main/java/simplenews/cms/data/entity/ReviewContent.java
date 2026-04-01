package simplenews.cms.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_content")
@DiscriminatorValue("ReviewContent")
public class ReviewContent extends Content {

	@Column(name = "score", precision = 4, scale = 2)
	private BigDecimal score;

	@OneToMany(mappedBy = "reviewContent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<ReviewFieldValue> fieldValues = new ArrayList<>();

	public BigDecimal getScore() { return score; }
	public void setScore(BigDecimal score) { this.score = score; }

	public List<ReviewFieldValue> getFieldValues() { return fieldValues; }
	public void setFieldValues(List<ReviewFieldValue> fieldValues) { this.fieldValues = fieldValues; }
}
