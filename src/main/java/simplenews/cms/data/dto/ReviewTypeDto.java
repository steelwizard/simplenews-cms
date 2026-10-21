package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ReviewType;

public class ReviewTypeDto {
	private Long id;
	private String key;
	private String title;

	public ReviewTypeDto() {}

	public ReviewTypeDto(ReviewType type) {
		this.id = type.getId();
		this.key = type.getKey();
		this.title = type.getTitle();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
