package kh.edu.istad.platform.customer.persistence.init;

import kh.edu.istad.platform.customer.persistence.document.CustomerSegmentDocument;
import kh.edu.istad.platform.customer.persistence.repository.CustomerSegmentMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerSegmentInitializer implements CommandLineRunner {

    private final CustomerSegmentMongoRepository customerSegmentMongoRepository;

    @Override
    public void run(String... args) {
        List<CustomerSegmentDocument> defaultSegments = List.of(
                new CustomerSegmentDocument("SEG-SILVER", "Silver", "Silver Tier Customer"),
                new CustomerSegmentDocument("SEG-GOLD", "Gold", "Gold Tier Customer"),
                new CustomerSegmentDocument("SEG-PREMIUM", "Premium", "Premium Tier Customer")
        );

        Flux.fromIterable(defaultSegments)
                .flatMap(segment -> customerSegmentMongoRepository.findById(segment.getId())
                        .switchIfEmpty(customerSegmentMongoRepository.save(segment)))
                .doOnNext(saved -> log.info("Initialized CustomerSegment: {} ({})", saved.getName(), saved.getId()))
                .blockLast();
    }
}
