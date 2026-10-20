package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateDeactivateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;

    public Mono<InitiateDeactivateCustomerResult> execute(InitiateDeactivateCustomerCommand command){
        log.info("Initiate deactivate customer use case {}", command);

        return customerRepository.findById(new CustomerId(command.customerId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found with id: " + command.customerId())))
                .flatMap(customer -> {
                    CustomerDeactivatedEvent event = customerDomainService.deactivateCustomer(customer);
                    return customerRepository.save(customer)
                            .map(saved -> new InitiateDeactivateCustomerResult(event.getCustomerId().value(), event.getDeactivatedAt()));
                });
    }

}
