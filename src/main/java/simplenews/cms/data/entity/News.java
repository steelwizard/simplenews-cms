package simplenews.cms.data.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "news_content")
@DiscriminatorValue("News")
public class News extends Content {
	// All fields are inherited from Content.
	// This table holds only the PK (FK to content).
}
