package kh.edu.istad.platform.customer.restapi.dto;

public record CustomerStatusReportResponse(
        String status,
        long total
) {
}
