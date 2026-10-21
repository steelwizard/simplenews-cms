package simplenews.cms.service.util;

import org.springframework.http.MediaType;
import simplenews.cms.data.dto.UploadFileDto;

import java.util.Locale;

public class FileUtil {

	public enum Kind {
		IMAGE, VIDEO, AUDIO, FILE
	}

	public static String getFileExtension(String filename) {
		if (filename == null) return "";
		int dot = filename.lastIndexOf('.');
		return dot < 0 ? "" : filename.substring(dot + 1);
	}

	public static String getFileHash(byte[] digest) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < digest.length; i++) {
			if ((0xff & digest[i]) < 0x10) {
				sb.append("0").append(Integer.toHexString((0xFF & digest[i])));
			} else {
				sb.append(Integer.toHexString(0xFF & digest[i]));
			}
		}
		return sb.toString();
	}

	public static boolean isImage(UploadFileDto file) {
		return kind(file.getExtension()) == Kind.IMAGE;
	}

	public static Kind kind(String ext) {
		MediaType type = getMediaType(ext);
		return switch (type.getType()) {
			case "image" -> Kind.IMAGE;
			case "video" -> Kind.VIDEO;
			case "audio" -> Kind.AUDIO;
			default -> Kind.FILE;
		};
	}

	public static MediaType getMediaType(String ext) {
		if (ext == null) return MediaType.APPLICATION_OCTET_STREAM;
		return switch (ext.toLowerCase(Locale.ROOT)) {
			case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
			case "png" -> MediaType.IMAGE_PNG;
			case "gif" -> MediaType.IMAGE_GIF;
			case "webp" -> MediaType.parseMediaType("image/webp");
			case "bmp" -> MediaType.parseMediaType("image/bmp");
			case "mp4" -> MediaType.parseMediaType("video/mp4");
			case "webm" -> MediaType.parseMediaType("video/webm");
			case "ogv" -> MediaType.parseMediaType("video/ogg");
			case "mov" -> MediaType.parseMediaType("video/quicktime");
			case "mp3" -> MediaType.parseMediaType("audio/mpeg");
			case "wav" -> MediaType.parseMediaType("audio/wav");
			case "ogg" -> MediaType.parseMediaType("audio/ogg");
			case "m4a" -> MediaType.parseMediaType("audio/mp4");
			case "aac" -> MediaType.parseMediaType("audio/aac");
			case "flac" -> MediaType.parseMediaType("audio/flac");
			default -> MediaType.APPLICATION_OCTET_STREAM;
		};
	}

	public static String shorthash(String hash) {
		return hash.substring(0, Math.min(hash.length(), 8));
	}
}
