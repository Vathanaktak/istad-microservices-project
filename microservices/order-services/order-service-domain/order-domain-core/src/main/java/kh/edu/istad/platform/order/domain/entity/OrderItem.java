package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.entity.BaseEntity;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.common.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;

public class OrderItem extends BaseEntity<OrderItemId> {

    private final OrderId orderId;
    private final Product product;
    private final Integer qty;
    private final Money price;
    private final Money subTotal;

    public OrderItem(OrderId orderId, Product product, Integer qty, Money price, Money subTotal) {
        this.orderId = orderId;
        this.product = product;
        this.qty = qty;
        this.price = price;
        this.subTotal = subTotal;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Product getProduct() {
        return product;
    }

    public Integer getQty() {
        return qty;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }

    private OrderItem(Builder builder) {
        super.setId(builder.id);
        orderId = builder.orderId;
        product = builder.product;
        qty = builder.qty;
        price = builder.price;
        subTotal = builder.subTotal;
    }


    public static final class Builder {
        private OrderItemId id;
        private OrderId orderId;
        private Product product;
        private Integer qty;
        private Money price;
        private Money subTotal;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder id(OrderItemId val) {
            id = val;
            return this;
        }

        public Builder orderId(OrderId val) {
            orderId = val;
            return this;
        }

        public Builder product(Product val) {
            product = val;
            return this;
        }

        public Builder qty(Integer val) {
            qty = val;
            return this;
        }

        public Builder price(Money val) {
            price = val;
            return this;
        }

        public Builder subTotal(Money val) {
            subTotal = val;
            return this;
        }

        public OrderItem build() {
            return new OrderItem(this);
        }
    }
}