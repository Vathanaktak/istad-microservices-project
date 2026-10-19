package kh.edu.istad.platform.customer.persistence.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "customer_segments")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerSegmentDocument {
    @Id
    private String id;
    private String name;
    private String description;
}
