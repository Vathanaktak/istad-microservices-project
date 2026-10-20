package kh.edu.istad.platform.customer.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusSegmentAggregationDto {
    @Id
    private GroupId id;
    private long total;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupId {
        private String status;
        private String segment;
    }
}
