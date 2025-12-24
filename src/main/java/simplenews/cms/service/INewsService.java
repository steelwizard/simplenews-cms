package simplenews.cms.service;

import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;

import java.util.List;

public interface INewsService {
	List<INewsDto> getNews(int limit, int offset);

	List<INewsDto> getAllNewsAdmin(int limit, int offset);

	INewsDto getNewsById(Long id);
	INewsDto getNewsByKey(String key);

	List<INewsDto> getNewsByCategoryName(String name, int limit, int offset);

	List<INewsDto> getNewsByCategoryIds(List<Long> categoryIds, int limit, int offset);

	List<INewsDto> getNewsByCreatorAndCategories(String creator, List<Long> categoryIds, int limit, int offset);

	INewsDto createNews(NewsDto newsDto, String user) throws InsufficientPermissionException, DuplicateKeyException;
	INewsDto updateNews(Long id, NewsDto newsDto, String user) throws InsufficientPermissionException, DuplicateKeyException;

	void deleteNews(Long id) throws InsufficientPermissionException;

	void deleteNews(NewsDto dto) throws InsufficientPermissionException;
}
