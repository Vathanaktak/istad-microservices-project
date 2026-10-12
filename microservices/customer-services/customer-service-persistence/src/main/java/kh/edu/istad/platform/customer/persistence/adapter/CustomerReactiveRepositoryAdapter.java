package kh.edu.istad.platform.customer.persistence.adapter;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CustomerReactiveRepositoryAdapter implements CustomerRepository {
    @Override
    public Customer save(Customer customer) {


        return null;
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return Optional.empty();
    }
}
