package account_service.exception;

public class UserExistsException extends RuntimeException{
    public UserExistsException() {
        super("User exist!");
    }

    public UserExistsException(String message) {
        super(message);
    }
}
