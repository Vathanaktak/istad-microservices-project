package kh.edu.istad.platform.iam.domain.exception;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String code, String message) { super(code, message); }
}
