package kh.edu.istad.platform.order.restapi.exception;

import kh.edu.istad.platform.order.client.exception.DownStreamServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DownStreamServiceException.class)
    public ResponseEntity<?> handler(DownStreamServiceException ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        Map.of(
                                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "message", ex.getMessage() != null ? ex.getMessage() : "Downstream service error",
                                "service", "customer-service"
                        )
                );

    }

}
