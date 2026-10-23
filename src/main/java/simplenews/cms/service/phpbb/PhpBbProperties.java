package simplenews.cms.service.phpbb;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cms.phpbb")
public class PhpBbProperties {

	private boolean enabled;
	private String baseUrl = "";
	private String apiUrl = "";
	private String apiKey = "";
	private String webhookKey = "";
	private long forumId;
	/**
	 * Public address of this site. The opening forum post links back here.
	 */
	private String siteUrl = "http://localhost:8080";

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getWebhookKey() {
		return webhookKey;
	}

	public void setWebhookKey(String webhookKey) {
		this.webhookKey = webhookKey;
	}

	public long getForumId() {
		return forumId;
	}

	public void setForumId(long forumId) {
		this.forumId = forumId;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public String publicBaseUrl() {
		return stripTrailingSlash(baseUrl);
	}

	public String publicSiteUrl() {
		return stripTrailingSlash(siteUrl);
	}

	private static String stripTrailingSlash(String value) {
		if (value == null) {
			return "";
		}
		String trimmed = value.trim();
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		return trimmed;
	}
}
