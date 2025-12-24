package simplenews.cms.data.api;

import simplenews.cms.data.entity.CategoryTypeEnum;

import java.sql.Timestamp;

public interface ICategory {
	Long getId();

	void setId(Long id);

	String getName();

	void setName(String name);

	String getTitle();

	void setTitle(String title);

	CategoryTypeEnum getType();

	void setType(CategoryTypeEnum type);

	String getCreator();

	void setCreator(String creator);

	Timestamp getLastModified();

	void setLastModified(Timestamp lastModified);

	Timestamp getCreated();

	void setCreated(Timestamp created);

	Boolean getVisible();

	void setVisible(Boolean visible);

	Long getParent();

	void setParent(Long parent);

	String getMetaKeywords();
	void setMetaKeywords(String metaKeywords);
	String getMetaDescription();
	void setMetaDescription(String metaDescription);
}
