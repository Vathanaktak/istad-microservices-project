package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;

    @Override
    public Customer save(Customer customer) {
        CustomerDocument document = toDocument(customer);
        CustomerDocument saved = customerMongoRepository.save(document).block();
        return toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        if (customerId == null || customerId.value() == null) {
            return Optional.empty();
        }
        CustomerDocument doc = customerMongoRepository.findById(customerId.value()).block();
        return Optional.ofNullable(toDomain(doc));
    }

    private CustomerDocument toDocument(Customer customer) {
        if (customer == null) {
            return null;
        }
        return CustomerDocument.builder()
                .id(customer.getId() != null ? customer.getId().value() : null)
                .username(customer.getUsername())
                .familyName(customer.getFamilyName())
                .givenName(customer.getGivenName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber() != null ? customer.getPhoneNumber().value() : null)
                .status(customer.getStatus())
                .build();
    }

    private Customer toDomain(CustomerDocument doc) {
        if (doc == null) {
            return null;
        }
        return Customer.Builder.builder()
                .id(doc.getId() != null ? new CustomerId(doc.getId()) : null)
                .username(doc.getUsername())
                .familyName(doc.getFamilyName())
                .givenName(doc.getGivenName())
                .email(doc.getEmail())
                .phoneNumber(doc.getPhoneNumber() != null ? new PhoneNumber(doc.getPhoneNumber()) : null)
                .status(doc.getStatus())
                .build();
    }
}
