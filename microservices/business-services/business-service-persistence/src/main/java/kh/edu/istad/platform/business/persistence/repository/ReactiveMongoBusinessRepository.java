package kh.edu.istad.platform.business.persistence.repository;

import kh.edu.istad.platform.business.persistence.entity.BusinessDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReactiveMongoBusinessRepository extends ReactiveMongoRepository<BusinessDocument, UUID> {
    Mono<Boolean> existsByLegalName(String legalName);
}
