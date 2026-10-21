package simplenews.cms.service.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import simplenews.cms.data.dto.UploadFileDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileUtilTest {

	// A missing name has no extension.
	@Test
	void missingFilenameHasNoExtension() {
		assertEquals("", FileUtil.getFileExtension(null));
		assertEquals("", FileUtil.getFileExtension("readme"));
	}

	// Only the part after the last dot counts as the extension.
	@Test
	void extensionIsThePartAfterTheLastDot() {
		assertEquals("gz", FileUtil.getFileExtension("notes.tar.gz"));
	}

	// The file hash is written as readable hexadecimal text.
	@Test
	void hashBytesBecomeHex() {
		assertEquals("0a10ff", FileUtil.getFileHash(new byte[] {10, 16, (byte) 255}));
	}

	// A short hash is not padded, and a long one keeps only the first eight characters.
	@Test
	void shortHashKeepsAtMostEightCharacters() {
		assertEquals("abc", FileUtil.shorthash("abc"));
		assertEquals("12345678", FileUtil.shorthash("1234567890abcdef"));
	}

	// Pictures, video, sound, and everything else are told apart by the extension.
	@Test
	void extensionDecidesTheKindOfFile() {
		assertEquals(FileUtil.Kind.IMAGE, FileUtil.kind("JPG"));
		assertEquals(FileUtil.Kind.VIDEO, FileUtil.kind("mp4"));
		assertEquals(FileUtil.Kind.AUDIO, FileUtil.kind("mp3"));
		assertEquals(FileUtil.Kind.FILE, FileUtil.kind("zip"));
		assertEquals(FileUtil.Kind.FILE, FileUtil.kind(null));
	}

	// Known picture types get an image content type, and unknown types get a generic one.
	@Test
	void mediaTypeMatchesTheExtension() {
		assertEquals(MediaType.IMAGE_PNG, FileUtil.getMediaType("png"));
		assertEquals(MediaType.APPLICATION_OCTET_STREAM, FileUtil.getMediaType("exe"));
	}

	// An uploaded picture is recognized from its extension.
	@Test
	void imageCheckUsesTheExtension() {
		UploadFileDto picture = new UploadFileDto();
		picture.setExtension("png");
		UploadFileDto zip = new UploadFileDto();
		zip.setExtension("zip");

		assertTrue(FileUtil.isImage(picture));
		assertFalse(FileUtil.isImage(zip));
	}
}
