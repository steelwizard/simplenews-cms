package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "news_field_value")
@IdClass(NewsFieldValueId.class)
public class NewsFieldValue {
	@Id
	@Column(name = "news_id")
	private Long newsId;

	@Id
	@Column(name = "field_id")
	private Long fieldId;

	@Column(name = "value_text", length = 1000)
	private String valueText;

	@Column(name = "value_number")
	private BigDecimal valueNumber;

	public Long getNewsId() {
		return newsId;
	}

	public void setNewsId(Long newsId) {
		this.newsId = newsId;
	}

	public Long getFieldId() {
		return fieldId;
	}

	public void setFieldId(Long fieldId) {
		this.fieldId = fieldId;
	}

	public String getValueText() {
		return valueText;
	}

	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	public BigDecimal getValueNumber() {
		return valueNumber;
	}

	public void setValueNumber(BigDecimal valueNumber) {
		this.valueNumber = valueNumber;
	}
}
