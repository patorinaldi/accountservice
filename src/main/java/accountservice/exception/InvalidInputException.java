package accountservice.exception;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException() {
        super("Invalid period input");
    }
    public InvalidInputException(String message) {
        super(message);
    }
}
