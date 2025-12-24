package simplenews.cms.service;

import org.springframework.cache.annotation.Cacheable;
import simplenews.cms.data.dto.SiteSettingDto;

import java.util.Map;

public interface ISiteSettingService {
	@Cacheable("siteSettings")
	Map<String, String> getAllSettings();

	String getSiteTitle();

	String getSiteMetaKeywords();

	String getSiteMetaDescription();
}
