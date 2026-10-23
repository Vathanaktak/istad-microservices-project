package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.GetCustomerResult;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class GetCustomerUseCase {

    private final CustomerRepository customerRepository;

    public Optional<GetCustomerResult> execute(UUID customerId){
        return customerRepository.findById(new CustomerId(customerId))
                .map(cus -> new GetCustomerResult(
                            cus.getId().value(),
                            cus.getUsername(),
                            cus.getGivenName(),
                            cus.getFamilyName(),
                            cus.getEmail(),
                            cus.getPhoneNumber() == null ? null:cus.getPhoneNumber().value(),
                            cus.getStatus() == null ? null : cus.getStatus().name()
                ));

    }
}
