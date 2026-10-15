package kh.edu.istad.platform.payment.domain.exception;

import kh.edu.istad.common.exception.DomainException;

public class PaymentDomainException extends DomainException {


    public PaymentDomainException(String message) {
        super(message);
    }

    public PaymentDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
