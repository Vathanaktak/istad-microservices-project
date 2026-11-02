package kh.edu.istad.platform.business.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.business.domain.dto.InitiateBusinessCommand;
import kh.edu.istad.platform.business.domain.usecase.GetBusinessByIdUseCase;
import kh.edu.istad.platform.business.domain.usecase.InitiateBusinessUseCase;
import kh.edu.istad.platform.business.domain.usecase.InitiateGetAllBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.BusinessCreateRequest;
import kh.edu.istad.platform.business.restapi.dto.BusinessCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final InitiateBusinessUseCase businessUseCase;
    private final InitiateGetAllBusinessUseCase getAllBusinessUseCase;
    private final GetBusinessByIdUseCase getBusinessByIdUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BusinessCreateResponse> createBusiness(
            @Valid @RequestBody BusinessCreateRequest request) {
        InitiateBusinessCommand command = new InitiateBusinessCommand(
                request.legalName(),
                request.displayName(),
                request.email(),
                request.phoneNumber()
        );
        return businessUseCase.initiateBusiness(command)
                .map(result -> new BusinessCreateResponse(
                        result.businessId(),
                        result.displayName(),
                        result.active(),
                        result.message()
                ));
    }

    @GetMapping
    public Flux<BusinessCreateResponse> findAll(){
        return getAllBusinessUseCase.findAll()
                .map(bus -> new BusinessCreateResponse(
                        bus.businessId(),
                        bus.displayName(),
                        bus.active(),
                        "Get All Business"
                ));
    }

    @GetMapping("/{businessId}")
    public Mono<BusinessCreateResponse> getBusinessById(@PathVariable UUID businessId) {
        return getBusinessByIdUseCase.getBusinessById(businessId)
                .map(result -> new BusinessCreateResponse(
                        result.businessId(),
                        result.displayName(),
                        result.active(),
                        result.message()
                ));
    }
}
