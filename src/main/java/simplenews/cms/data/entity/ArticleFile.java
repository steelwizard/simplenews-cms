package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

public class ArticleFile {

	@Id
	@Column
	Long id;

	@OneToOne(targetEntity = UploadFile.class)
	@Column
	UploadFile uploadFile;

	@Column
	String title;

	@Column
	String description;

	@Column
	Long articleId;

	@Column
	@Enumerated(EnumType.STRING)
	CategoryTypeEnum type;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UploadFile getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadFile uploadFile) {
		this.uploadFile = uploadFile;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getArticleId() {
		return articleId;
	}

	public void setArticleId(Long articleId) {
		this.articleId = articleId;
	}

	public CategoryTypeEnum getType() {
		return type;
	}

	public void setType(CategoryTypeEnum type) {
		this.type = type;
	}
}
