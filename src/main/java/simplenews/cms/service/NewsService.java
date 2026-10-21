package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import simplenews.cms.data.api.INews;
import simplenews.cms.data.dto.INewsDto;
import simplenews.cms.data.repository.NewsFieldValueRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.News;
import simplenews.cms.service.exception.DuplicateKeyException;
import simplenews.cms.service.exception.InsufficientPermissionException;
import simplenews.cms.service.util.SanitizeUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class NewsService implements INewsService {

	private static final Logger LOG = Logger.getLogger(NewsService.class.getName());

	public static final String NEWS_CACHE = "news";

	@Autowired
	NewsRepository newsRepository;
	@Autowired
	NewsFieldValueRepository newsFieldValueRepository;
//	@Autowired
//	CategoryRepository categoryRepository;
	@Autowired
	IUserService userService;
	@Autowired
	ICategoryService categoryService;
	@Autowired
	CacheManager caffeineCacheManager;
	@Autowired
	IPermissionService permissionService;
	@Autowired
	IUserCategoryService userCategoryService;

	@Cacheable("newsLatest")
	public List<INewsDto> getNews(int limit, int offset) {
		List<News> news = newsRepository.findByVisibleTrueOrderByLastModifiedDesc(limit, offset);
		List<INewsDto> newsDtoList = new ArrayList<>();
		news.forEach(n -> {
			INewsDto dto = new NewsDto(n);
			newsDtoList.add(dto);
		});
		return newsDtoList;
	}
	@Override
	public List<INewsDto> getAllNewsAdmin(int limit, int offset) {
		List<News> news = newsRepository.findOrderByLastModifiedDesc(limit, offset);
		List<INewsDto> newsDtoList = new ArrayList<>();
		news.forEach(n -> {
			INewsDto dto = new NewsDto(n);
			newsDtoList.add(dto);
		});
		return newsDtoList;
	}
	@Cacheable("news")
	public INewsDto getNewsById(Long id) {
		INewsDto newsDto = null;
//		Cache.ValueWrapper cached = getCache().get(id);
//		if (cached != null) {
//			LOG.log(Level.FINE, "cache hit for id: "+id);
//			newsDto = (NewsDto) cached.get();
//		}
		if (newsDto == null) {
			Optional<News> newsOptional = newsRepository.findById(id);
			if (newsOptional.isPresent()) {
				newsDto = new NewsDto(newsOptional.get());
//				getCache().put(newsDto.getId(), newsDto);
			}
		}
		return newsDto;
	}

	@Override
	@Cacheable("news")
	public INewsDto getNewsByKey(String key) {
		Optional<News> newsOptional = newsRepository.findByKey(key);
		INewsDto newsDto = null;
		if (newsOptional.isPresent()) {
			newsDto = new NewsDto(newsOptional.get());
		}
		return newsDto;
	}

	@Override
	@Cacheable("newsLatest")
	public List<INewsDto> getNewsByCategoryName(String name, int limit, int offset) {

		Category category = categoryService.getCategoryByName(name);
		List<News> news = new ArrayList<>();
		if (category != null) {
			news = newsRepository.findByCategoryAndVisibleTrueOrderByLastModifiedDesc(category.getId(), limit, offset);
		}
		List<INewsDto> newsDtoList = new ArrayList<>();
		Collections.sort(news, Comparator.comparing(News::getLastModified).reversed()); // Why doesn't this work in the DB?
		news.forEach(n -> {
			INewsDto dto = new NewsDto(n);
			newsDtoList.add(dto);
			getCache().put(dto.getId(), dto);
		});

		return newsDtoList;
	}
	@Override
	@Cacheable("newsLatest")
	public List<INewsDto> getNewsByCategoryIds(List<Long> categoryIds, int limit, int offset) {

		List<News> news = new ArrayList<>();

		news.addAll(newsRepository.findAllByCategoryIdIn(new HashSet<Long>(categoryIds), limit, offset));
		List<INewsDto> newsDtoList = new ArrayList<>();
		Collections.sort(news, Comparator.comparing(News::getLastModified).reversed()); // Why doesn't this work in the DB?
		news.forEach(n -> {
			INewsDto dto = new NewsDto(n);
			newsDtoList.add(dto);
			getCache().put(dto.getId(), dto);
		});

		return newsDtoList;
	}
	@Override
	public List<INewsDto> getNewsByCreatorAndCategories(String creator, List<Long> categoryIds, int limit, int offset) {
		LOG.log(Level.FINE, "getNewsByCreatorAndCategories");
		Set<Long> categorySet = new HashSet(categoryIds);
		List<News> news = new ArrayList<>();
		if (!categorySet.isEmpty()) {
			news = newsRepository.findAllByCreatorAndCategoryIdIn(creator, categorySet, limit, offset);
		}
		List<INewsDto> newsDtoList = new ArrayList<>();
		Collections.sort(news, Comparator.comparing(News::getLastModified).reversed()); // Why doesn't this work in the DB?
		news.forEach(n -> {
			INewsDto dto = new NewsDto(n);
			newsDtoList.add(dto);
			getCache().put(dto.getId(), dto);
		});

		return newsDtoList;
	}

//	@Override
//	public List<NewsDto> getAllNewsByCategoryId(long id) {
//
//		Category category = categoryService.getCategoryById(id);
//		List<News> news = new ArrayList<>();
//		if (category != null) {
//			news = newsRepository.findByCategoryId(category.getId());
//		}
//		List<NewsDto> newsDtoList = new ArrayList<>();
//		Collections.sort(news, Comparator.comparing(News::getLastModified).reversed());
//		news.forEach(n -> {
//			newsDtoList.add(new NewsDto(n));
//		});
//
//		return newsDtoList;
//	}

	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	@Override
	public NewsDto createNews(NewsDto newsDto, String user) throws InsufficientPermissionException,

			                                                       DuplicateKeyException {
		UserDto userDto = userService.getByUsername(user);
		if (userDto == null) {
			throw new IllegalArgumentException("No user to create article");
		}
		if(!permissionService.canCreate(newsDto)) throw new InsufficientPermissionException("Unauthorized article creation by user "+user);

		processKey(newsDto);

		News news = new News();

		news.setTitle(newsDto.getTitle());
		news.setCategory(categoryService.getCategoryByName(newsDto.getCategory()));
		news.setCreator(user);
		news.setLastModifiedBy(user);
		news.setContent(SanitizeUtil.sanitize(newsDto.getContent()));
		news.setKey(newsDto.getKey());
		news.setVisible(newsDto.getVisible());
		news.setCreated(new Timestamp(System.currentTimeMillis()));
		news.setLastModified(new Timestamp(System.currentTimeMillis()));
		news.setMetaKeywords(newsDto.getMetaKeywords());
		news.setMetaDescription(newsDto.getMetaDescription());


		News newsCreated = newsRepository.save(news);
		LOG.info(String.format("User %s created article %d %s: %s in Category %s.",
				newsCreated.getCreator(),
				newsCreated.getId(),
				newsCreated.getKey(),
				newsCreated.getTitle(),
				newsCreated.getCategory().getName())
		);
		return new NewsDto(newsCreated);
	}

	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	@Override
	public NewsDto updateNews(Long id, NewsDto newsDto, String user) throws InsufficientPermissionException,
																			DuplicateKeyException {
		UserDto userDto = userService.getByUsername(user);

		if (userDto == null) {
			throw new IllegalArgumentException("No user to edit article");
		}
		if(!permissionService.canEdit(newsDto)) throw new InsufficientPermissionException("Unauthorized article edit by user "+user+" for article "+newsDto.getId());

		News news = newsRepository.getReferenceById(id);
		if (news == null) {
			throw new IllegalArgumentException("Article doesn't exist");
		}
		if (!newsDto.getKey().equals(news.getKey()) && newsRepository.existsByKey(newsDto.getKey())) {
			processKey(newsDto);
			throw new DuplicateKeyException("key already exists");
		}

		news.setTitle(newsDto.getTitle());
		news.setCategory(categoryService.getCategoryByName(newsDto.getCategory()));
		news.setLastModifiedBy(user);
		news.setContent(SanitizeUtil.sanitize(newsDto.getContent()));
		news.setKey(newsDto.getKey());
		news.setVisible(newsDto.getVisible());
		news.setLastModified(new Timestamp(System.currentTimeMillis()));
		news.setMetaKeywords(newsDto.getMetaKeywords());
		news.setMetaDescription(newsDto.getMetaDescription());

		News newsUpdated = newsRepository.save(news);
		LOG.info(String.format("User %s updated article %d %s: %s in Category %s.",
				news.getCreator(),
				news.getId(),
				news.getKey(),
				news.getTitle(),
				news.getCategory().getName())
		);
		return new NewsDto(newsUpdated);
	}

	@Override
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public void deleteNews(Long id) throws InsufficientPermissionException {
		INews newsDto = newsRepository.getReferenceById(id);
		if(!permissionService.canDelete(new NewsDto(newsDto))) {
			throw new InsufficientPermissionException("Unauthorized article deletion by user "+userService.getCurrentUserName()+" for article id "+id);
		}
		newsFieldValueRepository.deleteByNews(id);
		newsRepository.deleteById(id);
		LOG.info("deleting post "+id);
	}
	@Override
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public void deleteNews(NewsDto dto) throws InsufficientPermissionException {
		if(!permissionService.canDelete(dto)) {
			throw new InsufficientPermissionException("Unauthorized article deletion by user "+userService.getCurrentUserName()+" for article id "+dto.getId());
		}
		newsFieldValueRepository.deleteByNews(dto.getId());
		newsRepository.deleteById(dto.getId());
	}

	private void processKey(NewsDto newsDto) {
		if (newsDto.getKey() == null || newsDto.getKey().isEmpty()) {
			StringBuilder keySb = new StringBuilder().append("");

			if (newsDto.getTitle() != null && !newsDto.getTitle().isEmpty() && !newsDto.getTitle().isBlank()) {
				String trimmedTitle = newsDto.getTitle().strip().toLowerCase().replaceAll("\s+","-");
				if (trimmedTitle.length()>15) {
					trimmedTitle = trimmedTitle.substring(0,14);
				}
				keySb.append(trimmedTitle);
			}
			if(!keySb.toString().endsWith("-"))keySb.append("-");
			keySb.append(Long.toString(System.currentTimeMillis()));
			newsDto.setKey(keySb.toString());
		} else {
			newsDto.setKey(newsDto.getKey().strip().toLowerCase().replaceAll("\s+","-"));
		}
		if (newsRepository.existsByKey(newsDto.getKey())) {
//			throw new DuplicateKeyException("key already exists");
			LOG.log(Level.FINE, "key "+newsDto.getKey()+"already exists");
			newsDto.setKey(newsDto.getKey()+"-"+System.currentTimeMillis());
		}
	}

	private Cache getCache() {
		Cache cache = caffeineCacheManager.getCache(NEWS_CACHE);
		return (cache);
	}

}
