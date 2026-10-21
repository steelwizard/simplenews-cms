package simplenews.cms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import simplenews.cms.service.IClickCounterService;

@Controller
public class ClickCounterController {

	@Autowired
	IClickCounterService clickCounterService;

	@GetMapping("/clicks/{secret:[A-Za-z0-9_\\-]+}")
	public ResponseEntity<String> show(@PathVariable String secret) {
		Long total = clickCounterService.totalForSecret(secret);
		if (total == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(Long.toString(total));
	}
}
