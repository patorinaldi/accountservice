package account_service.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException() {super("User not found!");}
    public NotFoundException(String message) {
        super(message);
    }
}
