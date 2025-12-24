package simplenews.cms.data.dto;

import simplenews.cms.data.entity.SiteSetting;

public class SiteSettingDto {
	public SiteSettingDto() {}
	public SiteSettingDto(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public SiteSettingDto(SiteSetting setting) {
		this.key = setting.getKey();
		this.value = setting.getValue();
	}

	String key;

	String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
