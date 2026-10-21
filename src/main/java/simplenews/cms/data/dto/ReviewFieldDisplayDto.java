package simplenews.cms.data.dto;

/**
 * One review field as shown to visitors. href points to the filtered review list
 * for filterable fields, or to the stored address for URL fields.
 */
public class ReviewFieldDisplayDto {
	private final String label;
	private final String value;
	private final String href;
	private final boolean external;
	private final Integer barPercent;

	public ReviewFieldDisplayDto(String label, String value, String href, boolean external) {
		this(label, value, href, external, null);
	}

	public ReviewFieldDisplayDto(String label, String value, String href, boolean external, Integer barPercent) {
		this.label = label;
		this.value = value;
		this.href = href;
		this.external = external;
		this.barPercent = barPercent;
	}

	public String getLabel() {
		return label;
	}

	public String getValue() {
		return value;
	}

	public String getHref() {
		return href;
	}

	public boolean isExternal() {
		return external;
	}

	public Integer getBarPercent() {
		return barPercent;
	}
}
