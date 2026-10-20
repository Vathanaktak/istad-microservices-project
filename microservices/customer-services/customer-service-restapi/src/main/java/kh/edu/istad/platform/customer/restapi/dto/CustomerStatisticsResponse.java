package kh.edu.istad.platform.customer.restapi.dto;

import java.util.List;

public record CustomerStatisticsResponse(
        long totalCustomers,
        List<StatusCountResponse> byStatus,
        List<SegmentCountResponse> bySegment,
        List<StatusSegmentCountResponse> byStatusAndSegment
) {
    public record StatusCountResponse(
            String status,
            long total
    ) {}

    public record SegmentCountResponse(
            String segmentId,
            String segmentName,
            long total
    ) {}

    public record StatusSegmentCountResponse(
            String status,
            String segmentId,
            String segmentName,
            long total
    ) {}
}
