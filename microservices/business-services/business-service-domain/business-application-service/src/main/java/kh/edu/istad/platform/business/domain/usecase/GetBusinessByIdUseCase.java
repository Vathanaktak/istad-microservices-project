package kh.edu.istad.platform.business.domain.usecase;

import kh.edu.istad.platform.business.domain.dto.InitiateBusinessResult;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetBusinessByIdUseCase {
    Mono<InitiateBusinessResult> getBusinessById(UUID businessId);
}
