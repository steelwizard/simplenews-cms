package simplenews.cms.service.exception;

public class PasswordConfirmNotEqualException extends Exception{

	public PasswordConfirmNotEqualException() {
		super("Password doesn't match");
	}
}
