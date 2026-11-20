package hexlet.code.app.exception;

public class EntityAlreadyAssignedToTaskException extends RuntimeException {
    public EntityAlreadyAssignedToTaskException(String message) {
        super(message);
    }
}
