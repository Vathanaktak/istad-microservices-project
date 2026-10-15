package kh.edu.istad.platform.payment.restapi.controller;

import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PaymentGlobalExceptionHandler {

    @ExceptionHandler(PaymentDomainException.class)
    public ProblemDetail handlePaymentDomainException(
            PaymentDomainException exception
    ){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
        problemDetail.setTitle("Payment Domain Error");
        return problemDetail;
    }

}
