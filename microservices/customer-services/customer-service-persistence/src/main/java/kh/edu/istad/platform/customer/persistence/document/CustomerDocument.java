package kh.edu.istad.platform.customer.persistence.document;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class CustomerDocument {
    @Id
    private CustomerId id;
    private final String username;
    private String familyName;
    private String givenName;
    private final String email;
    private final PhoneNumber phoneNumber;
    private CustomerStatus status;

}
