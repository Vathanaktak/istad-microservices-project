

package kh.edu.istad.platform.customer.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateDeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateUpdateCustomerUseCase;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final InitiateCustomerUseCase initiateCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final InitiateUpdateCustomerUseCase initiateUpdateCustomerUseCase;
    private final InitiateDeactivateCustomerUseCase initiateDeactivateCustomerUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public CustomerInitiateResponse initiateCustomer(
            @Valid @RequestBody CustomerInitiateRequest customerInitiateRequest
            ){
        InitiateCustomerResult result = initiateCustomerUseCase.execute(
                customerWebMapper.toCommand(customerInitiateRequest)
        );
        return customerWebMapper.toResponse(result);
    }

    @PutMapping("/{customerId}")
    public CustomerUpdateInitiateResponse updateCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateInitiateRequest request
    ){
        return customerWebMapper.toUpdateResponse(
                initiateUpdateCustomerUseCase.execute(customerWebMapper.toUpdateCommand(customerId, request))
        );
    }

    @PatchMapping("/{customerId}")
    public CustomerUpdateInitiateResponse patchCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateInitiateRequest request
    ){
        return updateCustomer(customerId, request);
    }

    @PatchMapping("/{customerId}/deactivate")
    public CustomerDeactivateInitiateResponse deactivateInitiateResponse(
            @PathVariable("customerId") UUID customerId
    ){
        return customerWebMapper.toDeactivateResponse(
                initiateDeactivateCustomerUseCase.execute(new InitiateDeactivateCustomerCommand(customerId))
        );
    }
}
