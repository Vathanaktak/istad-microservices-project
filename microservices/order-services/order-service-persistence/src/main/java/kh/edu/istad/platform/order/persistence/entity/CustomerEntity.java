package kh.edu.istad.platform.order.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table("customers")
@NoArgsConstructor
public class CustomerEntity {
    @Id
    private UUID customerId;
    @Column("family_name")
    private String familyName;
    @Column("given_name")
    private String givenName;
    private String username;

}
