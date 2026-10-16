package kh.edu.istad.platform.payment.domain.mapper;


import kh.edu.istad.platform.payment.domain.dto.PaymentResult;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "orderId.value",target = "orderId")
    @Mapping(source = "customerId.value",target = "customerId")
    @Mapping(source = "price.amount",target = "price")
    @Mapping(source = "paymentStatus",target = "status")
    @Mapping(source = "createdAt",target = "createAt")
    @Mapping(target = "paymentId", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "failureMessages", expression = "java(new java.util.ArrayList<>())")
    PaymentResult toPaymentResult(Payment payment);

}
