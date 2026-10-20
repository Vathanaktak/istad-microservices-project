package kh.edu.istad.common.dto;

import java.time.Instant;

public record ApiResponse<T>(
        Instant timestamp,
        int status,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(String message,T data){
        return new ApiResponse<>(Instant.now(),
                200,
                message,
                data
                );
    }
    public static <T> ApiResponse<T> ok(String message){
        return new ApiResponse<>(
                Instant.now(),
                200,
                message,
                null
        );
    }
    public static <T> ApiResponse<T> create(String message,T data){
        return new ApiResponse<>(
                Instant.now(),
                201,
                message,
                data
        );
    }
}
