package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import kh.edu.istad.platform.customer.persistence.mapper.CustomerPersistenceMapper;
import kh.edu.istad.platform.customer.persistence.repository.CustomerMongoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {

    private final CustomerMongoRepository customerMongoRepository;
    private final CustomerPersistenceMapper customerPersistenceMapper;

    @Override
    public Customer save(Customer customer) {
        CustomerDocument document = customerPersistenceMapper.toDocument(customer);
        CustomerDocument saved = customerMongoRepository.save(document).block();
        return customerPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        if (customerId == null || customerId.value() == null) {
            return Optional.empty();
        }
        CustomerDocument doc = customerMongoRepository.findById(customerId.value().toString()).block();
        return Optional.ofNullable(customerPersistenceMapper.toDomain(doc));
    }

    @Override
    public List<Customer> findAll() {
        List<Customer> customers = customerMongoRepository.findAll()
                .map(customerPersistenceMapper::toDomain)
                .collectList()
                .block();
        return customers != null ? customers : Collections.emptyList();
    }
}
