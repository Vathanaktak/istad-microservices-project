package kh.edu.istad.platform.iam.rest.error;

import kh.edu.istad.platform.iam.domain.exception.*;
import java.util.List;
import java.util.Map;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "kh.edu.istad.platform.iam.rest")
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = e.getBindingResult().getAllErrors().stream().map(err -> {
            String field = err instanceof FieldError f ? f.getField() : err.getObjectName();
            return Map.of("field", field, "message", String.valueOf(err.getDefaultMessage()));
        }).toList();
        ProblemDetail p = Problems.of(HttpStatus.BAD_REQUEST, "validation-error", "Validation failed", "One or more fields are invalid");
        p.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(p);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ProblemDetail> badRequest(Exception e) {
        return ResponseEntity.badRequest().body(Problems.of(HttpStatus.BAD_REQUEST, "malformed-request", "Malformed request", "Request body or parameter is invalid"));
    }
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(NotFoundException e) {
        return ResponseEntity.status(404).body(Problems.of(HttpStatus.NOT_FOUND, e.getCode(), "Resource not found", e.getMessage()));
    }
    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> conflict(ConflictException e) {
        return ResponseEntity.status(409).body(Problems.of(HttpStatus.CONFLICT, e.getCode(), "Conflict", e.getMessage()));
    }
    @ExceptionHandler(BusinessRuleException.class)
    ResponseEntity<ProblemDetail> rule(BusinessRuleException e) {
        return ResponseEntity.status(422).body(Problems.of(HttpStatus.UNPROCESSABLE_ENTITY, e.getCode(), "Business rule violated", e.getMessage()));
    }
    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> locked(OptimisticLockingFailureException e) {
        return ResponseEntity.status(409).body(Problems.of(HttpStatus.CONFLICT, "concurrent-update", "Concurrent update", "The resource was modified by someone else; reload and retry"));
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> denied(AccessDeniedException e) {
        return ResponseEntity.status(403).body(Problems.of(HttpStatus.FORBIDDEN, "forbidden", "Forbidden", "You do not have permission to perform this action"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception e) {
        return ResponseEntity.status(500).body(Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "Internal error", "Unexpected error"));
    }
}
