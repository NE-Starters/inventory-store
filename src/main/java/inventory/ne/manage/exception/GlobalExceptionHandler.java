package inventory.ne.manage.exception;

import inventory.ne.manage.dto.response.ApiResponse;
import inventory.ne.manage.util.LoggerUtil;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerUtil.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse> handleUnauthorizedException(UnauthorizedException ex) {
        logger.error("Unauthorized access: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Invalid argument: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ApiResponse(false, "An unexpected error occurred", null),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse> handleDuplicateEmailException(DuplicateEmailException ex) {
        logger.error("Duplicate email: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.error("Invalid credentials: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ApiResponse> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
        logger.error("Email not verified: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiResponse(false, ex.getMessage(), null),
                HttpStatus.FORBIDDEN);
    }
}