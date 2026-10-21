package simplenews.cms.data.dto;

import simplenews.cms.data.api.ICategory;
import simplenews.cms.data.api.ICategoryDto;
import simplenews.cms.data.entity.CategoryTypeEnum;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CategoryDto implements ICategoryDto {

	public CategoryDto(Long id, String name, String title, Long parent, CategoryTypeEnum type, boolean visible, Timestamp created, String metaKeywords, String metaDescription) {
		this.id = id;
		this.name = name;
		this.title = title;
		this.parent = parent;
		this.type = type;
		this.visible = visible;
		this.created = created;
		this.metaKeywords = metaKeywords;
		this.metaDescription = metaDescription;
	}

	public CategoryDto(ICategory category) {
		this.id = category.getId();
		this.name = category.getName();
		this.title = category.getTitle();
		this.parent = category.getParent();
		this.type = category.getType();
		this.visible = category.getVisible();
		this.created = category.getCreated();
		this.metaKeywords = category.getMetaKeywords();
		this.metaDescription = category.getMetaDescription();
		this.reviewType = category.getReviewType();
	}
	public CategoryDto() {}

	private Long id;
	private String name;
	private String title;

	private Long parent;

	private CategoryTypeEnum type;

	private boolean visible;

	private List<ICategoryDto> children = new ArrayList();

	private String creator;

	private Timestamp created;

	private String metaKeywords;

	private String metaDescription;

	private Long reviewType;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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
	public Long getParent() {
		return parent;
	}

	@Override
	public void setParent(Long parent) {
		this.parent = parent;
	}

	@Override
	public CategoryTypeEnum getType() {
		return type;
	}

	@Override
	public void setType(CategoryTypeEnum type) {
		this.type = type;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public List<ICategoryDto> getChildren() {
		return children;
	}

	@Override
	public void setChildren(List<ICategoryDto> categoryDtos) {
		this.children = categoryDtos;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
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

	@Override
	public Long getReviewType() {
		return reviewType;
	}

	@Override
	public void setReviewType(Long reviewType) {
		this.reviewType = reviewType;
	}
}
