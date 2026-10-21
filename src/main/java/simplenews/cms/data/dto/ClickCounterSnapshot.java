package simplenews.cms.data.dto;

/**
 * The click total and the secret that opens a page showing that total.
 */
public record ClickCounterSnapshot(long total, String secret) {
}
