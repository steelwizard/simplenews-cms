package simplenews.cms.data.dto;

import simplenews.cms.data.api.INavNode;
import simplenews.cms.data.entity.NavTree;

import java.util.ArrayList;
import java.util.List;

public class NavTreeDto {

	private Long id;
	private String handle;
	private String title;
	private List<INavNode> rootNodes = new ArrayList<>();

	public NavTreeDto() {}

	public NavTreeDto(NavTree tree) {
		this.id = tree.getId();
		this.handle = tree.getHandle();
		this.title = tree.getTitle();
	}

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getHandle() { return handle; }
	public void setHandle(String handle) { this.handle = handle; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public List<INavNode> getRootNodes() { return rootNodes; }
	public void setRootNodes(List<INavNode> rootNodes) { this.rootNodes = rootNodes; }
}
