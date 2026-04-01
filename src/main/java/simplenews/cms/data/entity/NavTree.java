package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "nav_tree")
public class NavTree {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	// Unique identifier used to look up a tree by name, e.g. "main", "footer"
	@Column(name = "handle", nullable = false, unique = true)
	private String handle;

	@Column(name = "title")
	private String title;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public String getHandle() { return handle; }
	public void setHandle(String handle) { this.handle = handle; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
}
