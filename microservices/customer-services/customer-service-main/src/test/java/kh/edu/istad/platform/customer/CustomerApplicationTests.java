package kh.edu.istad.platform.customer;

import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomerApplicationTests {

    @Autowired
    private InitiateCustomerUseCase initiateCustomerUseCase;

    @Autowired
    private kh.edu.istad.platform.customer.domain.usecase.InitiateFindAllUseCase initiateFindAllUseCase;

    @Test
    void testInitiateCustomer() {
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                "vathanak_" + System.currentTimeMillis(),
                "Thai",
                "Vathanak",
                "vathanak" + System.currentTimeMillis() + "@example.com",
                "012345678"
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command);
        assertNotNull(result);
        assertNotNull(result.customerId());
        System.out.println("Customer created successfully with ID: " + result.customerId());
    }

    @Test
    void testFindAll() {
        var results = initiateFindAllUseCase.findAll();
        assertNotNull(results);
        System.out.println("FindAll count: " + results.size());
        results.forEach(r -> System.out.println("Customer: " + r));
    }

}
