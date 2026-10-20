package kh.edu.istad.platform.customer.domain.dto;

import java.util.List;

public record CustomerStatisticsResult(
        long totalCustomers,
        List<StatusCount> byStatus,
        List<SegmentCount> bySegment,
        List<StatusSegmentCount> byStatusAndSegment
) {
    public record StatusCount(
            String status,
            long total
    ) {}

    public record SegmentCount(
            String segmentId,
            String segmentName,
            long total
    ) {}

    public record StatusSegmentCount(
            String status,
            String segmentId,
            String segmentName,
            long total
    ) {}
}
