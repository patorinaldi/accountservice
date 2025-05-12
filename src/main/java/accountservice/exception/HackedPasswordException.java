package accountservice.exception;

public class HackedPasswordException extends RuntimeException{
    public HackedPasswordException() {
        super("The password is in the hacker's database!");
    }

    public HackedPasswordException(String message) {
        super(message);
    }
}