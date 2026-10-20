package kh.edu.istad.platform.customer.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.usecase.*;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/customers", "/customers"})
public class CustomerController {

    private final InitiateCustomerUseCase initiateCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final InitiateUpdateCustomerUseCase initiateUpdateCustomerUseCase;
    private final InitiateDeactivateCustomerUseCase initiateDeactivateCustomerUseCase;
    private final InitiateFindAllUseCase findAllUseCase;
    private final InitiateDeleteCustomerUseCase deleteCustomerUseCase;
    private final InitiateChangeCustomerSegmentUseCase initiateChangeCustomerSegmentUseCase;
    private final CustomerStatisticsUseCase customerStatisticsUseCase;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<CustomerInitiateResponse> initiateCustomer(
            @Valid @RequestBody CustomerInitiateRequest customerInitiateRequest
    ){
        return initiateCustomerUseCase.execute(
                customerWebMapper.toCommand(customerInitiateRequest)
        ).map(customerWebMapper::toResponse);
    }

    @PutMapping("/{customerId}")
    public Mono<CustomerUpdateInitiateResponse> updateCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateInitiateRequest request
    ){
        return initiateUpdateCustomerUseCase.execute(customerWebMapper.toUpdateCommand(customerId, request))
                .map(customerWebMapper::toUpdateResponse);
    }

    @PatchMapping("/{customerId}")
    public Mono<CustomerUpdateInitiateResponse> patchCustomer(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerUpdateInitiateRequest request
    ){
        return updateCustomer(customerId, request);
    }

    @PatchMapping("/{customerId}/deactivate")
    public Mono<CustomerDeactivateInitiateResponse> deactivateInitiateResponse(
            @PathVariable("customerId") UUID customerId
    ){
        return initiateDeactivateCustomerUseCase.execute(new InitiateDeactivateCustomerCommand(customerId))
                .map(customerWebMapper::toDeactivateResponse);
    }

    @PatchMapping("/{customerId}/segment")
    public Mono<CustomerInitiateResponse> changeCustomerSegment(
            @PathVariable("customerId") UUID customerId,
            @Valid @RequestBody CustomerChangeSegmentRequest request
    ) {
        return initiateChangeCustomerSegmentUseCase.execute(
                customerWebMapper.toChangeSegmentCommand(customerId, request)
        ).map(customerWebMapper::toResponse);
    }

    @GetMapping
    public Flux<FindAllResult> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String customerSegment
    ) {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(
                name,
                email,
                phoneNumber,
                status != null ? status.name() : null,
                customerSegment
        );
        return findAllUseCase.findAll(criteria);
    }

    public Flux<FindAllResult> findAll(
            String name,
            String email,
            String phoneNumber,
            CustomerStatus status
    ) {
        return findAll(name, email, phoneNumber, status, null);
    }

    @GetMapping({"/statistics", "/stats"})
    public Mono<CustomerStatisticsResponse> getCustomerStatistics() {
        return customerStatisticsUseCase.execute()
                .map(customerWebMapper::toStatisticsResponse);
    }

    @DeleteMapping("/{id}")
    public Mono<Boolean> deleteCustomerById(@PathVariable String id){
        return deleteCustomerUseCase.deleteCustomerById(id);
    }
}
