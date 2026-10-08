package kh.edu.istad.platform.customer.domain.event;

import kh.edu.istad.common.event.DomainEvent;
import kh.edu.istad.platform.customer.domain.entity.Customer;

import java.time.ZonedDateTime;

public class CustomerInitialedEvent implements DomainEvent<Customer> {
    private final Customer customer;
    private final ZonedDateTime initialedAt;

    public CustomerInitialedEvent(Customer customer, ZonedDateTime initialedAt) {
        this.customer = customer;
        this.initialedAt = initialedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ZonedDateTime getInitialedAt() {
        return initialedAt;
    }
}
