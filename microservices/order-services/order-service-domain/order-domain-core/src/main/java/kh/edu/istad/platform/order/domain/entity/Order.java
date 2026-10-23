package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.entity.AggregateRoot;
import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.*;

import javax.swing.*;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private final UUID customerId;
    private final BusinessId businessId;
    private final StreetAddress streetAddress;
    private Money price;
    private List<OrderItem> items;
    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessages;


    public void validateOrder(){
        if(price == null || price.amount().compareTo(java.math.BigDecimal.ZERO) <= 0){
            throw new DomainException("Total order price must be greater than zero");
        }
        if(items == null || items.isEmpty()){
            throw new DomainException("Order must contain at least one item");
        }
    }

    public void initializeOrder(){
        setId(OrderId.generate());
        this.trackingId = TrackingId.generate();
        this.orderStatus = OrderStatus.PENDING;
        initializeOrderItem();
    }
    public void pay(){
        if(orderStatus != OrderStatus.PENDING){
            throw new DomainException("Only a PENDING order can be paid");
        }
        orderStatus = OrderStatus.PAID;
    }
    public void cancel(List<String> failureMessages){
        if(orderStatus != OrderStatus.PENDING){
            throw new DomainException("Only a PENDING order can be cancelled");
        }
        orderStatus = OrderStatus.CANCELLED;
        this.failureMessages = failureMessages;
    }

    public void initializeOrderItem(){
        for (OrderItem item : items){
            item.initializeOrderItem(super.getId(),OrderItemId.generate());
        }
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public StreetAddress getStreetAddress() {
        return streetAddress;
    }

    public Money getPrice() {
        return price;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public TrackingId getTrackingId() {
        return trackingId;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public List<String> getFailureMessages() {
        return failureMessages;
    }

    private Order(Builder builder) {
        super.setId(builder.orderId);
        customerId = builder.customerId;
        businessId = builder.businessId;
        streetAddress = builder.streetAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessages = builder.failureMessages;
    }

    public static Builder builder() { return new Builder(); }


    public static final class Builder {
        private OrderId orderId;
        private UUID customerId;
        private BusinessId businessId;
        private StreetAddress streetAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessages;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder customerId(UUID val) {
            customerId = val;
            return this;
        }

        public Builder businessId(BusinessId val) {
            businessId = val;
            return this;
        }

        public Builder streetAddress(StreetAddress val) {
            streetAddress = val;
            return this;
        }

        public Builder money(Money val) {
            price = val;
            return this;
        }

        public Builder items(List<OrderItem> val) {
            items = val;
            return this;
        }

        public Builder trackingId(TrackingId val) {
            trackingId = val;
            return this;
        }

        public Builder orderStatus(OrderStatus val) {
            orderStatus = val;
            return this;
        }

        public Builder failureMessages(List<String> val) {
            failureMessages = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}
