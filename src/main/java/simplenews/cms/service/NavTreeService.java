package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;
import simplenews.cms.data.api.INavNode;
import simplenews.cms.data.dto.NavNodeDto;
import simplenews.cms.data.dto.NavTreeDto;
import simplenews.cms.data.entity.NavNode;
import simplenews.cms.data.entity.NavTree;
import simplenews.cms.data.repository.NavNodeRepository;
import simplenews.cms.data.repository.NavTreeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@ApplicationScope
public class NavTreeService implements INavTreeService {

	private static final Logger LOG = Logger.getLogger(NavTreeService.class.getName());

	@Autowired
	private NavTreeRepository navTreeRepository;

	@Autowired
	private NavNodeRepository navNodeRepository;

	@Override
	public List<NavTreeDto> getAllTrees() {
		return navTreeRepository.findAll()
				.stream()
				.map(NavTreeDto::new)
				.toList();
	}

	@Override
	@Cacheable("navTree")
	public NavTreeDto getTreeByHandle(String handle) {
		Optional<NavTree> tree = navTreeRepository.findByHandle(handle);
		return tree.map(this::loadTree).orElse(null);
	}

	@Override
	@Cacheable("navTree")
	public NavTreeDto getTreeById(Long id) {
		NavTree tree = navTreeRepository.getReferenceById(id);
		return loadTree(tree);
	}

	@Override
	@CacheEvict(cacheNames = "navTree", allEntries = true)
	public NavTreeDto saveTree(NavTreeDto dto, String user) {
		NavTree entity = dto.getId() != null
				? navTreeRepository.getReferenceById(dto.getId())
				: new NavTree();
		entity.setHandle(dto.getHandle());
		entity.setTitle(dto.getTitle());
		return new NavTreeDto(navTreeRepository.save(entity));
	}

	@Override
	@CacheEvict(cacheNames = "navTree", allEntries = true)
	public void deleteTree(Long id) {
		navTreeRepository.deleteById(id);
	}

	@Override
	@CacheEvict(cacheNames = "navTree", allEntries = true)
	public NavNodeDto saveNode(NavNodeDto dto) {
		NavNode entity = dto.getId() != null
				? navNodeRepository.getReferenceById(dto.getId())
				: new NavNode();
		entity.setTree(navTreeRepository.getReferenceById(dto.getTreeId()));
		entity.setParent(dto.getParentId() != null
				? navNodeRepository.getReferenceById(dto.getParentId())
				: null);
		entity.setLabel(dto.getLabel());
		entity.setUrl(dto.getUrl());
		entity.setRefType(dto.getRefType());
		entity.setRefId(dto.getRefId());
		entity.setVisible(dto.isVisible());
		entity.setOpenInNewTab(dto.isOpenInNewTab());
		entity.setOrderIndex(dto.getOrderIndex());
		return new NavNodeDto(navNodeRepository.save(entity));
	}

	@Override
	@CacheEvict(cacheNames = "navTree", allEntries = true)
	public void deleteNode(Long id) {
		navNodeRepository.deleteById(id);
	}

	@Override
	@CacheEvict(cacheNames = "navTree", allEntries = true)
	public void refreshNavCache() {
		LOG.info("Evicting nav tree cache.");
	}

	// Mirrors CategoryService.loadCategories(): fetch flat list, build tree in memory.
	private NavTreeDto loadTree(NavTree tree) {
		NavTreeDto dto = new NavTreeDto(tree);
		List<NavNode> allNodes = navNodeRepository.findByTreeIdOrderByOrderIndex(tree.getId());

		Map<Long, NavNodeDto> nodeMap = allNodes.stream()
				.map(NavNodeDto::new)
				.collect(Collectors.toMap(NavNodeDto::getId, n -> n));

		List<INavNode> roots = new ArrayList<>();
		allNodes.forEach(node -> {
			NavNodeDto nodeDto = nodeMap.get(node.getId());
			if (node.getParent() == null) {
				roots.add(nodeDto);
			} else {
				NavNodeDto parentDto = nodeMap.get(node.getParent().getId());
				if (parentDto != null) {
					parentDto.getChildren().add(nodeDto);
				}
			}
		});
		dto.setRootNodes(roots);
		return dto;
	}
}
