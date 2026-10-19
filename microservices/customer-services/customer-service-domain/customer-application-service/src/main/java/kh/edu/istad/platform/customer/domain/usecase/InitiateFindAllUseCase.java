package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateFindAllUseCase {

    private final CustomerRepository customerRepository;

    public List<FindAllResult> findAll() {
        return findAll(null);
    }

    public List<FindAllResult> findAll(CustomerFilterCriteria criteria) {
        List<Customer> customers = criteria != null
                ? customerRepository.filter(criteria)
                : customerRepository.findAll();
        return customers.stream()
                .map(this::toResult)
                .toList();
    }

    FindAllResult toResult(Customer customer) {
        return new FindAllResult(
                customer.getId() != null ? customer.getId().value() : null,
                customer.getUsername(),
                customer.getFamilyName(),
                customer.getGivenName(),
                customer.getEmail(),
                customer.getPhoneNumber() != null ? customer.getPhoneNumber().value() : null,
                customer.getCustomerSegment() != null ? customer.getCustomerSegment().name() : null
        );
    }
}
