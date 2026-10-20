package kh.edu.istad.platform.customer.restapi.controller;

import kh.edu.istad.platform.customer.domain.usecase.CustomerStatisticsUseCase;
import kh.edu.istad.platform.customer.restapi.dto.CustomerStatisticsResponse;
import kh.edu.istad.platform.customer.restapi.mapper.CustomerWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/statistics", "/statistics"})
public class CustomerStatisticsController {

    private final CustomerStatisticsUseCase customerStatisticsUseCase;
    private final CustomerWebMapper customerWebMapper;

    @GetMapping
    public Mono<CustomerStatisticsResponse> getStatistics() {
        return customerStatisticsUseCase.execute()
                .map(customerWebMapper::toStatisticsResponse);
    }
}
