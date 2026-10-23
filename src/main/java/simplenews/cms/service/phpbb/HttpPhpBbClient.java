package simplenews.cms.service.phpbb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HttpPhpBbClient implements PhpBbClient {

	private static final Pattern TOPIC_ID = Pattern.compile("\"topicId\"\\s*:\\s*(\\d+)");

	private final HttpClient http = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.build();

	@Autowired
	PhpBbProperties properties;

	@Override
	public long createTopic(long newsId, String title, String permalink, long forumId) {
		String body = "{\"newsId\":" + newsId
				+ ",\"title\":" + quote(title)
				+ ",\"permalink\":" + quote(permalink)
				+ ",\"forumId\":" + forumId + "}";
		String response = send("/topics", body);
		Matcher matcher = TOPIC_ID.matcher(response);
		if (!matcher.find()) {
			throw new PhpBbException("phpBB did not return a topic id");
		}
		long topicId = Long.parseLong(matcher.group(1));
		if (topicId <= 0) {
			throw new PhpBbException("phpBB did not return a topic id");
		}
		return topicId;
	}

	@Override
	public void updateTopic(long newsId, String title, String permalink, boolean locked) {
		String body = "{\"title\":" + quote(title)
				+ ",\"permalink\":" + quote(permalink)
				+ ",\"locked\":" + locked + "}";
		send("/topics/" + newsId, body);
	}

	private String send(String path, String body) {
		String apiUrl = properties.getApiUrl() == null ? "" : properties.getApiUrl().trim();
		while (apiUrl.endsWith("/")) {
			apiUrl = apiUrl.substring(0, apiUrl.length() - 1);
		}
		if (apiUrl.isEmpty()) {
			throw new PhpBbException("cms.phpbb.api-url is not set");
		}
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(apiUrl + path))
					.timeout(Duration.ofSeconds(10))
					.header("Authorization", "Bearer " + (properties.getApiKey() == null ? "" : properties.getApiKey()))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(body))
					.build();
			HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() / 100 != 2) {
				throw new PhpBbException("phpBB returned " + response.statusCode());
			}
			return response.body() == null ? "" : response.body();
		} catch (PhpBbException ex) {
			throw ex;
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new PhpBbException("phpBB request was interrupted", ex);
		} catch (IOException | RuntimeException ex) {
			throw new PhpBbException("phpBB request failed", ex);
		}
	}

	private static String quote(String value) {
		if (value == null) {
			return "\"\"";
		}
		StringBuilder out = new StringBuilder(value.length() + 2);
		out.append('"');
		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			switch (ch) {
				case '\\', '"' -> out.append('\\').append(ch);
				case '\n' -> out.append("\\n");
				case '\r' -> out.append("\\r");
				case '\t' -> out.append("\\t");
				default -> {
					if (ch < 0x20) {
						out.append(String.format("\\u%04x", (int) ch));
					} else {
						out.append(ch);
					}
				}
			}
		}
		out.append('"');
		return out.toString();
	}
}
