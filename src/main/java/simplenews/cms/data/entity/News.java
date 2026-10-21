package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import simplenews.cms.data.api.INews;

import java.sql.Timestamp;
import java.util.StringJoiner;

@Entity
@Table(name = "news")
public class News implements INews {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title")
	private String title;

	@Column(name = "key")
	private String key;

	@Column(name = "content", columnDefinition = "text")
	private String content;

	@ManyToOne
	@JoinColumn(name = "category")
	private Category category;

	@Column(name = "creator")
	private String creator;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

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
	public Category getCategory() {
		return category;
	}

	@Override
	public void setCategory(Category category) {
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
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	@Override
	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
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
	public String toString() {
		return new StringJoiner(", ", News.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("title='" + title + "'")
				.add("key='" + key + "'")
				.add("content='" + content + "'")
				.add("category=" + category)
				.add("creator='" + creator + "'")
				.add("lastModifiedBy=" + lastModifiedBy)
				.add("lastModified=" + lastModified)
				.add("created=" + created)
				.add("visible=" + visible)
				.add("metaKeywords='" + metaKeywords + "'")
				.add("metaDescription='" + metaDescription + "'")
				.toString();
	}
}
