package simplenews.cms.service.exception;

public class UserAlreadyExistsException extends Exception{
	public UserAlreadyExistsException(String username) {
		super("Username "+username+" already exists");
	}
}
