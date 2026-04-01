package simplenews.cms.service;

import simplenews.cms.data.dto.NavNodeDto;
import simplenews.cms.data.dto.NavTreeDto;

import java.util.List;

public interface INavTreeService {

	List<NavTreeDto> getAllTrees();

	NavTreeDto getTreeByHandle(String handle);

	NavTreeDto getTreeById(Long id);

	NavTreeDto saveTree(NavTreeDto dto, String user);

	void deleteTree(Long id);

	NavNodeDto saveNode(NavNodeDto dto);

	void deleteNode(Long id);

	void refreshNavCache();
}
