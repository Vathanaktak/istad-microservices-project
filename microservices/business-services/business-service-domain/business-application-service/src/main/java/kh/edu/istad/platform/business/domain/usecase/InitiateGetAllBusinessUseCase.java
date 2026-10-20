package kh.edu.istad.platform.business.domain.usecase;

import kh.edu.istad.platform.business.domain.dto.InitiateBusinessResult;
import reactor.core.publisher.Flux;

public interface InitiateGetAllBusinessUseCase {

    public Flux<InitiateBusinessResult> findAll();
}
