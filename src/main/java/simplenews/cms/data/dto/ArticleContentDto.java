package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ArticleContent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArticleContentDto implements INewsDto {

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
	private Boolean visible;
	private String metaKeywords;
	private String metaDescription;
	private String preview;
	private Integer orderIndex;
	private Long parentId;
	private List<ArticleContentDto> children = new ArrayList<>();

	public ArticleContentDto() {}

	public ArticleContentDto(ArticleContent entity) {
		this.id = entity.getId();
		this.title = entity.getTitle();
		this.key = entity.getKey();
		this.content = entity.getContent();
		this.category = entity.getCategory() != null ? entity.getCategory().getName() : null;
		this.creator = entity.getCreator();
		this.lastModifiedBy = entity.getLastModifiedBy();
		this.lastModified = entity.getLastModified() != null ? entity.getLastModified().toLocalDateTime() : null;
		this.created = entity.getCreated() != null ? entity.getCreated().toLocalDateTime() : null;
		this.visible = entity.getVisible();
		this.metaKeywords = entity.getMetaKeywords();
		this.metaDescription = entity.getMetaDescription();
		this.preview = entity.getPreview();
		this.orderIndex = entity.getOrderIndex();
		this.parentId = entity.getParent() != null ? entity.getParent().getId() : null;
	}

	@Override public Long getId() { return id; }
	@Override public void setId(Long id) { this.id = id; }
	@Override public String getTitle() { return title; }
	@Override public void setTitle(String title) { this.title = title; }
	@Override public String getKey() { return key; }
	@Override public void setKey(String key) { this.key = key; }
	@Override public String getContent() { return content; }
	@Override public void setContent(String content) { this.content = content; }
	@Override public String[] getTags() { return tags; }
	@Override public void setTags(String[] tags) { this.tags = tags; }
	@Override public String getCategory() { return category; }
	@Override public void setCategory(String category) { this.category = category; }
	@Override public String getCreator() { return creator; }
	@Override public void setCreator(String creator) { this.creator = creator; }
	@Override public LocalDateTime getLastModified() { return lastModified; }
	@Override public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
	@Override public String getLastModifiedBy() { return lastModifiedBy; }
	@Override public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }
	@Override public LocalDateTime getCreated() { return created; }
	@Override public void setCreated(LocalDateTime created) { this.created = created; }
	@Override public Boolean getVisible() { return visible; }
	@Override public void setVisible(Boolean visible) { this.visible = visible; }
	@Override public String getMetaKeywords() { return metaKeywords; }
	@Override public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }
	@Override public String getMetaDescription() { return metaDescription; }
	@Override public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }
	@Override public String getPreview() { return preview; }
	@Override public void setPreview(String preview) { this.preview = preview; }

	public Integer getOrderIndex() { return orderIndex; }
	public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

	public Long getParentId() { return parentId; }
	public void setParentId(Long parentId) { this.parentId = parentId; }

	public List<ArticleContentDto> getChildren() { return children; }
	public void setChildren(List<ArticleContentDto> children) { this.children = children; }
}
