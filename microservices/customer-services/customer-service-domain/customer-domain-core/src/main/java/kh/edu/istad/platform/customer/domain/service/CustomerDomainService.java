package kh.edu.istad.platform.customer.domain.service;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.event.CustomerInitialedEvent;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdateEvent;

public interface CustomerDomainService {

    CustomerInitialedEvent initialCustomer(Customer customer);

    CustomerUpdateEvent updateCustomer(Customer customer, String familyName, String givenName);

    CustomerDeactivatedEvent deactivateCustomer(Customer customer);

}
