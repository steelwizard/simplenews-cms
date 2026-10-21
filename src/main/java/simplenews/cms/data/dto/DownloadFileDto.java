package simplenews.cms.data.dto;

import simplenews.cms.data.entity.UploadFile;
import simplenews.cms.service.util.FileUtil;

public class DownloadFileDto {
	private final Long id;
	private final String filename;
	private final String url;
	private final String src;
	private final FileUtil.Kind kind;

	public DownloadFileDto(UploadFile file) {
		UploadFileDto dto = new UploadFileDto(file);
		this.id = dto.getId();
		this.filename = dto.getFilename();
		this.url = "/download/" + dto.getId();
		this.src = "/media/" + dto.getLink();
		this.kind = FileUtil.kind(dto.getExtension());
	}

	public Long getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}

	public String getUrl() {
		return url;
	}

	public String getSrc() {
		return src;
	}

	public FileUtil.Kind getKind() {
		return kind;
	}

	public boolean isImage() {
		return kind == FileUtil.Kind.IMAGE;
	}

	public boolean isVideo() {
		return kind == FileUtil.Kind.VIDEO;
	}

	public boolean isAudio() {
		return kind == FileUtil.Kind.AUDIO;
	}

	public boolean isFile() {
		return kind == FileUtil.Kind.FILE;
	}
}
