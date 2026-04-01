package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import simplenews.cms.data.api.INews;

import java.sql.Timestamp;
import java.util.StringJoiner;

@Entity
@Table(name = "content")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING)
public abstract class Content implements INews {

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title")
	private String title;

	@Column(name = "key")
	private String key;

	@Column(name = "content")
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

	@Column(name = "preview")
	private String preview;

	// Self-reference used by ArticleContent for tree structure
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Content parent;

	@Override
	public Long getId() { return id; }
	@Override
	public void setId(Long id) { this.id = id; }

	@Override
	public String getTitle() { return title; }
	@Override
	public void setTitle(String title) { this.title = title; }

	@Override
	public String getKey() { return key; }
	@Override
	public void setKey(String key) { this.key = key; }

	@Override
	public String getContent() { return content; }
	@Override
	public void setContent(String content) { this.content = content; }

	@Override
	public Category getCategory() { return category; }
	@Override
	public void setCategory(Category category) { this.category = category; }

	@Override
	public String getCreator() { return creator; }
	@Override
	public void setCreator(String creator) { this.creator = creator; }

	@Override
	public String getLastModifiedBy() { return lastModifiedBy; }
	@Override
	public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

	@Override
	public Timestamp getLastModified() { return lastModified; }
	@Override
	public void setLastModified(Timestamp lastModified) { this.lastModified = lastModified; }

	@Override
	public Timestamp getCreated() { return created; }
	@Override
	public void setCreated(Timestamp created) { this.created = created; }

	@Override
	public Boolean getVisible() { return visible; }
	@Override
	public void setVisible(Boolean visible) { this.visible = visible; }

	@Override
	public String getMetaKeywords() { return metaKeywords; }
	@Override
	public void setMetaKeywords(String metaKeywords) { this.metaKeywords = metaKeywords; }

	@Override
	public String getMetaDescription() { return metaDescription; }
	@Override
	public void setMetaDescription(String metaDescription) { this.metaDescription = metaDescription; }

	@Override
	public String getPreview() { return preview; }
	@Override
	public void setPreview(String preview) { this.preview = preview; }

	public Content getParent() { return parent; }
	public void setParent(Content parent) { this.parent = parent; }

	@Override
	public String toString() {
		return new StringJoiner(", ", getClass().getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("title='" + title + "'")
				.add("key='" + key + "'")
				.add("category=" + category)
				.add("creator='" + creator + "'")
				.add("lastModified=" + lastModified)
				.add("visible=" + visible)
				.toString();
	}
}
