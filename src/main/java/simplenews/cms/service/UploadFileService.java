package simplenews.cms.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import simplenews.cms.data.dto.UploadFileDto;
import simplenews.cms.data.dto.UserDto;
import simplenews.cms.data.entity.UploadFile;
import simplenews.cms.data.repository.UploadFileRepository;
import simplenews.cms.security.RoleEnum;
import simplenews.cms.service.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UploadFileService implements IUploadFileService {

	public static final String DEFAULT_UPLOAD_PATH = "/opt/cms/";
	@Autowired
	private Environment env;
	@Autowired
	private IUserService userService;
	@Autowired
	private IPermissionService permissionService;

	String uploadPath = null;

	private static final Logger LOG = Logger.getLogger(UploadFileService.class.getName());

	@Autowired
	private UploadFileRepository uploadFileRepository;


	public List<UploadFileDto> getUploadFiles(int limit, int offset) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		UserDto user = userService.getByUsername(username);
		if (userService.checkRoleAtLeast(RoleEnum.EDITOR, user.getRole())) {
			return uploadFileRepository.getUploadFilesPaginated(limit, offset)
					.stream()
					.map(f -> new UploadFileDto(f))
					.collect(Collectors.toList());
		} else {
			return uploadFileRepository.getUploadFilesPaginatedForUser(limit, offset, user.getUsername())
					.stream()
					.map(f -> new UploadFileDto(f))
					.collect(Collectors.toList());
		}

	}
	@Override
	public UploadFileDto getById(Long id) {
		return new UploadFileDto(uploadFileRepository.findById(id).get());
	}

	public UploadFileDto getUploadFile(Long id) {
		Optional<UploadFile> uploadFileOptional = uploadFileRepository.findById(id);
		UploadFileDto uploadFile = null;
		if(uploadFileOptional.isPresent()) {
			uploadFile = new UploadFileDto(uploadFileOptional.get());
		}
		return uploadFile;
	}
	public UploadFile upload(MultipartFile upload, String user) {
		//TODO: check permission
		MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance("SHA-256");
			String originalFilename = upload.getOriginalFilename();
			String extension = FileUtil.getFileExtension(originalFilename);
			byte[] hashBytes = messageDigest.digest(upload.getBytes());
			String hash = FileUtil.getFileHash(hashBytes);

			String filename = originalFilename;
			Path uploadPath = Paths.get(getCurrentUploadFilePath(hash),filename);
			if (Files.exists(uploadPath)) {
				throw new RuntimeException("File already exists");
			}
			Files.write(uploadPath, upload.getBytes());

			UploadFile uploadFile = new UploadFile();
			uploadFile.setFilename(originalFilename);
			uploadFile.setExtension(extension);
			uploadFile.setHash(hash);
			uploadFile.setCreated(new Timestamp(System.currentTimeMillis()));
			uploadFile.setCreator(user);

			return uploadFileRepository.save(uploadFile);

		} catch (NoSuchAlgorithmException e) {
			LOG.log(Level.SEVERE, "Message disgest failed" ,e);
			return null;
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Couldn't read file "+upload.getOriginalFilename() ,e);
			return null;
		}
	}

	public File getFile(String filename) {
		File file = new File(getBasePath(), filename);
		if (file == null || !file.exists()) {
			LOG.log(Level.FINE, filename+" File doesn't exist");
			return null;
		} else if (!file.isFile()) {
			LOG.log(Level.FINE, filename+" Not a file");
			return null;
		}
		return file;
	}

	private String getBasePath() {
		if (uploadPath == null) {
			String envProp = env.getProperty("upload.file.basePath");
			if (envProp == null || envProp.isEmpty()) uploadPath = DEFAULT_UPLOAD_PATH;
			uploadPath = envProp;
		}
		Path path = Paths.get(uploadPath, "uploads");
		return path.toString();
	}

	private String getCurrentUploadFilePath(String hash) {
		Calendar cal = Calendar.getInstance();
		Integer year = cal.get(Calendar.YEAR);
		Integer month = cal.get(Calendar.MONTH)+1;
		Path path;
		String shortHash;
		if (hash != null) {
			shortHash = hash.substring(0, Math.min(hash.length(), 8));
			path = Paths.get(getBasePath(),year.toString(),month.toString(),shortHash);
		} else {
			path = Paths.get(getBasePath(),year.toString(),month.toString());
		}

		if (!Files.exists(path)) {
			try {
				Files.createDirectories(path);
			} catch (IOException e) {
				LOG.info("IOException occurred trying to create upload subdirectory.");
				e.printStackTrace();
			}
		}
		return path.toString();
	}

	@Override
	@Transactional
	public boolean deleteById(Long id) {
		UploadFile file = uploadFileRepository.findById(id).orElseThrow();
		//Check permission

		Calendar cal = Calendar.getInstance();
		cal.setTime(file.getCreated());
		Integer year = cal.get(Calendar.YEAR);
		Integer month = cal.get(Calendar.MONTH)+1;

		Path path = Paths.get(getBasePath(), year.toString(), month.toString(),
				FileUtil.shorthash(file.getHash()),file.getFilename());

		if (Files.exists(path) && !Files.isDirectory(path)) {
			try {
				Files.delete(path);
				uploadFileRepository.deleteById(id);
				return true;
			} catch (IOException e) {
				LOG.severe(e.toString());
			}
		}
		return false;
	}

}
