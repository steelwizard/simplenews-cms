package simplenews.cms.data.dto;

import simplenews.cms.data.api.ICategoryDto;

import java.util.ArrayList;
import java.util.List;

/**
 * A resolved menu entry, ready for rendering. href is null for plain headings;
 * categoryTree is set only for CATEGORY_TREE entries.
 */
public class NavLinkDto {
	private String label;
	private String href;
	private boolean newTab;
	private List<ICategoryDto> categoryTree;
	private List<NavLinkDto> children = new ArrayList<>();

	public NavLinkDto(String label, String href, boolean newTab) {
		this.label = label;
		this.href = href;
		this.newTab = newTab;
	}

	public String getLabel() {
		return label;
	}

	public String getHref() {
		return href;
	}

	public boolean isNewTab() {
		return newTab;
	}

	public List<ICategoryDto> getCategoryTree() {
		return categoryTree;
	}

	public void setCategoryTree(List<ICategoryDto> categoryTree) {
		this.categoryTree = categoryTree;
	}

	public List<NavLinkDto> getChildren() {
		return children;
	}
}
