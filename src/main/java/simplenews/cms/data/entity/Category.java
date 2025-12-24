package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import simplenews.cms.data.api.ICategory;
import simplenews.cms.data.api.ICategoryDto;

import java.sql.Timestamp;

@Entity
@Table(name = "category")
public class Category implements ICategory {

	@Id
	@Column
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String title;

	@JoinColumn(name="parent",referencedColumnName = "id")
	private Long parent;
	@Column
	@Enumerated(EnumType.STRING)
	private CategoryTypeEnum type;

	@Column(name = "creator")
	private String creator;

	@Column(name = "last_modified")
	private Timestamp lastModified;

	@Column(name = "created")
	private Timestamp created;

	@Column(name = "visible")
	private Boolean visible;

	@Column(name = "meta_keywords")
	private String metaKeywords;

	@Column(name = "meta_description")
	private String metaDescription;

	public Category(){}
	public Category(ICategoryDto categoryDto){
		this.id = categoryDto.getId();
		this.created = categoryDto.getCreated();
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
	public CategoryTypeEnum getType() {
		return type;
	}

	@Override
	public void setType(CategoryTypeEnum type) {
		this.type = type;
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
	public Timestamp getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public Timestamp getCreated() {
		return created;
	}

	@Override
	public void setCreated(Timestamp created) {
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
	public Long getParent() {
		return parent;
	}

	@Override
	public void setParent(Long parent) {
		this.parent = parent;
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
