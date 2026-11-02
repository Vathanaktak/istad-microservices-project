package kh.edu.istad.platform.order.client.exception;

import lombok.Getter;

@Getter
public class DownStreamServiceException
        extends RuntimeException {
    private String service;
    public DownStreamServiceException(String message) {
        super(message);
    }

    public DownStreamServiceException(String message, String service) {
        super(message);
        this.service = service;
    }

}
