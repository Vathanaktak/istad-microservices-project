package kh.edu.istad.platform.business.domain.usecase;

import kh.edu.istad.platform.business.domain.dto.GetBusinessResult;
import reactor.core.publisher.Mono;

import java.util.UUID;


public interface GetBusinessUseCase {
    Mono<GetBusinessResult> findById(UUID businessId);
}
