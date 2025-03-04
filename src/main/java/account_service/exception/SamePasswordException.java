package account_service.exception;

public class SamePasswordException extends RuntimeException {

    public SamePasswordException() {
        super("The passwords must be different!");
    }

    public SamePasswordException(String message) {
        super(message);
    }
}
