package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.InitiateUpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateUpdateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdateEvent;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
@Slf4j
public class InitiateUpdateCustomerUseCase {
    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;

    public Mono<InitiateUpdateCustomerResult> execute(InitiateUpdateCustomerCommand command) {
        log.info("Initiate update customer use case {}", command);

        return customerRepository.findById(new CustomerId(command.customerId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Customer not found with id: " + command.customerId())))
                .flatMap(customer -> {
                    CustomerUpdateEvent event = customerDomainService.updateCustomer(customer, command.familyName(), command.givenName());
                    return customerRepository.save(customer)
                            .map(saved -> new InitiateUpdateCustomerResult(
                                    command.customerId(),
                                    command.familyName(),
                                    command.givenName(),
                                    event.getUpdatedAt()
                            ));
                });
    }
}
