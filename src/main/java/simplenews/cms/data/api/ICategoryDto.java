package simplenews.cms.data.api;

import simplenews.cms.data.entity.CategoryTypeEnum;

import java.sql.Timestamp;
import java.util.List;

public interface ICategoryDto {
	Long getId();

	void setId(Long id);

	String getName();

	void setName(String name);

	String getTitle();

	void setTitle(String title);

	Long getParent();

	void setParent(Long parent);

	CategoryTypeEnum getType();

	void setType(CategoryTypeEnum type);

	boolean isVisible();

	void setVisible(boolean visible);

	List<ICategoryDto> getChildren();

	void setChildren(List<ICategoryDto> categoryDtos);

	String getCreator();

	void setCreator(String creator);

	Timestamp getCreated();

	void setCreated(Timestamp created);

	String getMetaKeywords();
	void setMetaKeywords(String metaKeywords);
	String getMetaDescription();
	void setMetaDescription(String metaDescription);
	Long getReviewType();
	void setReviewType(Long reviewType);

}
