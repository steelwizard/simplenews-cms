package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.logging.Logger;

@Service
public class SiteSettingService implements ISiteSettingService {

	private static final Logger LOG = Logger.getLogger(SiteSettingService.class.getName());

	@Autowired
	private SiteSettingsInternalService settingsInternalService;

	@Override
	public Map<String,String> getAllSettings() {
		return settingsInternalService.getAllSettings();
	}

	@Override
	public String getSiteTitle() {
		return getAllSettings().get("site.title");
	}
	@Override
	public String getSiteMetaKeywords() {
		return getAllSettings().get("site.meta.keywods");
	}
	@Override
	public String getSiteMetaDescription() {
		return getAllSettings().get("site.meta.description");
	}

}
