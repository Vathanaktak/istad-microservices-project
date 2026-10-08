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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateDeactivateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;

    public InitiateDeactivateCustomerResult execute(InitiateDeactivateCustomerCommand command){
        log.info("Initiate deactivate customer use case {}", command);

        Customer customer = customerRepository.findById(new CustomerId(command.customerId()))
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " ));

        CustomerDeactivatedEvent event = customerDomainService.deactivateCustomer(customer);
        customerRepository.save(customer);
        return new InitiateDeactivateCustomerResult(event.getCustomerId().value(), event.getDeactivatedAt());

    }

}
