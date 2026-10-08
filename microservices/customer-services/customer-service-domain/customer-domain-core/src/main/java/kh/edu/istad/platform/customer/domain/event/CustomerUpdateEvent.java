package kh.edu.istad.platform.customer.domain.event;

import kh.edu.istad.common.event.DomainEvent;
import kh.edu.istad.platform.customer.domain.entity.Customer;

import java.time.ZonedDateTime;

public class CustomerUpdateEvent implements DomainEvent<Customer> {
    private final Customer customer;
    private final ZonedDateTime updatedAt;

    public CustomerUpdateEvent(Customer customer, ZonedDateTime updatedAt) {
        this.customer = customer;
        this.updatedAt = updatedAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}
