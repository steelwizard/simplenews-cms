package simplenews.cms.data.entity;

import java.io.Serializable;
import java.util.Objects;

public class NewsFieldValueId implements Serializable {
	private Long newsId;
	private Long fieldId;

	public NewsFieldValueId() {}

	public NewsFieldValueId(Long newsId, Long fieldId) {
		this.newsId = newsId;
		this.fieldId = fieldId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NewsFieldValueId other)) return false;
		return Objects.equals(newsId, other.newsId) && Objects.equals(fieldId, other.fieldId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(newsId, fieldId);
	}
}
