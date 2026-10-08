package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.service.CustomerDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class InitiateCustomerUseCase {
    @Autowired
    private final CustomerDomainService customerDomainService;

    public InitiateCustomerResult execute(InitiateCustomerCommand command) {
        log.info("initiate customer use case {}", command);
//        return new
        return new InitiateCustomerResult(UUID.randomUUID());
    }
}
