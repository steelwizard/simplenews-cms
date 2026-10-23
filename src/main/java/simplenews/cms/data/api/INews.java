package simplenews.cms.data.api;

import simplenews.cms.data.entity.Category;

import java.sql.Timestamp;

public interface INews {
	Long getId();

	void setId(Long id);

	String getTitle();

	void setTitle(String title);

	String getKey();

	void setKey(String key);

	String getContent();

	void setContent(String content);

	Category getCategory();

	void setCategory(Category category);

	String getCreator();

	void setCreator(String creator);

	String getLastModifiedBy();

	void setLastModifiedBy(String lastModifiedBy);

	Timestamp getLastModified();

	void setLastModified(Timestamp lastModified);

	Timestamp getCreated();

	void setCreated(Timestamp created);

	Boolean getVisible();

	void setVisible(Boolean visible);

	String getMetaKeywords();
	void setMetaKeywords(String metaKeywords);
	String getMetaDescription();
	void setMetaDescription(String metaDescription);

	Long getPhpbbTopicId();
	void setPhpbbTopicId(Long phpbbTopicId);
	Integer getPhpbbReplyCount();
	void setPhpbbReplyCount(Integer phpbbReplyCount);

	@Override
	String toString();
}
