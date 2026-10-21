package simplenews.cms.data.dto;

import simplenews.cms.data.entity.NavigationItem;

public class NavAdminRowDto {
	private final NavigationItem item;
	private final int depth;
	private final String label;
	private final String target;
	private final boolean broken;
	private final boolean first;
	private final boolean last;

	public NavAdminRowDto(NavigationItem item, int depth, String label, String target,
						  boolean broken, boolean first, boolean last) {
		this.item = item;
		this.depth = depth;
		this.label = label;
		this.target = target;
		this.broken = broken;
		this.first = first;
		this.last = last;
	}

	public NavigationItem getItem() {
		return item;
	}

	public int getDepth() {
		return depth;
	}

	public String getLabel() {
		return label;
	}

	public String getTarget() {
		return target;
	}

	public boolean isBroken() {
		return broken;
	}

	public boolean isFirst() {
		return first;
	}

	public boolean isLast() {
		return last;
	}
}
