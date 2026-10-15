package kh.edu.istad.platform.payment.domain.service;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.common.valueobject.Money;
import kh.edu.istad.common.valueobject.OrderId;
import kh.edu.istad.platform.payment.domain.dto.CancelPaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.CreatePaymentCommand;
import kh.edu.istad.platform.payment.domain.dto.PaymentResult;
import kh.edu.istad.platform.payment.domain.entity.CreditEntry;
import kh.edu.istad.platform.payment.domain.entity.CreditHistory;
import kh.edu.istad.platform.payment.domain.entity.Payment;
import kh.edu.istad.platform.payment.domain.event.PaymentEvent;
import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;
import kh.edu.istad.platform.payment.domain.port.out.CreditEntryRepository;
import kh.edu.istad.platform.payment.domain.port.out.CreditHistoryRepository;
import kh.edu.istad.platform.payment.domain.port.out.PaymentRepository;
import kh.edu.istad.platform.payment.domain.usecase.PaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentApplicationServiceImpl
        implements PaymentUseCase {
//    @Autowired
    private final PaymentDomainService paymentDomainService;
    private final PaymentRepository paymentRepository;
    private final CreditEntryRepository creditEntryRepository;
    private final CreditHistoryRepository creditHistoryRepository;


    @Override
    public PaymentResult processPayment(
            CreatePaymentCommand command) {

        log.info("Received payment command for order id: {}",command.orderId());

        Payment payment = new Payment(
                null,
                new CustomerId(command.customerId()),
                new OrderId(command.orderId()),
                new Money(command.price()),
                null,
                null
        );
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();

        PaymentEvent paymentEvent = paymentDomainService.validateAndInitiatePayment(
                payment,
                creditEntry,
                creditHistories,
                failureMessages
        );
        persistDbObjects(
                payment,
                creditEntry,
                creditHistories,
                failureMessages
        );

        return toResult(paymentEvent.getPayment(),failureMessages);
    }

    @Override
    public PaymentResult cancelPayment(
            CancelPaymentCommand command) {
        log.info("Received cancel payment command for order id: {}",command.orderId());

        Payment payment = paymentRepository.findByOrderId(command.orderId())
                .orElseThrow(
                        () -> new PaymentDomainException(
                                "Payment order id not found"
                        )
                );
        CreditEntry creditEntry = getCreditEntry(payment.getCustomerId());
        List<CreditHistory> creditHistories = getCreditHistory(payment.getCustomerId());
        List<String> failureMessages = new ArrayList<>();

        PaymentEvent paymentEvent = paymentDomainService.validateAndCancelPayment(
                payment,
                creditEntry,
                creditHistories,
                failureMessages
        );
        persistDbObjects(
                payment,
                creditEntry,
                creditHistories,
                failureMessages);

        return toResult(paymentEvent.getPayment(),failureMessages);
    }

    private CreditEntry getCreditEntry(CustomerId customerId){

        return creditEntryRepository.findByCustomerId(customerId)
                .orElseThrow(
                        () -> new PaymentDomainException(
                                "Cloud not find credit entry for customer: "+customerId.value()
                        )
                );
    }

    private List<CreditHistory> getCreditHistory(CustomerId customerId){
        return new ArrayList<>(creditHistoryRepository.findByCustomerId(customerId));
    }
    private void persistDbObjects(
            Payment payment,
            CreditEntry creditEntry,
            List<CreditHistory> creditHistories,
            List<String> failureMessages
    ){

        paymentRepository.save(payment);
        if(failureMessages.isEmpty()){
            creditEntryRepository.save(creditEntry);
            creditHistoryRepository.save(creditHistories.get(creditHistories.size()-1));
        }
    }
    private PaymentResult toResult(
            Payment payment,
            List<String> failureMessages
    ){
        return new PaymentResult(
                payment.getId().value(),
                payment.getOrderId().value(),
                payment.getCustomerId().value(),
                payment.getPrice().getAmount(),
                payment.getPaymentStatus(),
                payment.getCreatedAt(),
                failureMessages
        );

    }
}
