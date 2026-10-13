package kh.edu.istad.platform.order.domain.usecase;

import kh.edu.istad.platform.order.domain.dto.CreateOrderCommand;
import kh.edu.istad.platform.order.domain.dto.CreateOrderResult;

public interface InitiateOrderUseCase {

    public CreateOrderResult createOrder(
            CreateOrderCommand command
    );

}
