package simplenews.cms.service.phpbb;

public class PhpBbException extends RuntimeException {

	public PhpBbException(String message) {
		super(message);
	}

	public PhpBbException(String message, Throwable cause) {
		super(message, cause);
	}
}
