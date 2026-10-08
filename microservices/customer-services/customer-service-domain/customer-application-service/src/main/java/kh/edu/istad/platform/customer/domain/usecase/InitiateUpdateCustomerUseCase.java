package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.InitiateUpdateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateUpdateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdateEvent;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class InitiateUpdateCustomerUseCase {
    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;

    public InitiateUpdateCustomerResult execute(InitiateUpdateCustomerCommand command) {
        log.info("Initiate update customer use case {}", command);
        // Implement the logic to initiate the update customer process
        // For example, you can call the customerDomainService to perform the update
        // and return the result as InitiateUpdateCustomerResult

        Customer customer= customerRepository.findById(new CustomerId(command.customerId()))
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + command.customerId()));

        CustomerUpdateEvent event = customerDomainService.updateCustomer(customer,command.familyName(),command.givenName());
        customerRepository.save(customer);
        return new InitiateUpdateCustomerResult(command.customerId(),command.familyName()
        ,command.givenName(),event.getUpdatedAt());
    }
}

