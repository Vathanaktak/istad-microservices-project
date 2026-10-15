package kh.edu.istad.platform.payment.restapi.controller;


import jakarta.validation.Valid;
import kh.edu.istad.platform.payment.domain.dto.CancelPaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.PaymentResult;
import kh.edu.istad.platform.payment.domain.usecase.PaymentUseCase;
import kh.edu.istad.platform.payment.restapi.dto.PaymentCreateRequest;
import kh.edu.istad.platform.payment.restapi.dto.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody
            PaymentCreateRequest request
    ){

        CreatePaymentCommand command = new CreatePaymentCommand(
                request.customerId(),
                request.orderId(),
                request.price()
        );
        PaymentResult result = paymentUseCase.processPayment(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable UUID orderId
            ){
        CancelPaymentCommand command = new CancelPaymentCommand(orderId);
        PaymentResult result = paymentUseCase.cancelPayment(command);
        return ResponseEntity.ok(toResponse(result));

    }


    private PaymentResponse toResponse(PaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.customerId(),
                result.price(),
                result.status(),
                result.createAt(),
                result.failureMessages()
        );
    }
}
