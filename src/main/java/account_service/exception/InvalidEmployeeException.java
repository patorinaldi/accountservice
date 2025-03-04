package account_service.exception;

public class InvalidEmployeeException extends RuntimeException {
    public InvalidEmployeeException() {
    super("Invalid Employee");
  }
    public InvalidEmployeeException(String message) {
        super(message);
    }
}