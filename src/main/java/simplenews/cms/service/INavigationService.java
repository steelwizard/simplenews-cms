package simplenews.cms.service;

import simplenews.cms.data.dto.NavAdminRowDto;
import simplenews.cms.data.dto.NavLinkDto;
import simplenews.cms.data.dto.NavSyncResultDto;
import simplenews.cms.data.dto.NavigationItemDto;
import simplenews.cms.data.entity.NavigationItem;
import simplenews.cms.data.entity.NavigationMenuEnum;

import java.util.List;

public interface INavigationService {
	List<NavLinkDto> getMenu(NavigationMenuEnum menu);

	List<NavAdminRowDto> getAdminRows(NavigationMenuEnum menu);

	NavigationItem getItem(Long id);

	List<NavigationItem> getParentCandidates(Long excludeId);

	NavigationItem save(NavigationItemDto dto, String user);

	void move(Long id, boolean up);

	void delete(Long id);

	void seedDefaults();

	/**
	 * Menu that receives new categories automatically, or null when switched off.
	 */
	NavigationMenuEnum getAutoCategoryMenu();

	void setAutoCategoryMenu(NavigationMenuEnum menu);

	/**
	 * Menu that receives new static pages automatically, or null when switched off.
	 */
	NavigationMenuEnum getAutoPageMenu();

	void setAutoPageMenu(NavigationMenuEnum menu);

	NavSyncResultDto sync();
}
