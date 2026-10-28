package kh.edu.istad.platform.order;

import kh.edu.istad.platform.order.persistence.repository.BusinessReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.test.StepVerifier;

@SpringBootTest(properties = {
        "spring.r2dbc.url=r2dbc:postgresql://localhost:9543/db_order",
        "spring.r2dbc.username=iteuser",
        "spring.r2dbc.password=qwerqwer"
})
public class BusinessTests {

    @SpringBootApplication
    @EnableR2dbcRepositories(basePackages = "kh.edu.istad.platform.order.persistence.repository")
    static class TestConfig {
    }

    @Autowired
    private BusinessReactiveRepository businessReactiveRepository;

    @Test
    void testFIndBusiness(){
        StepVerifier.create(businessReactiveRepository.findAll())
                .thenConsumeWhile(businessEntity -> {
                    System.out.println("Business found: " + businessEntity.getBusinessName() + " | Price: " + businessEntity.getProductPrice());
                    return true;
                })
                .verifyComplete();
    }

}
