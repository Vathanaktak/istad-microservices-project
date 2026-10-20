package kh.edu.istad.platform.business.domain.service;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.business.domain.dto.InitiateBusinessCommand;
import kh.edu.istad.platform.business.domain.dto.InitiateBusinessResult;
import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.event.BusinessRegisteredEvent;
import kh.edu.istad.platform.business.domain.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.usecase.InitiateBusinessUseCase;
import kh.edu.istad.platform.business.domain.usecase.InitiateGetAllBusinessUseCase;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
@Builder
public class BusinessApplicationServiceImpl implements InitiateBusinessUseCase, InitiateGetAllBusinessUseCase {

    private final BusinessDomainService businessDomainService;
    private final BusinessRepository businessRepository;

    @Override
    public Mono<InitiateBusinessResult> initiateBusiness(InitiateBusinessCommand command) {

        return businessRepository.existsByLegalName(command.legalName())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new DomainException("Business legal name already exists"));
                    }
                    Business business = Business.builder()
                            .businessName(new BusinessName(command.legalName(), command.displayName()))
                            .businessContact(new BusinessContact(command.email(), command.phoneNumber()))
                            .build();

                    BusinessRegisteredEvent event = businessDomainService.validateAndInitiateBusiness(business);
                    log.info("Business initialized with id: {} at {}",
                            event.business().getId().value(), event.createAt());

                    return businessRepository.save(event.business())
                            .map(saved -> new InitiateBusinessResult(
                                    saved.getId().value(),
                                    saved.getBusinessName().displayName(),
                                    saved.isActive(),
                                    "Business created successfully"
                            ));
                });
    }

    @Override
    public Flux<InitiateBusinessResult> findAll() {
        return businessRepository.findAll()
                .map(
                        bus -> new InitiateBusinessResult(
                                bus.getId().value(),
                                bus.getBusinessName().displayName(),
                                bus.isActive(),
                                "Get All Business Successfuly"

                        )
                );
    }
}
