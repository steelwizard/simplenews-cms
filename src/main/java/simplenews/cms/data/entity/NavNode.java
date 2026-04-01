package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "nav_node")
public class NavNode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tree_id", nullable = false)
	private NavTree tree;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private NavNode parent;

	@Column(name = "label", nullable = false)
	private String label;

	// Explicit URL override. Used when ref_type = URL, or to override a
	// generated URL for other ref types.
	@Column(name = "url")
	private String url;

	@Column(name = "ref_type")
	@Enumerated(EnumType.STRING)
	private NavRefTypeEnum refType;

	// ID of the referenced entity (category id, article content id, etc.)
	@Column(name = "ref_id")
	private Long refId;

	@Column(name = "visible", nullable = false)
	private boolean visible = true;

	@Column(name = "open_in_new_tab", nullable = false)
	private boolean openInNewTab = false;

	@Column(name = "order_index", nullable = false)
	private int orderIndex = 0;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public NavTree getTree() { return tree; }
	public void setTree(NavTree tree) { this.tree = tree; }

	public NavNode getParent() { return parent; }
	public void setParent(NavNode parent) { this.parent = parent; }

	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public NavRefTypeEnum getRefType() { return refType; }
	public void setRefType(NavRefTypeEnum refType) { this.refType = refType; }

	public Long getRefId() { return refId; }
	public void setRefId(Long refId) { this.refId = refId; }

	public boolean isVisible() { return visible; }
	public void setVisible(boolean visible) { this.visible = visible; }

	public boolean isOpenInNewTab() { return openInNewTab; }
	public void setOpenInNewTab(boolean openInNewTab) { this.openInNewTab = openInNewTab; }

	public int getOrderIndex() { return orderIndex; }
	public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
