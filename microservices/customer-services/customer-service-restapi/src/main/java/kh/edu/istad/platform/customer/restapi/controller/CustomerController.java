

package kh.edu.istad.platform.customer.restapi.controller;
import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.usecase.CustomerStatusReportUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateDeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateFindAllUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateUpdateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.restapi.dto.*;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/v1/customers", "/customers"})
public class CustomerController {

    private final InitiateCustomerUseCase initiateCustomerUseCase;
    private final CustomerWebMapper customerWebMapper;
    private final InitiateUpdateCustomerUseCase initiateUpdateCustomerUseCase;
    private final InitiateDeactivateCustomerUseCase initiateDeactivateCustomerUseCase;
    private final InitiateFindAllUseCase findAllUseCase;
    private final CustomerStatusReportUseCase customerStatusReportUseCase;

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

    @GetMapping
    public List<FindAllResult> findAll(
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

    public List<FindAllResult> findAll(
            String name,
            String email,
            String phoneNumber,
            CustomerStatus status
    ) {
        return findAll(name, email, phoneNumber, status, null);
    }

    @GetMapping("/report")
    public List<CustomerStatusReportResponse> getCustomerStatusReport() {
        return customerStatusReportUseCase.execute().stream()
                .map(r -> new CustomerStatusReportResponse(r.status(), r.total()))
                .toList();
    }
}
