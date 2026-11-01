package kh.edu.istad.common.webflux.dto;

public record ErrorApiResponse(
        Integer statusCode,
        String message,
        String details
) {
}
