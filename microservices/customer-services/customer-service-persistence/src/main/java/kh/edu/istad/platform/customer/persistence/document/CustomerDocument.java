package kh.edu.istad.platform.customer.persistence.document;

import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDocument implements Persistable<String> {
    @Id
    private String id;
    private String username;
    private String familyName;
    private String givenName;
    private String email;
    private String phoneNumber;
    private CustomerStatus status;

    @CreatedBy
    private String createBy;
    @CreatedDate
    private Instant createAt;
    @LastModifiedBy
    private String modifyBy;
    @LastModifiedDate
    private Instant modifyAt;

    @Override
    public boolean isNew() {
        return createAt == null;
    }
}
