package kh.edu.istad.platform.customer.restapi.controller;

import jakarta.validation.Valid;
import kh.edu.istad.platform.customer.domain.usecase.*;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegmentFilterCriteria;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentCreateRequest;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentResponse;
import kh.edu.istad.platform.customer.restapi.dto.CustomerSegmentUpdateRequest;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/customer-segments", "/customer-segments"})
public class CustomerSegmentController {

    private final InitiateCreateCustomerSegmentUseCase createCustomerSegmentUseCase;
    private final InitiateFindAllCustomerSegmentsUseCase findAllCustomerSegmentsUseCase;
    private final InitiateFindCustomerSegmentByIdUseCase findCustomerSegmentByIdUseCase;
    private final InitiateUpdateCustomerSegmentUseCase updateCustomerSegmentUseCase;
    private final InitiateDeleteCustomerSegmentUseCase deleteCustomerSegmentUseCase;
    private final CustomerWebMapper customerWebMapper;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mono<CustomerSegmentResponse> createSegment(@Valid @RequestBody CustomerSegmentCreateRequest request) {
        return createCustomerSegmentUseCase.execute(customerWebMapper.toSegmentCommand(request))
                .map(customerWebMapper::toSegmentResponse);
    }

    @GetMapping
    public Flux<CustomerSegmentResponse> findAllSegments(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String query
    ) {
        CustomerSegmentFilterCriteria criteria = new CustomerSegmentFilterCriteria(id, name, description, query);
        return findAllCustomerSegmentsUseCase.findAll(criteria)
                .map(customerWebMapper::toSegmentResponse);
    }

    public Flux<CustomerSegmentResponse> findAllSegments() {
        return findAllSegments(null, null, null, null);
    }

    @GetMapping("/{id}")
    public Mono<CustomerSegmentResponse> findSegmentById(@PathVariable String id) {
        return findCustomerSegmentByIdUseCase.findById(id)
                .map(customerWebMapper::toSegmentResponse);
    }

    @PutMapping("/{id}")
    public Mono<CustomerSegmentResponse> updateSegment(
            @PathVariable String id,
            @Valid @RequestBody CustomerSegmentUpdateRequest request
    ) {
        return updateCustomerSegmentUseCase.execute(customerWebMapper.toSegmentUpdateCommand(id, request))
                .map(customerWebMapper::toSegmentResponse);
    }

    @DeleteMapping("/{id}")
    public Mono<Boolean> deleteSegmentById(@PathVariable String id) {
        return deleteCustomerSegmentUseCase.deleteById(id);
    }
}
