package kh.edu.istad.platform.order.domain.entity;

import kh.edu.istad.common.entity.BaseEntity;
import kh.edu.istad.platform.business.domain.valueobject.Money;
import kh.edu.istad.platform.order.domain.valueobject.OrderId;
import kh.edu.istad.platform.order.domain.valueobject.OrderItemId;

public class OrderItem extends BaseEntity<OrderItemId> {

    private OrderId orderId;
    private final String productName;
    private final int qty;
    private final Money price;
    private Money subTotal;

    public OrderItem(OrderItemId orderItemId, String productName, int qty, Money price) {
        super.setId(orderItemId);
        this.productName = productName;
        this.qty = qty;
        this.price = price;
        this.subTotal = new Money(price.amount().multiply(java.math.BigDecimal.valueOf(qty)));
    }

    public void initializeOrderItem(
            OrderId orderId, OrderItemId orderItemId
    ){
        this.orderId = orderId;
        super.setId(orderItemId);
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQty() {
        return qty;
    }

    public Money getPrice() {
        return price;
    }

    public Money getSubTotal() {
        return subTotal;
    }
}
