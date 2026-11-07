package kh.edu.istad.platform.iam.domain.exception;

public class NotFoundException extends DomainException {
    public NotFoundException(String code, String message) { super(code, message); }
    public static NotFoundException of(String type, Object id) {
        return new NotFoundException(type.toLowerCase() + "-not-found", type + " not found: " + id);
    }
}
