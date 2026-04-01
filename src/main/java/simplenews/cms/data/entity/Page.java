package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.sql.Timestamp;

public class Page {
	@Id
	@Column
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column(name = "title")
	private String title;

	@Column(name = "key")
	private String key;

	@Column(name = "preview")
	private String preview;

	@Column(name = "content")
	private String content;

//	@ManyToOne
//	@JoinColumn(name = "category")
//	private Category category;

	@Column(name = "creator")
	private String creator;

	@Column(name = "last_modified")
	private Timestamp lastModified;

	@Column(name = "created")
	private Timestamp created;

	@Column(name = "visible")
	private Boolean visible;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="parent",referencedColumnName = "id")
	private Page parent;


}
