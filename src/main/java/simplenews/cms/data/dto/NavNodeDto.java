package simplenews.cms.data.dto;

import simplenews.cms.data.api.INavNode;
import simplenews.cms.data.entity.NavNode;
import simplenews.cms.data.entity.NavRefTypeEnum;

import java.util.ArrayList;
import java.util.List;

public class NavNodeDto implements INavNode {

	private Long id;
	private Long treeId;
	private Long parentId;
	private String label;
	private String url;
	private NavRefTypeEnum refType;
	private Long refId;
	private boolean visible;
	private boolean openInNewTab;
	private int orderIndex;
	private List<INavNode> children = new ArrayList<>();

	public NavNodeDto() {}

	public NavNodeDto(NavNode node) {
		this.id = node.getId();
		this.treeId = node.getTree().getId();
		this.parentId = node.getParent() != null ? node.getParent().getId() : null;
		this.label = node.getLabel();
		this.url = node.getUrl();
		this.refType = node.getRefType();
		this.refId = node.getRefId();
		this.visible = node.isVisible();
		this.openInNewTab = node.isOpenInNewTab();
		this.orderIndex = node.getOrderIndex();
	}

	@Override public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getTreeId() { return treeId; }
	public void setTreeId(Long treeId) { this.treeId = treeId; }

	public Long getParentId() { return parentId; }
	public void setParentId(Long parentId) { this.parentId = parentId; }

	@Override public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }

	@Override public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	@Override public NavRefTypeEnum getRefType() { return refType; }
	public void setRefType(NavRefTypeEnum refType) { this.refType = refType; }

	@Override public Long getRefId() { return refId; }
	public void setRefId(Long refId) { this.refId = refId; }

	@Override public boolean isVisible() { return visible; }
	public void setVisible(boolean visible) { this.visible = visible; }

	@Override public boolean isOpenInNewTab() { return openInNewTab; }
	public void setOpenInNewTab(boolean openInNewTab) { this.openInNewTab = openInNewTab; }

	@Override public int getOrderIndex() { return orderIndex; }
	public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

	@Override public List<INavNode> getChildren() { return children; }
	@Override public void setChildren(List<INavNode> children) { this.children = children; }
}
