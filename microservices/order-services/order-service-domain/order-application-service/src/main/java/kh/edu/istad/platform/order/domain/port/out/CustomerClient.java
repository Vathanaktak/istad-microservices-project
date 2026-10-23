package kh.edu.istad.platform.order.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface CustomerClient {

    Optional<CustomerInfo> getCustomer(UUID customerId);

    record CustomerInfo(
            UUID id,
            String username,
            boolean active
    ){}

}
