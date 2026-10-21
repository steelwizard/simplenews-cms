package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.dto.DownloadFileDto;
import simplenews.cms.data.dto.NewsDto;
import simplenews.cms.data.dto.UploadFileDto;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.entity.NewsFile;
import simplenews.cms.data.entity.UploadFile;
import simplenews.cms.data.repository.NewsFileRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.data.repository.UploadFileRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NewsFileService implements INewsFileService {

	@Autowired
	NewsFileRepository newsFileRepository;
	@Autowired
	UploadFileRepository uploadFileRepository;
	@Autowired
	NewsRepository newsRepository;
	@Autowired
	IUploadFileService uploadFileService;
	@Autowired
	IPermissionService permissionService;

	@Override
	public List<DownloadFileDto> getByNewsId(Long newsId) {
		if (newsId == null) return List.of();
		return getByNewsIds(List.of(newsId)).getOrDefault(newsId, List.of());
	}

	@Override
	public Map<Long, List<DownloadFileDto>> getByNewsIds(Collection<Long> newsIds) {
		Map<Long, List<DownloadFileDto>> result = new LinkedHashMap<>();
		if (newsIds == null || newsIds.isEmpty()) return result;
		List<NewsFile> links = newsFileRepository.findByNewsIdInOrderByNewsIdAscPositionAscIdAsc(newsIds);
		if (links.isEmpty()) return result;
		Set<Long> fileIds = links.stream().map(NewsFile::getFileId).collect(Collectors.toSet());
		Map<Long, UploadFile> files = new HashMap<>();
		uploadFileRepository.findAllById(fileIds).forEach(f -> files.put(f.getId(), f));
		for (NewsFile link : links) {
			UploadFile file = files.get(link.getFileId());
			if (file == null) continue;
			result.computeIfAbsent(link.getNewsId(), k -> new ArrayList<>()).add(new DownloadFileDto(file));
		}
		return result;
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public void addFiles(Long newsId, MultipartFile[] files, String user) {
		if (newsId == null || files == null) return;
		int position = newsFileRepository.findMaxPosition(newsId);
		for (MultipartFile upload : files) {
			if (upload == null || upload.isEmpty()) continue;
			UploadFile saved = uploadFileService.upload(upload, user);
			if (saved == null) {
				throw new IllegalArgumentException("Could not store " + upload.getOriginalFilename() + ".");
			}
			NewsFile link = new NewsFile();
			link.setNewsId(newsId);
			link.setFileId(saved.getId());
			link.setPosition(++position);
			newsFileRepository.save(link);
		}
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public void removeFiles(Long newsId, Collection<Long> fileIds) {
		if (newsId == null || fileIds == null || fileIds.isEmpty()) return;
		newsFileRepository.deleteByNewsIdAndFileIdIn(newsId, fileIds);
	}

	@Override
	@Transactional
	@CacheEvict(cacheNames = {"news", "newsLatest"}, allEntries = true)
	public void deleteByNews(Long newsId) {
		if (newsId == null) return;
		newsFileRepository.deleteByNewsId(newsId);
	}

	@Override
	public UploadFileDto getDownload(Long fileId) {
		if (fileId == null) return null;
		List<NewsFile> links = newsFileRepository.findByFileId(fileId);
		if (links.isEmpty()) return null;
		boolean allowed = false;
		for (NewsFile link : links) {
			News news = newsRepository.findById(link.getNewsId()).orElse(null);
			if (news == null) continue;
			if (Boolean.TRUE.equals(news.getVisible()) || permissionService.canEdit(new NewsDto(news))) {
				allowed = true;
				break;
			}
		}
		if (!allowed) return null;
		return uploadFileRepository.findById(fileId).map(UploadFileDto::new).orElse(null);
	}
}
