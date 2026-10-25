package kh.edu.istad.platform.order.restapi.controller;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.order.domain.exception.ServiceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class OrderGlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex){
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Order Domain Error");
        return problem;
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailable(ServiceUnavailableException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Dependent Service Unavailable");
        problem.setProperty("service", ex.getServiceName());
        if (ex.getCause() != null) {
            problem.setProperty("cause", ex.getCause().getClass().getSimpleName() + ": " + ex.getCause().getMessage());
        }
        return problem;
    }

}
