package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class InitiateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;
    private final CustomerDataMapper customerDataMapper;

    public Mono<InitiateCustomerResult> execute(InitiateCustomerCommand command) {
        log.info("initiate customer use case {}", command);

        CustomerSegment segment = command.customerSegment() != null && !command.customerSegment().isBlank()
                ? CustomerSegment.fromName(command.customerSegment())
                : CustomerSegment.SILVER;

        Customer customer = Customer.Builder.builder()
                .username(command.username())
                .familyName(command.familyName())
                .givenName(command.givenName())
                .email(command.email())
                .phoneNumber(new PhoneNumber(command.phoneNumber()))
                .customerSegment(segment)
                .build();

        customerDomainService.initialCustomer(customer); // sets UUID + ACTIVE, defaults segment to SILVER if null

        return customerRepository.save(customer)
                .map(customerDataMapper::toInitiateCustomerResult);
    }
}
