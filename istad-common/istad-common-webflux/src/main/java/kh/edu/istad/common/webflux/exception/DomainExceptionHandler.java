package kh.edu.istad.common.webflux.exception;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.webflux.dto.ErrorApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorApiResponse> exception(DomainException exception){
        HttpStatus status = (exception.getMessage() != null && exception.getMessage().toLowerCase().contains("not found"))
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(new ErrorApiResponse(
                        status.value(),
                        exception.getMessage(),
                        status.getReasonPhrase()
                ));
    }

}
