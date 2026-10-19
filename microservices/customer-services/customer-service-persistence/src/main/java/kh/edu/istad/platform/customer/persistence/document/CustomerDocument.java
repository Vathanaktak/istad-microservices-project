package kh.edu.istad.platform.customer.persistence.document;

import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

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

    // References CustomerSegmentDocument in customer_segments collection.
    // In Spring Data MongoDB, @DocumentReference is the modern reference annotation (Spring Data 3.3+),
    // and @DBRef is the legacy BSON DBRef reference annotation. Both cannot be placed on the same field
    // simultaneously without causing a MappingException, so @DocumentReference is used here.
    @DocumentReference
    private CustomerSegmentDocument customerSegment;

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
