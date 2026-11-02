package kh.edu.istad.platform.business.domain.port.out;

import kh.edu.istad.platform.business.domain.entity.Business;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusinessRepository {
    Mono<Business> save(Business business);
    Mono<Boolean> existsByLegalName(String legalName);
    Flux<Business> findAll();
    Mono<Business> findById(UUID id);
}
