package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class InitiateDeleteCustomerUseCase {

        private final CustomerRepository customerRepository;
        public Boolean deleteCustomerById(String id){
            Customer customer = customerRepository.findById(new CustomerId(UUID.fromString(id)))
                    .orElseThrow(
                            () -> new DomainException("Customer Not found")
                    );
            return customerRepository.deleteCustomerById(id);
        }

}
