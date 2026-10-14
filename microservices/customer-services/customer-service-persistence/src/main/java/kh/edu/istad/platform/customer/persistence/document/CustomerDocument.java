package kh.edu.istad.platform.customer.persistence.document;

import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Document(collection = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDocument {
    @Id
    private UUID id;
    private String username;
    private String familyName;
    private String givenName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;
}
