package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import simplenews.cms.data.entity.SiteSetting;
import simplenews.cms.data.repository.SiteSettingRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class SiteSettingsInternalService {
	// This class exists because the caching doesn't work if getAllSettings()
	// is called from within within the same class.

	private static final Logger LOG = Logger.getLogger(SiteSettingsInternalService.class.getName());

	@Autowired
	SiteSettingRepository siteSettingRepository;

	@Cacheable("siteSetting")
	public Map<String, String> getAllSettings() {
		LOG.info("Loading site settings");
		List<SiteSetting> settingsList = siteSettingRepository.findAll();
		Map<String, String> siteSettingMap = new HashMap<>();
		settingsList.forEach(s -> siteSettingMap.put(s.getKey(), s.getValue()));
		return siteSettingMap;
	}
}
