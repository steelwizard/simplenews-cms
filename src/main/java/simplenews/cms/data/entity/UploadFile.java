package simplenews.cms.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
@Table(name = "upload_file")
public class UploadFile {

	@Id
	@Column
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@Column
	private String hash;

	@Column
	private String extension;

	@Column
	private String filename;

	@Column(name = "created")
	private Timestamp created;

	@Column(name = "creator")
	private String creator;

	public UploadFile() {}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UploadFile that = (UploadFile) o;
		return Objects.equals(id, that.id) && Objects.equals(hash, that.hash) && Objects.equals(extension, that.extension) && Objects.equals(filename, that.filename) && Objects.equals(created, that.created) && Objects.equals(creator, that.creator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, hash, extension, filename, created, creator);
	}

	@Override
	public String toString() {
		return new StringJoiner(", ", UploadFile.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("hash='" + hash + "'")
				.add("extension='" + extension + "'")
				.add("filename='" + filename + "'")
				.add("created=" + created)
				.add("creator='" + creator + "'")
				.toString();
	}
}
