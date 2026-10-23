package kh.edu.istad.platform.order.domain.exception;

public class ServiceUnavailableException extends RuntimeException{
    private final String serviceName;

    public ServiceUnavailableException(String serviceName,Throwable cause){
        super(serviceName+ " is currently unavailable", cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
