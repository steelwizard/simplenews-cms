package simplenews.cms.data.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * A review as returned by the JSON endpoints: the article fields plus its review values,
 * keyed by field key. NUMBER, RATING and PERCENT values are numbers, everything else is text.
 */
public class ReviewJsonDto {
	private final Long id;
	private final String title;
	private final String key;
	private final String content;
	private final String category;
	private final String reviewType;
	private final String creator;
	private final String lastModifiedBy;
	private final LocalDateTime created;
	private final LocalDateTime lastModified;
	private final String metaKeywords;
	private final String metaDescription;
	private final Map<String, Object> fields;

	public ReviewJsonDto(INewsDto news, String reviewType, Map<String, Object> fields) {
		this.id = news.getId();
		this.title = news.getTitle();
		this.key = news.getKey();
		this.content = news.getContent();
		this.category = news.getCategory();
		this.reviewType = reviewType;
		this.creator = news.getCreator();
		this.lastModifiedBy = news.getLastModifiedBy();
		this.created = news.getCreated();
		this.lastModified = news.getLastModified();
		this.metaKeywords = news.getMetaKeywords();
		this.metaDescription = news.getMetaDescription();
		this.fields = fields;
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getKey() {
		return key;
	}

	public String getContent() {
		return content;
	}

	public String getCategory() {
		return category;
	}

	public String getReviewType() {
		return reviewType;
	}

	public String getCreator() {
		return creator;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
}
