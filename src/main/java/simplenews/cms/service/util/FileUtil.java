package simplenews.cms.service.util;

import org.springframework.http.MediaType;
import simplenews.cms.data.dto.UploadFileDto;


public class FileUtil {
	public static String getFileExtension(String filename) {
		return filename.substring(filename.lastIndexOf(".") + 1);
		//TODO: maybe FileTypeDetector?
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
		MediaType type = getMediaType(file.getExtension());
		return type.equals(MediaType.IMAGE_JPEG)
				|| type.equals(MediaType.IMAGE_PNG)
				|| type.equals(MediaType.IMAGE_GIF);
	}

	public static MediaType getMediaType(String ext) {
		switch (ext) {
			case "jpg":
			case "jpeg":
				return MediaType.IMAGE_JPEG;
			case "png":
				return MediaType.IMAGE_PNG;
			case "gif":
				return MediaType.IMAGE_GIF;
			default:
				return MediaType.APPLICATION_OCTET_STREAM;
		}
	}
}
