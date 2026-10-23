package simplenews.cms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simplenews.cms.service.phpbb.PhpBbDiscussionService;
import simplenews.cms.service.phpbb.PhpBbProperties;
import simplenews.cms.service.phpbb.WebhookSecrets;

@RestController
@RequestMapping("/_private/phpbb")
public class PhpBbWebhookController {

	@Autowired
	PhpBbProperties properties;
	@Autowired
	PhpBbDiscussionService discussionService;

	@PostMapping("/topics/{topicId}/replies")
	public ResponseEntity<Void> replies(@PathVariable long topicId,
										@RequestHeader(value = "Authorization", required = false) String authorization,
										@RequestBody ReplyCount body) {
		if (!WebhookSecrets.bearerMatches(properties.getWebhookKey(), authorization)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		if (body == null || body.getReplyCount() < 0) {
			return ResponseEntity.badRequest().build();
		}
		boolean updated = discussionService.recordReplyCount(topicId, body.getReplyCount());
		if (!updated) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.noContent().build();
	}

	public static class ReplyCount {
		private int replyCount;

		public int getReplyCount() {
			return replyCount;
		}

		public void setReplyCount(int replyCount) {
			this.replyCount = replyCount;
		}
	}
}
