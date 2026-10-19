package kh.edu.istad.platform.customer.domain.port.out;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.CustomerStatusReportResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(CustomerId customerId);
    List<Customer> findAll();
    List<Customer> filter(CustomerFilterCriteria criteria);
    List<CustomerStatusReportResult> getCustomerStatusReport();
}
