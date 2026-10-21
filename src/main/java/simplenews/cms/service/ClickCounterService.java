package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.dto.ClickCounterSnapshot;
import simplenews.cms.data.entity.ClickCounter;
import simplenews.cms.data.repository.ClickCounterRepository;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class ClickCounterService implements IClickCounterService {

	private static final SecureRandom RANDOM = new SecureRandom();

	@Autowired
	ClickCounterRepository clickCounterRepository;

	@Override
	@Transactional
	public void recordClick() {
		if (clickCounterRepository.increment(ClickCounter.ROW_ID) == 0) {
			clickCounterRepository.insertIfAbsent(ClickCounter.ROW_ID, newSecret());
			clickCounterRepository.increment(ClickCounter.ROW_ID);
		}
	}

	@Override
	@Transactional
	public ClickCounterSnapshot snapshot() {
		ClickCounter row = clickCounterRepository.findById(ClickCounter.ROW_ID).orElse(null);
		if (row == null) {
			clickCounterRepository.insertIfAbsent(ClickCounter.ROW_ID, newSecret());
			row = clickCounterRepository.findById(ClickCounter.ROW_ID).orElseThrow();
		}
		return new ClickCounterSnapshot(row.getTotal(), row.getSecret());
	}

	@Override
	@Transactional(readOnly = true)
	public Long totalForSecret(String secret) {
		ClickCounter row = clickCounterRepository.findById(ClickCounter.ROW_ID).orElse(null);
		if (row == null || !same(row.getSecret(), secret)) return null;
		return row.getTotal();
	}

	private static String newSecret() {
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static boolean same(String expected, String given) {
		if (expected == null || given == null) return false;
		return MessageDigest.isEqual(expected.getBytes(java.nio.charset.StandardCharsets.UTF_8),
				given.getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}
}
