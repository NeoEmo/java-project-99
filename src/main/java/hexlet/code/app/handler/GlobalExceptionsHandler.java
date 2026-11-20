package hexlet.code.app.handler;

import hexlet.code.app.exception.ResourceAlreadyExistException;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.exception.EntityAlreadyAssignedToTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionsHandler {
	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionsHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex) {

		var errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.toList();

		LOG.warn("Validation failed: {}", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid request data",
				"details", errors.toString()));
	}

	@ExceptionHandler(ResourceAlreadyExistException.class)
	public ResponseEntity<Map<String, String>> handleResourceAlreadyExistException(
			ResourceAlreadyExistException ex) {

		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<Map<String, String>> handleAuthenticationException(
			AuthenticationException ex) {

		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid email or password"));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, String>> handleAccessDeniedException(
			AccessDeniedException ex) {

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
	}

	@ExceptionHandler(EntityAlreadyAssignedToTaskException.class)
	public ResponseEntity<Map<String, String>> handleUserAlreadyAssignedToTaskException(
			EntityAlreadyAssignedToTaskException ex
	) {

		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
	}
}
