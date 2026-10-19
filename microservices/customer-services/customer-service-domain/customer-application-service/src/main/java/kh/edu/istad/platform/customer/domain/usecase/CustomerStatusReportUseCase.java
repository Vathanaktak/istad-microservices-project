package kh.edu.istad.platform.customer.domain.usecase;

import kh.edu.istad.platform.customer.domain.dto.CustomerStatusReportResult;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomerStatusReportUseCase {

    private final CustomerRepository customerRepository;

    public List<CustomerStatusReportResult> execute() {
        log.info("Generating customer status report");
        return customerRepository.getCustomerStatusReport();
    }
}
