package kh.edu.istad.platform.customer.domain.service;

import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.event.CustomerDeactivatedEvent;
import kh.edu.istad.platform.customer.domain.event.CustomerInitialedEvent;
import kh.edu.istad.platform.customer.domain.event.CustomerUpdateEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CustomerDomainServiceImpl implements CustomerDomainService {
    @Override
    public CustomerInitialedEvent initialCustomer(Customer customer) {
        customer.initiateCustomer();

        return new CustomerInitialedEvent(customer, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public CustomerUpdateEvent updateCustomer(Customer customer, String familyName, String givenName) {
        customer.updateCustomer(familyName, givenName);
        return new CustomerUpdateEvent(customer, ZonedDateTime.now(ZoneId.of("UTC")));
    }

    @Override
    public CustomerDeactivatedEvent deactivateCustomer(Customer customer) {
        customer.deactivateCustomer();
        return new CustomerDeactivatedEvent(customer.getId(),ZonedDateTime.now(ZoneId.of("UTC")));
    }
}
