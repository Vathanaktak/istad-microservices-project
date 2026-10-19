package kh.edu.istad.platform.customer.persistence.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatusAggregationDto {
    @Id
    private String id;
    private String status;
    private long total;

    public String getResolvedStatus() {
        if (status != null && !status.isBlank()) {
            return status;
        }
        return id;
    }
}
