package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "article_content")
@DiscriminatorValue("ArticleContent")
public class ArticleContent extends Content {

	// Ordering among siblings in the article tree.
	// Parent reference for the tree structure lives on Content.parent.
	@Column(name = "order_index")
	private Integer orderIndex;

	public Integer getOrderIndex() { return orderIndex; }
	public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
}
