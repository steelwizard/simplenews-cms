package simplenews.cms.data.dto;

import simplenews.cms.data.entity.NavigationItem;
import simplenews.cms.data.entity.NavigationMenuEnum;
import simplenews.cms.data.entity.NavigationTypeEnum;

public class NavigationItemDto {
	private Long id;
	private NavigationMenuEnum menu = NavigationMenuEnum.HEADER;
	private Long parent;
	private String label;
	private NavigationTypeEnum type = NavigationTypeEnum.PAGE;
	private Long pageId;
	private Long categoryId;
	private Long reviewTypeId;
	private String url;
	private boolean visible = true;
	private boolean newTab;

	public NavigationItemDto() {}

	public NavigationItemDto(NavigationItem item) {
		this.id = item.getId();
		this.menu = item.getMenu();
		this.parent = item.getParent();
		this.label = item.getLabel();
		this.type = item.getType();
		this.pageId = item.getPageId();
		this.categoryId = item.getCategoryId();
		this.reviewTypeId = item.getReviewTypeId();
		this.url = item.getUrl();
		this.visible = item.isVisible();
		this.newTab = item.isNewTab();
	}

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
}
