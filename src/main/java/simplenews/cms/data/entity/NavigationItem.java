package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.sql.Timestamp;

@Entity
@Table(name = "navigation_item")
public class NavigationItem {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "menu", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private NavigationMenuEnum menu;

	@Column(name = "parent")
	private Long parent;

	@Column(name = "position", nullable = false)
	private int position;

	@Column(name = "label", length = 100)
	private String label;

	@Column(name = "type", nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private NavigationTypeEnum type;

	@Column(name = "page_id")
	private Long pageId;

	@Column(name = "category_id")
	private Long categoryId;

	@Column(name = "review_type_id")
	private Long reviewTypeId;

	@Column(name = "url", length = 500)
	private String url;

	@Column(name = "visible", nullable = false)
	private boolean visible = true;

	@Column(name = "new_tab", nullable = false)
	private boolean newTab;

	@Column(name = "auto_managed", nullable = false, columnDefinition = "boolean not null default false")
	private boolean autoManaged;

	@Column(name = "creator")
	private String creator;

	@Column(name = "created")
	private Timestamp created;

	@Column(name = "last_modified")
	private Timestamp lastModified;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public NavigationMenuEnum getMenu() {
		return menu;
	}

	public void setMenu(NavigationMenuEnum menu) {
		this.menu = menu;
	}

	public Long getParent() {
		return parent;
	}

	public void setParent(Long parent) {
		this.parent = parent;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public NavigationTypeEnum getType() {
		return type;
	}

	public void setType(NavigationTypeEnum type) {
		this.type = type;
	}

	public Long getPageId() {
		return pageId;
	}

	public void setPageId(Long pageId) {
		this.pageId = pageId;
	}

	public Long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}

	public Long getReviewTypeId() {
		return reviewTypeId;
	}

	public void setReviewTypeId(Long reviewTypeId) {
		this.reviewTypeId = reviewTypeId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isNewTab() {
		return newTab;
	}

	public void setNewTab(boolean newTab) {
		this.newTab = newTab;
	}

	public boolean isAutoManaged() {
		return autoManaged;
	}

	public void setAutoManaged(boolean autoManaged) {
		this.autoManaged = autoManaged;
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

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}
}
