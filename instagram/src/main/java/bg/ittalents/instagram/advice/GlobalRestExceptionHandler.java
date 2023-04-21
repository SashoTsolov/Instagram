package bg.ittalents.instagram.advice;

import bg.ittalents.instagram.exception.BadRequestException;
import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.exception.UserAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalRestExceptionHandler extends ResponseEntityExceptionHandler {
    private record ErrorDTO(Object message, int status, LocalDateTime time) { }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException e) {
        final ErrorDTO errorDTO = new ErrorDTO(e.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        e.printStackTrace(); // TODO
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException e) {
        final ErrorDTO errorDTO = new ErrorDTO(e.getMessage(),
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now());
        e.printStackTrace(); // TODO
        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException e) {
        final ErrorDTO errorDTO = new ErrorDTO(e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
        e.printStackTrace(); // TODO
        return new ResponseEntity<>(errorDTO, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        final ErrorDTO errorDTO = new ErrorDTO(e.getMessage(),
                HttpStatus.CONFLICT.value(), LocalDateTime.now());
        e.printStackTrace(); // TODO
        return new ResponseEntity<>(errorDTO, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllOtherExceptions(Exception e) {
        final ErrorDTO errorDTO = new ErrorDTO(e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
        e.printStackTrace(); // TODO
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        final Map<String, String> errors = new HashMap<>();
        e.getBindingResult()
                .getAllErrors()
                .forEach((error) -> {
                    final String fieldName = ((FieldError) error).getField();
                    final String message = error.getDefaultMessage();
                    errors.put(fieldName, message);
                });
        e.printStackTrace(); // TODO
        final ErrorDTO errorDTO = new ErrorDTO(errors, HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }
}
