package kh.edu.istad.platform.order.domain.port.out;

import java.util.Optional;
import java.util.UUID;

public interface BusinessClient {

    Optional<BusinessInfo> getBusiness(UUID businessId);
    record BusinessInfo(
            UUID businessId,
            String displayName,
            boolean active
    ){}

}
