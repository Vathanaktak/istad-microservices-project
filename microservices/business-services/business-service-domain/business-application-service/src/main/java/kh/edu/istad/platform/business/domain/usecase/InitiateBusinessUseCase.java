package kh.edu.istad.platform.business.domain.usecase;

import kh.edu.istad.platform.business.domain.dto.InitiateBusinessCommand;
import kh.edu.istad.platform.business.domain.dto.InitiateBusinessResult;
import reactor.core.publisher.Mono;

public interface InitiateBusinessUseCase {

    Mono<InitiateBusinessResult> initiateBusiness(
            InitiateBusinessCommand command
    );
}
