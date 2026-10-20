package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerStatisticsResult;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerStatisticsUseCase {

    private final CustomerRepository customerRepository;

    public Mono<CustomerStatisticsResult> execute() {
        log.info("Generating customer statistics aggregation by status and segment");
        return customerRepository.getCustomerStatistics();
    }
}
