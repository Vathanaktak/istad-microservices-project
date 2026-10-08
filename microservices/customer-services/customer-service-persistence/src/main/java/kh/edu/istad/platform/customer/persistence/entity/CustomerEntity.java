package kh.edu.istad.platform.customer.persistence.entity;

import jakarta.persistence.*;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter @Setter @NoArgsConstructor
public class CustomerEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    private String familyName;
    private String givenName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;
}