package kh.edu.istad.platform.order.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table("order_address")
@NoArgsConstructor
public class OrderAddressEntity {
    @Id
    private UUID id;
    private String street;
    private String city;
    private String postalCode;

}
