package simplenews.cms.service;

import simplenews.cms.data.dto.PageDto;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import java.util.List;

public interface IPageService {
	List<PageDto> list(int limit, int offset) throws InsufficientPermissionException;

	List<PageDto> listVisible();

	PageDto getById(Long id);

	PageDto getByKey(String key);

	PageDto create(PageDto pageDto, String user) throws InsufficientPermissionException, DuplicateKeyException;

	PageDto update(Long id, PageDto pageDto, String user) throws InsufficientPermissionException, DuplicateKeyException;

	void delete(Long id) throws InsufficientPermissionException;

	void prepareKey(PageDto pageDto);
}
