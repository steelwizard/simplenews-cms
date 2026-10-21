package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * The single site-wide click total. The row is created on first use, together with its secret.
 */
@Entity
@Table(name = "click_counter")
public class ClickCounter {

	public static final long ROW_ID = 1L;

	@Id
	private Long id;

	@Column(nullable = false)
	private long total;

	@Column(nullable = false, length = 64)
	private String secret;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
}
