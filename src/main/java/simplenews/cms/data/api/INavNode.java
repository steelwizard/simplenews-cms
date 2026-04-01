package simplenews.cms.data.api;

import simplenews.cms.data.entity.NavRefTypeEnum;

import java.util.List;

public interface INavNode {
	Long getId();
	String getLabel();
	String getUrl();
	NavRefTypeEnum getRefType();
	Long getRefId();
	boolean isVisible();
	boolean isOpenInNewTab();
	int getOrderIndex();
	List<INavNode> getChildren();
	void setChildren(List<INavNode> children);
}
