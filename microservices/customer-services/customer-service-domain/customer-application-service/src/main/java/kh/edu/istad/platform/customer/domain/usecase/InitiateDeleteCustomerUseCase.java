package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class InitiateDeleteCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Mono<Boolean> deleteCustomerById(String id) {
        return customerRepository.findById(new CustomerId(UUID.fromString(id)))
                .switchIfEmpty(Mono.error(new DomainException("Customer Not found")))
                .flatMap(customer -> customerRepository.deleteCustomerById(id));
    }
}
