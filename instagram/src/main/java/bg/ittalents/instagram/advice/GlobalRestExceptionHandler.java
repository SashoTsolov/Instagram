package bg.ittalents.instagram.advice;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {
    private record ErrorDTO(Object message, int status, LocalDateTime time) {
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(e.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(e.getMessage(),
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e) {
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(e.getMessage(),
                HttpStatus.CONFLICT.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception e) {
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        final Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            final String fieldName = ((FieldError) error).getField();
            final String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);

        });
        final ErrorDTO ERROR_MESSAGE = new ErrorDTO(errors, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        return new ResponseEntity<>(ERROR_MESSAGE, HttpStatus.BAD_REQUEST);
    }
}
