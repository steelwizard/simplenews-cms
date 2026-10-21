package simplenews.cms.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import simplenews.cms.data.dto.ClickCounterSnapshot;
import simplenews.cms.data.entity.ClickCounter;
import simplenews.cms.data.repository.ClickCounterRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClickCounterServiceTest {

	private ClickCounterRepository counters;
	private ClickCounterService service;

	@BeforeEach
	void setUp() {
		counters = mock(ClickCounterRepository.class);
		service = new ClickCounterService();
		ReflectionTestUtils.setField(service, "clickCounterRepository", counters);
	}

	// A click on an existing counter adds one and does not replace the secret.
	@Test
	void clickIncrementsTheExistingTotal() {
		when(counters.increment(ClickCounter.ROW_ID)).thenReturn(1);

		service.recordClick();

		verify(counters, never()).insertIfAbsent(eq(ClickCounter.ROW_ID), anyString());
	}

	// The first click creates the counter, then counts itself.
	@Test
	void firstClickCreatesTheCounter() {
		when(counters.increment(ClickCounter.ROW_ID)).thenReturn(0, 1);

		service.recordClick();

		verify(counters).insertIfAbsent(eq(ClickCounter.ROW_ID), anyString());
		verify(counters, times(2)).increment(ClickCounter.ROW_ID);
	}

	// The secret shown in the admin panel reads back the stored total.
	@Test
	void matchingSecretShowsTheTotal() {
		ClickCounter row = row("abc", 12);
		when(counters.findById(ClickCounter.ROW_ID)).thenReturn(Optional.of(row));

		assertEquals(12L, service.totalForSecret("abc"));
		assertNull(service.totalForSecret("nope"));
		assertNull(service.totalForSecret(null));
	}

	// Opening the admin panel creates the secret when the counter does not exist yet.
	@Test
	void snapshotCreatesASecret() {
		ClickCounter created = new ClickCounter();
		when(counters.findById(ClickCounter.ROW_ID)).thenReturn(Optional.empty(), Optional.of(created));
		when(counters.insertIfAbsent(eq(ClickCounter.ROW_ID), anyString())).thenAnswer(invocation -> {
			created.setId(ClickCounter.ROW_ID);
			created.setTotal(0);
			created.setSecret(invocation.getArgument(1));
			return 1;
		});

		ClickCounterSnapshot snapshot = service.snapshot();

		assertEquals(0, snapshot.total());
		assertEquals(created.getSecret(), snapshot.secret());
	}

	private static ClickCounter row(String secret, long total) {
		ClickCounter row = new ClickCounter();
		row.setId(ClickCounter.ROW_ID);
		row.setSecret(secret);
		row.setTotal(total);
		return row;
	}
}
