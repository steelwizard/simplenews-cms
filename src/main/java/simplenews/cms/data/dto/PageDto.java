package simplenews.cms.data.dto;

import simplenews.cms.data.entity.StaticPage;

import java.time.LocalDateTime;

public class PageDto {
	private Long id;
	private String title;
	private String key;
	private String content;
	private String creator;
	private String lastModifiedBy;
	private LocalDateTime lastModified;
	private LocalDateTime created;
	private Boolean visible;
	private String metaKeywords;
	private String metaDescription;

	public PageDto() {}

	public PageDto(StaticPage page) {
		this.id = page.getId();
		this.title = page.getTitle();
		this.key = page.getKey();
		this.content = page.getContent();
		this.creator = page.getCreator();
		this.lastModifiedBy = page.getLastModifiedBy();
		if (page.getLastModified() != null) {
			this.lastModified = page.getLastModified().toLocalDateTime();
		}
		if (page.getCreated() != null) {
			this.created = page.getCreated().toLocalDateTime();
		}
		this.visible = page.getVisible();
		this.metaKeywords = page.getMetaKeywords();
		this.metaDescription = page.getMetaDescription();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public Boolean getVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	public String getMetaKeywords() {
		return metaKeywords;
	}

	public void setMetaKeywords(String metaKeywords) {
		this.metaKeywords = metaKeywords;
	}

	public String getMetaDescription() {
		return metaDescription;
	}

	public void setMetaDescription(String metaDescription) {
		this.metaDescription = metaDescription;
	}
}
