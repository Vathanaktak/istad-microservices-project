package kh.edu.istad.platform.business.domain.port.out;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusinessRepository {
    Mono<Business> save(Business business);
    Mono<Boolean> existsByLegalName(String legalName);
    Mono<Business> findById(BusinessId businessId);
}
