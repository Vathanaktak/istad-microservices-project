package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatisticsResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository {
    Mono<Customer> save(Customer customer);
    Mono<Customer> findById(CustomerId customerId);
    Flux<Customer> findAll();
    Flux<Customer> filter(CustomerFilterCriteria criteria);
    Mono<CustomerStatisticsResult> getCustomerStatistics();
    Mono<Boolean> deleteCustomerById(String id);
}
