package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.entity.CustomerEntity;
import kh.edu.istad.platform.customer.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;

    @Override
    public Customer save(Customer customer) {
        return toDomain(jpaRepository.save(toEntity(customer)));
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return jpaRepository.findById(customerId.value()).map(this::toDomain);
    }

    private CustomerEntity toEntity(Customer c) {
        CustomerEntity e = new CustomerEntity();
        e.setId(c.getId().value());
        e.setUsername(c.getUsername());
        e.setFamilyName(c.getFamilyName());
        e.setGivenName(c.getGivenName());
        e.setEmail(c.getEmail());
        e.setPhoneNumber(c.getPhoneNumber() == null ? null : c.getPhoneNumber().value());
        e.setStatus(c.getStatus());
        return e;
    }

    private Customer toDomain(CustomerEntity e) {
        return Customer.Builder.builder()
                .id(new CustomerId(e.getId()))
                .username(e.getUsername())
                .familyName(e.getFamilyName())
                .givenName(e.getGivenName())
                .email(e.getEmail())
                .phoneNumber(e.getPhoneNumber() == null ? null : new PhoneNumber(e.getPhoneNumber()))
                .status(e.getStatus())
                .build();
    }
}