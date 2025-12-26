package simplenews.cms.data.dto;

import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import simplenews.cms.data.entity.UploadFile;
import simplenews.cms.service.util.FileUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.StringJoiner;

public class UploadFileDto {
	private Long id;
	private String hash;
	private String extension;
	private String filename;
	private Timestamp created;
	private String creator;

	public UploadFileDto() {}
	public UploadFileDto(Long id, String hash, String extension, String filename, Timestamp created, String creator) {
		this.id = id;
		this.hash = hash;
		this.extension = extension;
		this.filename = filename;
		this.created = created;
		this.creator = creator;
	}
	public UploadFileDto(UploadFile uploadFile) {
		this.id = uploadFile.getId();
		this.hash = uploadFile.getHash();
		this.extension = uploadFile.getExtension();
		this.filename = uploadFile.getFilename();
		this.created = uploadFile.getCreated();
		this.creator = uploadFile.getCreator();
	}

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
	public String toString() {
		return new StringJoiner(", ", UploadFileDto.class.getSimpleName() + "[", "]")
				.add("id=" + id)
				.add("hash='" + hash + "'")
				.add("extension='" + extension + "'")
				.add("filename='" + filename + "'")
				.add("created=" + created)
				.add("creator='" + creator + "'")
				.toString();
	}
	public String getLink() {
		String shortHash = FileUtil.shorthash(hash);
		Calendar cal = Calendar.getInstance();
		cal.setTime(created);

		Integer year = cal.get(Calendar.YEAR);
		Integer month = cal.get(Calendar.MONTH)+1;

		StringBuilder path = new StringBuilder(year.toString());
		path.append("/").append(month.toString())
				.append("/").append(shortHash)
				.append("/").append(filename);
		return path.toString();
	}
}
