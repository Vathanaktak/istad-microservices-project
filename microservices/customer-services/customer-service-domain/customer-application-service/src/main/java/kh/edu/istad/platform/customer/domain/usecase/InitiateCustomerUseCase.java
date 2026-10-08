package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

//@Slf4j
//@RequiredArgsConstructor
//@Component
//public class InitiateCustomerUseCase {
//    @Autowired
//    private final CustomerDomainService customerDomainService;
//
//    public InitiateCustomerResult execute(InitiateCustomerCommand command) {
//        log.info("initiate customer use case {}", command);
////        return new
//        return new InitiateCustomerResult(UUID.randomUUID());
//    }
//}

@Slf4j
@RequiredArgsConstructor
@Component
public class InitiateCustomerUseCase {

    private final CustomerDomainService customerDomainService;
    private final CustomerRepository customerRepository;

    public InitiateCustomerResult execute(InitiateCustomerCommand command) {
        log.info("initiate customer use case {}", command);

        Customer customer = Customer.Builder.builder()
                .username(command.username())
                .familyName(command.familyName())
                .givenName(command.givenName())
                .email(command.email())
                .phoneNumber(new PhoneNumber(command.phoneNumber()))
                .build();

        customerDomainService.initialCustomer(customer); // sets UUID + ACTIVE
        Customer saved = customerRepository.save(customer);

        return new InitiateCustomerResult(saved.getId().value());
    }
}
