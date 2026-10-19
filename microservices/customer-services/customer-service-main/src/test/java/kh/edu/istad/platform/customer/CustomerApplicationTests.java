package kh.edu.istad.platform.customer;

import kh.edu.istad.platform.customer.domain.dto.CustomerStatusReportResult;
import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerCommand;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateDeactivateCustomerCommand;
import kh.edu.istad.platform.customer.domain.usecase.CustomerStatusReportUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateDeactivateCustomerUseCase;
import kh.edu.istad.platform.customer.domain.usecase.InitiateFindAllUseCase;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.restapi.controller.CustomerController;
import kh.edu.istad.platform.customer.restapi.dto.CustomerStatusReportResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomerApplicationTests {

    @Autowired
    private InitiateCustomerUseCase initiateCustomerUseCase;

    @Autowired
    private InitiateFindAllUseCase initiateFindAllUseCase;

    @Autowired
    private InitiateDeactivateCustomerUseCase initiateDeactivateCustomerUseCase;

    @Autowired
    private CustomerStatusReportUseCase customerStatusReportUseCase;

    @Autowired
    private CustomerController customerController;

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
    void testFindAllWithoutFilter() {
        List<FindAllResult> results = initiateFindAllUseCase.findAll();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should return at least one customer");
        System.out.println("FindAll without filter count: " + results.size());
    }

    @Test
    void testFindAllFilterByName() {
        // Test filtering by name (matches username, familyName, or givenName case-insensitively)
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("Thai", null, null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers matching 'Thai'");
        for (FindAllResult customer : results) {
            boolean matches = (customer.familyName() != null && customer.familyName().toLowerCase().contains("thai"))
                    || (customer.givenName() != null && customer.givenName().toLowerCase().contains("thai"))
                    || (customer.username() != null && customer.username().toLowerCase().contains("thai"));
            assertTrue(matches, "Result must match name filter 'Thai': " + customer);
        }
        System.out.println("FindAll by name 'Thai' count: " + results.size());
    }

    @Test
    void testFindAllFilterByEmail() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, "example.com", null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers with email containing 'example.com'");
        for (FindAllResult customer : results) {
            assertTrue(customer.email() != null && customer.email().toLowerCase().contains("example.com"));
        }
        System.out.println("FindAll by email count: " + results.size());
    }

    @Test
    void testFindAllFilterByPhoneNumber() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, "012345678", null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers with phone '012345678'");
        for (FindAllResult customer : results) {
            assertTrue(customer.phoneNumber() != null && customer.phoneNumber().contains("012345678"));
        }
        System.out.println("FindAll by phone count: " + results.size());
    }

    @Test
    void testFindAllFilterByStatus() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, null, "ACTIVE");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find active customers");
        System.out.println("FindAll by status ACTIVE count: " + results.size());
    }

    @Test
    void testFindAllFilterCombined() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("tak", "example.com", null, "ACTIVE");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        System.out.println("FindAll combined filter count: " + results.size());
    }

    @Test
    void testFindAllFilterNoMatch() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("NON_EXISTENT_NAME_XYZ_12345", null, null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for non-matching filter");
    }

    @Test
    void testCustomerControllerFindAllWithoutParams() {
        // Test controller endpoint when all params are null (ensures no NullPointerException on status.name())
        List<FindAllResult> results = customerController.findAll(null, null, null, null);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testCustomerControllerFindAllWithParams() {
        // Test controller endpoint with name and status
        List<FindAllResult> results = customerController.findAll("Thai", null, null, CustomerStatus.ACTIVE);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testInitiateCustomerWithDefaultSilverSegment() {
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                "silver_user_" + System.currentTimeMillis(),
                "Silver",
                "User",
                "silver" + System.currentTimeMillis() + "@example.com",
                "011111111",
                null // default segment
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command);
        assertNotNull(result);
        assertNotNull(result.customerId());
        assertEquals("Silver", result.customerSegment(), "Default segment should be Silver");
    }

    @Test
    void testInitiateCustomerWithGoldSegment() {
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                "gold_user_" + System.currentTimeMillis(),
                "Gold",
                "User",
                "gold" + System.currentTimeMillis() + "@example.com",
                "022222222",
                "Gold"
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command);
        assertNotNull(result);
        assertNotNull(result.customerId());
        assertEquals("Gold", result.customerSegment(), "Segment should be Gold");
    }

    @Test
    void testInitiateCustomerWithPremiumSegment() {
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                "premium_user_" + System.currentTimeMillis(),
                "Premium",
                "User",
                "premium" + System.currentTimeMillis() + "@example.com",
                "033333333",
                "Premium"
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command);
        assertNotNull(result);
        assertNotNull(result.customerId());
        assertEquals("Premium", result.customerSegment(), "Segment should be Premium");
    }

    @Test
    void testFilterByCustomerSegment() {
        // Create a unique gold customer
        String username = "filter_gold_" + System.currentTimeMillis();
        initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username,
                "GoldFamily",
                "GoldGiven",
                username + "@example.com",
                "044444444",
                "Gold"
        ));

        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, null, null, "Gold");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria);
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers with Gold segment");
        for (FindAllResult c : results) {
            assertEquals("Gold", c.customerSegment(), "Every result must belong to Gold segment");
        }
    }

    @Test
    void testCustomerControllerFindAllWithCustomerSegmentParam() {
        List<FindAllResult> results = customerController.findAll(null, null, null, null, "Silver");
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers in Silver segment");
        for (FindAllResult c : results) {
            assertEquals("Silver", c.customerSegment());
        }
    }

    @Test
    void testCustomerStatusReport() {
        // Create a customer and deactivate to guarantee both ACTIVE and INACTIVE customers exist
        String username = "report_user_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username,
                "ReportFamily",
                "ReportGiven",
                username + "@example.com",
                "077777777"
        ));
        assertNotNull(created);
        assertNotNull(created.customerId());

        // Deactivate this customer
        initiateDeactivateCustomerUseCase.execute(new InitiateDeactivateCustomerCommand(created.customerId()));

        // Test use case report
        List<CustomerStatusReportResult> reportResults = customerStatusReportUseCase.execute();
        assertNotNull(reportResults);
        assertFalse(reportResults.isEmpty(), "Report must not be empty");

        // Verify all reported statuses match existing CustomerStatus enum values
        for (CustomerStatusReportResult item : reportResults) {
            assertNotNull(item.status());
            assertTrue(item.status().equals(CustomerStatus.ACTIVE.name()) || item.status().equals(CustomerStatus.INACTIVE.name()),
                    "Status must be an actual value defined in CustomerStatus: " + item.status());
            assertTrue(item.total() >= 0, "Total must be non-negative");
            System.out.println("Status: " + item.status() + " -> Total: " + item.total());
        }

        // Test controller endpoint: GET /customers/report (or /api/v1/customers/report)
        List<CustomerStatusReportResponse> controllerResponses = customerController.getCustomerStatusReport();
        assertNotNull(controllerResponses);
        assertEquals(reportResults.size(), controllerResponses.size());

        // Ensure both ACTIVE and INACTIVE counts are >= 1
        long activeCount = controllerResponses.stream()
                .filter(r -> CustomerStatus.ACTIVE.name().equals(r.status()))
                .mapToLong(CustomerStatusReportResponse::total)
                .findFirst()
                .orElse(0L);
        long inactiveCount = controllerResponses.stream()
                .filter(r -> CustomerStatus.INACTIVE.name().equals(r.status()))
                .mapToLong(CustomerStatusReportResponse::total)
                .findFirst()
                .orElse(0L);

        assertTrue(activeCount > 0, "Active count must be > 0");
        assertTrue(inactiveCount > 0, "Inactive count must be > 0");
    }
}
