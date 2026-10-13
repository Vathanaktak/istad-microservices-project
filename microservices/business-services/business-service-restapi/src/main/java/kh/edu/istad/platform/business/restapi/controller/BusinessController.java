package kh.edu.istad.platform.business.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.business.domain.dto.InitiateBusinessCommand;
import kh.edu.istad.platform.business.domain.usecase.InitiateBusinessUseCase;
import kh.edu.istad.platform.business.restapi.dto.BusinessCreateRequest;
import kh.edu.istad.platform.business.restapi.dto.BusinessCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final InitiateBusinessUseCase businessUseCase;

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


}
