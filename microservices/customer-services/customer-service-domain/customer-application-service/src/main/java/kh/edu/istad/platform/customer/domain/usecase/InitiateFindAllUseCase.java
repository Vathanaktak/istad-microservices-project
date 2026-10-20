package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.mapper.CustomerDataMapper;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitiateFindAllUseCase {

    private final CustomerRepository customerRepository;
    private final CustomerDataMapper customerDataMapper;

    public Flux<FindAllResult> findAll() {
        return findAll(null);
    }

    public Flux<FindAllResult> findAll(CustomerFilterCriteria criteria) {
        Flux<Customer> customers = criteria != null
                ? customerRepository.filter(criteria)
                : customerRepository.findAll();
        return customers.map(customerDataMapper::toFindAllResult);
    }
}
