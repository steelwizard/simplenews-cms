package simplenews.cms.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import simplenews.cms.data.api.INews;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.StringJoiner;

public class NewsDto implements INewsDto {

	private Long id;
	private String title;
	private String key;
	private String content;
	private String[] tags;
	private String category;
	private String creator;
	private String lastModifiedBy;
	private LocalDateTime lastModified;
	private LocalDateTime created;
	@JsonIgnore
	private Boolean visible;
	private String metaKeywords;
	private String metaDescription;
	private String preview;

	public NewsDto() {}
	public NewsDto(INews news) {
		this.id = news.getId();
		this.title = news.getTitle();
		this.key = news.getKey();
		this.content = news.getContent();
		this.category = news.getCategory().getName();
		this.creator = news.getCreator();
		this.lastModified = news.getLastModified().toLocalDateTime();
		this.created = news.getCreated().toLocalDateTime();
		this.visible = news.getVisible();
		this.metaKeywords = news.getMetaKeywords();
		this.metaDescription = news.getMetaDescription();
		this.lastModifiedBy = news.getLastModifiedBy();
		this.preview = news.getPreview();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String[] getTags() {
		return tags;
	}

	@Override
	public void setTags(String[] tags) {
		this.tags = tags;
	}

	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	@Override
	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public LocalDateTime getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	@Override
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Override
	public LocalDateTime getCreated() {
		return created;
	}

	@Override
	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	@Override
	public Boolean getVisible() {
		return visible;
	}

	@Override
	public void setVisible(Boolean visible) {
		this.visible = visible;
	}
	@Override
	public String getMetaKeywords() {
		return metaKeywords;
	}
	@Override
	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}
	@Override
	public String getMetaDescription() {
		return metaDescription;
	}
	@Override
	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}
	@Override
	public String getPreview() {
		return preview;
	}
	@Override
	public void setPreview(String preview) {
		this.preview = preview;
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", NewsDto.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("title='" + title + "'")
				.add("key='" + key + "'")
				.add("content='" + content + "'")
				.add("tags=" + Arrays.toString(tags))
				.add("category='" + category + "'")
				.add("creator='" + creator + "'")
				.add("lastModifiedBy='" + lastModifiedBy + "'")
				.add("lastModified=" + lastModified)
				.add("created=" + created)
				.add("visible=" + visible)
				.add("metaKeywords='" + metaKeywords + "'")
				.add("metaDescription='" + metaDescription + "'")
				.add("preview='" + preview + "'")
				.toString();
	}
}
