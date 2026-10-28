package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.entity.AggregateRoot;
import kh.edu.istad.common.valueobject.*;
import kh.edu.istad.platform.order.domain.exception.OrderDomainException;
import kh.edu.istad.platform.order.domain.valueobject.StreetAddress;
import kh.edu.istad.platform.order.domain.valueobject.TrackingId;
import java.util.List;
import java.util.UUID;

public class Order extends AggregateRoot<OrderId> {

    private final CustomerId customerId;
    private final BusinessId businessId;
    private final StreetAddress deliveryAddress;
    private final Money price;
    private final List<OrderItem> items;

    private TrackingId trackingId;
    private OrderStatus orderStatus;
    private List<String> failureMessage;
    private Long version;

//    =======start domain logic ============

    public void InitializeOrder(){

        if(super.getId() != null || orderStatus != null){
            throw new OrderDomainException(
                    "Order has already been initialized"
            );
        }

        super.setId(new OrderId(UUID.randomUUID()));
        trackingId = new TrackingId(UUID.randomUUID());
        orderStatus = OrderStatus.PENDING;
        validateOrder();

    }

    // validate pay order

    public void pay(){
        if(orderStatus != OrderStatus.PENDING){
            throw new OrderDomainException(
                    "Order state is not correct for pay operation"
            );
        }
        orderStatus = OrderStatus.PAID;
    }

    //=======Approve order
    public void approve(){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException(
                    "Order state is not correct for approve"
            );
        }
        orderStatus = OrderStatus.APPROVED;
    }
    // cancel
    public void initCancel(List<String> failureMessage){
        if(orderStatus != OrderStatus.PAID){
            throw new OrderDomainException(
                    "Order state is not correct for init cancel"
            );
        }
        orderStatus = OrderStatus.CANCELLING;
        updateFailureMessage(failureMessage);
    }

    public void updateFailureMessage(
            List<String> failureMessage
    ){
        if(this.failureMessage != null && failureMessage != null) {
            this.failureMessage.addAll(failureMessage);
        }else {
            this.failureMessage = failureMessage;
        }
    }

    public void cancel(){
        if(orderStatus != OrderStatus.PENDING || orderStatus != OrderStatus.CANCELLING){
            throw  new OrderDomainException(
                    "Order state is not correct for init cancel"
            );
        }
    }


    public void validateOrder(){
        if(!price.isGreaterThanZero() || price == null){
            throw new OrderDomainException
                    ("Price must be greater than zero and must be not null"
                    );
        }
        //validate product price
        Money orderSubTotal = items.stream()
                .map(orderItem -> {
                    validateItemPrice(orderItem);
                    return orderItem.getSubTotal();
                })
                .reduce(Money.ZERO,Money::add);

        if(!price.equals(orderSubTotal)){
            throw new OrderDomainException(
                    "Price is not equal to order subTotal price"
            );
        }
    }

    private void validateItemPrice(OrderItem orderItem){
        if(orderItem.getPrice().equals(orderItem.getProduct().getPrice())){
            throw new OrderDomainException(
                    "Price must be equal to product price"
            );
        }
    }


//    ===========end domain logic============

    public CustomerId getCustomerId() {
        return customerId;
    }

    public BusinessId getBusinessId() {
        return businessId;
    }

    public StreetAddress getDeliveryAddress() {
        return deliveryAddress;
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

    public List<String> getFailureMessage() {
        return failureMessage;
    }

    public Long getVersion() {
        return version;
    }

    private Order(Builder builder) {
        super.setId(builder.id);
        customerId = builder.customerId;
        businessId = builder.businessId;
        deliveryAddress = builder.deliveryAddress;
        price = builder.price;
        items = builder.items;
        trackingId = builder.trackingId;
        orderStatus = builder.orderStatus;
        failureMessage = builder.failureMessage;
        version = builder.version;
    }


    public static final class Builder {
        private OrderId id;
        private CustomerId customerId;
        private BusinessId businessId;
        private StreetAddress deliveryAddress;
        private Money price;
        private List<OrderItem> items;
        private TrackingId trackingId;
        private OrderStatus orderStatus;
        private List<String> failureMessage;
        private Long version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderId val) {
            id = val;
            return this;
        }

        public Builder customerId(CustomerId val) {
            customerId = val;
            return this;
        }

        public Builder businessId(BusinessId val) {
            businessId = val;
            return this;
        }

        public Builder deliveryAddress(StreetAddress val) {
            deliveryAddress = val;
            return this;
        }

        public Builder price(Money val) {
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

        public Builder failureMessage(List<String> val) {
            failureMessage = val;
            return this;
        }

        public Builder version(Long val) {
            version = val;
            return this;
        }

        public Order build() {
            return new Order(this);
        }
    }
}