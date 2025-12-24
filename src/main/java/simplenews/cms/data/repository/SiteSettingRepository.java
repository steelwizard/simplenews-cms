package simplenews.cms.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import simplenews.cms.data.entity.SiteSetting;

public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}
