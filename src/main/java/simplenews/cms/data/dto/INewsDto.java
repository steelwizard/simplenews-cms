package simplenews.cms.data.dto;

import java.time.LocalDateTime;

public interface INewsDto {
	Long getId();

	void setId(Long id);

	String getTitle();

	void setTitle(String title);

	String getKey();

	void setKey(String key);

	String getContent();

	void setContent(String content);

	String[] getTags();

	void setTags(String[] tags);

	String getCategory();

	void setCategory(String category);

	String getCreator();

	void setCreator(String creator);

	LocalDateTime getLastModified();

	void setLastModified(LocalDateTime lastModified);

	String getLastModifiedBy();

	void setLastModifiedBy(String lastModifiedBy);

	LocalDateTime getCreated();

	void setCreated(LocalDateTime created);

	Boolean getVisible();

	void setVisible(Boolean visible);

	String getMetaKeywords();
	void setMetaKeywords(String metaKeywords);
	String getMetaDescription();
	void setMetaDescription(String metaDescription);

	String getPreview();

	void setPreview(String preview);

	@Override
	String toString();
}
