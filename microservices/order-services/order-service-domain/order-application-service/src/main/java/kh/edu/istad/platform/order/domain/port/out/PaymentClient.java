package kh.edu.istad.platform.order.domain.port.out;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentClient {

    PaymentInfo pay(UUID orderId, UUID customerId, BigDecimal amount);

    record PaymentInfo(
            String status,
            List<String> failureMessages
    ){
        public boolean isCompleted(){
            return "COMPLETED".equals(status);
        }
    }
}
