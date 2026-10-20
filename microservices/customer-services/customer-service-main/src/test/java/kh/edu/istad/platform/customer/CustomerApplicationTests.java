package kh.edu.istad.platform.customer;

import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.dto.*;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.port.out.CustomerRepository;
import kh.edu.istad.platform.customer.domain.port.out.CustomerSegmentRepository;
import kh.edu.istad.platform.customer.domain.usecase.*;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegmentFilterCriteria;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerStatus;
import kh.edu.istad.platform.customer.restapi.controller.CustomerController;
import kh.edu.istad.platform.customer.restapi.controller.CustomerSegmentController;
import kh.edu.istad.platform.customer.restapi.controller.CustomerStatisticsController;
import kh.edu.istad.platform.customer.restapi.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class CustomerApplicationTests {

    @Autowired
    private InitiateCustomerUseCase initiateCustomerUseCase;

    @Autowired
    private InitiateFindAllUseCase initiateFindAllUseCase;

    @Autowired
    private InitiateUpdateCustomerUseCase initiateUpdateCustomerUseCase;

    @Autowired
    private InitiateDeactivateCustomerUseCase initiateDeactivateCustomerUseCase;

    @Autowired
    private InitiateDeleteCustomerUseCase initiateDeleteCustomerUseCase;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerController customerController;

    @Autowired
    private CustomerSegmentController customerSegmentController;

    @Autowired
    private InitiateCreateCustomerSegmentUseCase initiateCreateCustomerSegmentUseCase;

    @Autowired
    private InitiateFindAllCustomerSegmentsUseCase initiateFindAllCustomerSegmentsUseCase;

    @Autowired
    private InitiateFindCustomerSegmentByIdUseCase initiateFindCustomerSegmentByIdUseCase;

    @Autowired
    private InitiateUpdateCustomerSegmentUseCase initiateUpdateCustomerSegmentUseCase;

    @Autowired
    private InitiateDeleteCustomerSegmentUseCase initiateDeleteCustomerSegmentUseCase;

    @Autowired
    private InitiateChangeCustomerSegmentUseCase initiateChangeCustomerSegmentUseCase;

    @Autowired
    private CustomerSegmentRepository customerSegmentRepository;

    @Autowired
    private CustomerStatisticsUseCase customerStatisticsUseCase;

    @Autowired
    private CustomerStatisticsController customerStatisticsController;

    // ==========================================
    // CREATE (C) Tests
    // ==========================================

    @Test
    void testCreateCustomerViaUseCase() {
        String username = "create_uc_" + System.currentTimeMillis();
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                username,
                "Family",
                "Given",
                username + "@example.com",
                "012345678"
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command).block();
        assertNotNull(result);
        assertNotNull(result.customerId());
        assertEquals("Silver", result.customerSegment(), "Default segment should be Silver");

        // Verify in DB via repository findById
        Customer persisted = customerRepository.findById(new CustomerId(result.customerId())).block();
        assertNotNull(persisted, "Customer should be persisted in DB");
        assertEquals(username, persisted.getUsername());
        assertEquals(CustomerStatus.ACTIVE, persisted.getStatus());
    }

    @Test
    void testCreateCustomerViaController() {
        String username = "create_ctrl_" + System.currentTimeMillis();
        CustomerInitiateRequest request = new CustomerInitiateRequest(
                username,
                "CtrlFamily",
                "CtrlGiven",
                username + "@example.com",
                "098765432",
                "Gold"
        );

        CustomerInitiateResponse response = customerController.initiateCustomer(request).block();
        assertNotNull(response);
        assertNotNull(response.customerId());
        assertEquals("Gold", response.customerSegment());

        // Verify in DB
        Customer persisted = customerRepository.findById(new CustomerId(response.customerId())).block();
        assertNotNull(persisted);
        assertEquals(username, persisted.getUsername());
        assertEquals("Gold", persisted.getCustomerSegment().name());
    }

    // ==========================================
    // READ (R) Tests
    // ==========================================

    @Test
    void testReadCustomerFindById() {
        String username = "read_id_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "ReadFam", "ReadGiv", username + "@example.com", "011223344"
        )).block();
        assertNotNull(created);

        Customer found = customerRepository.findById(new CustomerId(created.customerId())).block();
        assertNotNull(found, "Customer must be found by ID");
        assertEquals(created.customerId(), found.getId().value());
        assertEquals(username, found.getUsername());
        assertEquals("ReadFam", found.getFamilyName());
        assertEquals("ReadGiv", found.getGivenName());
    }

    @Test
    void testReadFindAllWithoutFilter() {
        List<FindAllResult> results = initiateFindAllUseCase.findAll().collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should return at least one customer");
    }

    @Test
    void testReadFindAllFilterByName() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("Thai", null, null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        for (FindAllResult customer : results) {
            boolean matches = (customer.familyName() != null && customer.familyName().toLowerCase().contains("thai"))
                    || (customer.givenName() != null && customer.givenName().toLowerCase().contains("thai"))
                    || (customer.username() != null && customer.username().toLowerCase().contains("thai"));
            assertTrue(matches, "Result must match name filter 'Thai'");
        }
    }

    @Test
    void testReadFindAllFilterByEmail() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, "example.com", null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        for (FindAllResult customer : results) {
            assertTrue(customer.email() != null && customer.email().toLowerCase().contains("example.com"));
        }
    }

    @Test
    void testReadFindAllFilterByPhoneNumber() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, "012345678", null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        for (FindAllResult customer : results) {
            assertTrue(customer.phoneNumber() != null && customer.phoneNumber().contains("012345678"));
        }
    }

    @Test
    void testReadFindAllFilterByStatus() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, null, "ACTIVE");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find active customers");
    }

    @Test
    void testReadFindAllFilterCombined() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("tak", "example.com", null, "ACTIVE");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
    }

    @Test
    void testReadFindAllFilterNoMatch() {
        CustomerFilterCriteria criteria = new CustomerFilterCriteria("NON_EXISTENT_NAME_XYZ_99999", null, null, null);
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for non-matching filter");
    }

    @Test
    void testReadCustomerControllerFindAllWithoutParams() {
        List<FindAllResult> results = customerController.findAll(null, null, null, null).collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testReadCustomerControllerFindAllWithParams() {
        List<FindAllResult> results = customerController.findAll("Thai", null, null, CustomerStatus.ACTIVE).collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    void testReadCustomerControllerFindAllWithCustomerSegmentParam() {
        List<FindAllResult> results = customerController.findAll(null, null, null, null, "Silver").collectList().block();
        assertNotNull(results);
        for (FindAllResult c : results) {
            assertEquals("Silver", c.customerSegment());
        }
    }

    // ==========================================
    // UPDATE (U) Tests
    // ==========================================

    @Test
    void testUpdateCustomerViaUseCase() {
        String username = "update_uc_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "OldFamily", "OldGiven", username + "@example.com", "055555555"
        )).block();
        assertNotNull(created);

        // Update name
        InitiateUpdateCustomerCommand updateCommand = new InitiateUpdateCustomerCommand(
                created.customerId(),
                "NewFamily",
                "NewGiven"
        );
        InitiateUpdateCustomerResult updateResult = initiateUpdateCustomerUseCase.execute(updateCommand).block();
        assertNotNull(updateResult);
        assertEquals("NewFamily", updateResult.familyName());
        assertEquals("NewGiven", updateResult.givenName());

        // Verify update persisted in DB
        Customer updatedInDb = customerRepository.findById(new CustomerId(created.customerId())).block();
        assertNotNull(updatedInDb);
        assertEquals("NewFamily", updatedInDb.getFamilyName());
        assertEquals("NewGiven", updatedInDb.getGivenName());
    }

    @Test
    void testUpdateCustomerViaControllerPut() {
        String username = "update_put_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "OrigFamily", "OrigGiven", username + "@example.com", "066666666"
        )).block();
        assertNotNull(created);

        // PUT update
        CustomerUpdateInitiateRequest request = new CustomerUpdateInitiateRequest("PutFamily", "PutGiven");
        CustomerUpdateInitiateResponse response = customerController.updateCustomer(created.customerId(), request).block();
        assertNotNull(response);
        assertEquals("PutFamily", response.familyName());
        assertEquals("PutGiven", response.givenName());

        // Verify in DB
        Customer inDb = customerRepository.findById(new CustomerId(created.customerId())).block();
        assertNotNull(inDb);
        assertEquals("PutFamily", inDb.getFamilyName());
        assertEquals("PutGiven", inDb.getGivenName());
    }

    @Test
    void testPatchCustomerViaController() {
        String username = "patch_ctrl_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "OrigFamily", "OrigGiven", username + "@example.com", "077777771"
        )).block();
        assertNotNull(created);

        // PATCH update
        CustomerUpdateInitiateRequest request = new CustomerUpdateInitiateRequest("PatchFamily", "PatchGiven");
        CustomerUpdateInitiateResponse response = customerController.patchCustomer(created.customerId(), request).block();
        assertNotNull(response);
        assertEquals("PatchFamily", response.familyName());
        assertEquals("PatchGiven", response.givenName());
    }

    @Test
    void testDeactivateCustomerViaUseCaseAndController() {
        String username = "deact_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "DeactFam", "DeactGiv", username + "@example.com", "088888888"
        )).block();
        assertNotNull(created);

        // Deactivate via Controller
        CustomerDeactivateInitiateResponse deactResponse = customerController.deactivateInitiateResponse(created.customerId()).block();
        assertNotNull(deactResponse);
        assertEquals(created.customerId(), deactResponse.customerId());

        // Verify status in DB is INACTIVE
        Customer inDb = customerRepository.findById(new CustomerId(created.customerId())).block();
        assertNotNull(inDb);
        assertEquals(CustomerStatus.INACTIVE, inDb.getStatus(), "Customer status must be INACTIVE in DB");
    }

    // ==========================================
    // DELETE (D) Tests
    // ==========================================

    @Test
    void testDeleteCustomerViaUseCaseAndVerifyDataLostInDb() {
        String username = "del_uc_" + System.currentTimeMillis();
        InitiateCustomerResult created = initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "DelFam", "DelGiv", username + "@example.com", "099999991"
        )).block();
        assertNotNull(created);

        UUID customerId = created.customerId();

        // 1. Verify customer exists before deletion
        Customer beforeDelete = customerRepository.findById(new CustomerId(customerId)).block();
        assertNotNull(beforeDelete, "Customer should exist before deletion");

        // 2. Perform deletion via Use Case
        Boolean deleteResult = initiateDeleteCustomerUseCase.deleteCustomerById(customerId.toString()).block();
        assertNotNull(deleteResult);
        assertTrue(deleteResult, "Delete operation must return true");

        // 3. VERIFY DATA IN DB IS ACTUALLY GONE
        Customer afterDelete = customerRepository.findById(new CustomerId(customerId)).block();
        assertNull(afterDelete, "Customer document must be completely removed from MongoDB after deletion");

        // 4. Deleting already-deleted customer must throw DomainException("Customer Not found")
        assertThrows(DomainException.class, () ->
                initiateDeleteCustomerUseCase.deleteCustomerById(customerId.toString()).block(),
                "Deleting a non-existent customer must throw DomainException"
        );
    }

    @Test
    void testDeleteCustomerViaControllerAndVerifyDataLostInDb() {
        String username = "del_ctrl_" + System.currentTimeMillis();
        CustomerInitiateResponse created = customerController.initiateCustomer(new CustomerInitiateRequest(
                username, "CtrlDelFam", "CtrlDelGiv", username + "@example.com", "099999992"
        )).block();
        assertNotNull(created);

        UUID customerId = created.customerId();

        // 1. Verify customer exists
        Customer beforeDelete = customerRepository.findById(new CustomerId(customerId)).block();
        assertNotNull(beforeDelete);

        // 2. Perform deletion via Controller DELETE /customers/{id}
        Boolean deleteResponse = customerController.deleteCustomerById(customerId.toString()).block();
        assertNotNull(deleteResponse);
        assertTrue(deleteResponse, "Controller DELETE must return true");

        // 3. VERIFY DATA IN DB IS GONE
        Customer afterDelete = customerRepository.findById(new CustomerId(customerId)).block();
        assertNull(afterDelete, "Customer document must NOT exist in MongoDB after deletion");

        // 4. Verify repeated delete throws DomainException
        assertThrows(DomainException.class, () ->
                customerController.deleteCustomerById(customerId.toString()).block()
        );
    }

    // ==========================================
    // Customer Segments & Status Report Tests
    // ==========================================

    @Test
    void testInitiateCustomerWithDefaultSilverSegment() {
        InitiateCustomerCommand command = new InitiateCustomerCommand(
                "silver_user_" + System.currentTimeMillis(),
                "Silver",
                "User",
                "silver" + System.currentTimeMillis() + "@example.com",
                "011111111",
                null
        );

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command).block();
        assertNotNull(result);
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

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command).block();
        assertNotNull(result);
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

        InitiateCustomerResult result = initiateCustomerUseCase.execute(command).block();
        assertNotNull(result);
        assertEquals("Premium", result.customerSegment(), "Segment should be Premium");
    }

    @Test
    void testFilterByCustomerSegment() {
        String username = "filter_gold_" + System.currentTimeMillis();
        initiateCustomerUseCase.execute(new InitiateCustomerCommand(
                username, "GoldFamily", "GoldGiven", username + "@example.com", "044444444", "Gold"
        )).block();

        CustomerFilterCriteria criteria = new CustomerFilterCriteria(null, null, null, null, "Gold");
        List<FindAllResult> results = initiateFindAllUseCase.findAll(criteria).collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find customers with Gold segment");
        for (FindAllResult c : results) {
            assertEquals("Gold", c.customerSegment(), "Every result must belong to Gold segment");
        }
    }

    // ==========================================
    // Customer Segment Tests (CRUD & Business Rules)
    // ==========================================

    @Test
    void testCreateCustomerSegmentViaUseCase() {
        String name = "Enterprise_" + System.currentTimeMillis();
        CreateCustomerSegmentCommand cmd = new CreateCustomerSegmentCommand(null, name, "Enterprise Tier");
        CustomerSegmentResult result = initiateCreateCustomerSegmentUseCase.execute(cmd).block();

        assertNotNull(result);
        assertNotNull(result.id());
        assertTrue(result.id().startsWith("SEG-"));
        assertEquals(name, result.name());
        assertEquals("Enterprise Tier", result.description());
    }

    @Test
    void testCreateCustomerSegmentViaController() {
        String name = "Diamond_" + System.currentTimeMillis();
        CustomerSegmentCreateRequest req = new CustomerSegmentCreateRequest(null, name, "Diamond Tier");
        CustomerSegmentResponse resp = customerSegmentController.createSegment(req).block();

        assertNotNull(resp);
        assertNotNull(resp.id());
        assertEquals(name, resp.name());
        assertEquals("Diamond Tier", resp.description());
    }

    @Test
    void testCreateCustomerSegmentDuplicateNameFails() {
        // Attempt creating a segment named 'Silver' (case-insensitive)
        CustomerSegmentCreateRequest req = new CustomerSegmentCreateRequest(null, "sIlVeR", "Duplicate test");
        DomainException ex = assertThrows(DomainException.class, () -> customerSegmentController.createSegment(req).block());
        assertTrue(ex.getMessage().contains("already exists"), "Expected duplicate name error, got: " + ex.getMessage());
    }

    @Test
    void testFindAllCustomerSegments() {
        List<CustomerSegmentResponse> segments = customerSegmentController.findAllSegments().collectList().block();
        assertNotNull(segments);
        assertFalse(segments.isEmpty());

        List<String> segmentNames = segments.stream().map(CustomerSegmentResponse::name).toList();
        assertTrue(segmentNames.stream().anyMatch("Silver"::equalsIgnoreCase), "Should contain Silver");
        assertTrue(segmentNames.stream().anyMatch("Gold"::equalsIgnoreCase), "Should contain Gold");
        assertTrue(segmentNames.stream().anyMatch("Premium"::equalsIgnoreCase), "Should contain Premium");
    }

    @Test
    void testFindAllCustomerSegmentsFilterByName() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments(null, "gold", null, null)
                .collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty(), "Should find segments matching 'gold'");
        for (CustomerSegmentResponse s : results) {
            assertTrue(s.name().toLowerCase().contains("gold"), "Segment name must contain 'gold'");
        }
    }

    @Test
    void testFindAllCustomerSegmentsFilterById() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments("SEG-SILVER", null, null, null)
                .collectList().block();
        assertNotNull(results);
        assertEquals(1, results.size(), "Should find exactly 1 segment for SEG-SILVER");
        assertEquals("Silver", results.get(0).name());
    }

    @Test
    void testFindAllCustomerSegmentsFilterByDescription() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments(null, null, "Tier", null)
                .collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        for (CustomerSegmentResponse s : results) {
            assertTrue(s.description().toLowerCase().contains("tier"));
        }
    }

    @Test
    void testFindAllCustomerSegmentsFilterByQuery() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments(null, null, null, "premium")
                .collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(s -> "Premium".equalsIgnoreCase(s.name())));
    }

    @Test
    void testFindAllCustomerSegmentsFilterNoMatch() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments(null, "NO_MATCH_SEG_99999", null, null)
                .collectList().block();
        assertNotNull(results);
        assertTrue(results.isEmpty(), "Should return empty list for unmatched filter");
    }

    @Test
    void testFindAllCustomerSegmentsFilterCombined() {
        List<CustomerSegmentResponse> results = customerSegmentController.findAllSegments("SEG-", "Silver", null, null)
                .collectList().block();
        assertNotNull(results);
        assertFalse(results.isEmpty());
        for (CustomerSegmentResponse s : results) {
            assertTrue(s.id().startsWith("SEG-"));
            assertTrue(s.name().toLowerCase().contains("silver"));
        }
    }

    @Test
    void testFindCustomerSegmentByIdAndName() {
        // Find by ID
        CustomerSegmentResponse byId = customerSegmentController.findSegmentById("SEG-SILVER").block();
        assertNotNull(byId);
        assertEquals("Silver", byId.name());

        // Find by Name (case-insensitive fallback)
        CustomerSegmentResponse byName = customerSegmentController.findSegmentById("gold").block();
        assertNotNull(byName);
        assertEquals("SEG-GOLD", byName.id());

        // Find non-existent
        assertThrows(DomainException.class, () -> customerSegmentController.findSegmentById("NON_EXISTENT_SEG_999").block());
    }

    @Test
    void testUpdateCustomerSegmentSuccess() {
        String originalName = "Bronze_" + System.currentTimeMillis();
        CustomerSegmentResponse created = customerSegmentController.createSegment(
                new CustomerSegmentCreateRequest(null, originalName, "Original Bronze")
        ).block();
        assertNotNull(created);

        String updatedName = "BronzePlus_" + System.currentTimeMillis();
        CustomerSegmentResponse updated = customerSegmentController.updateSegment(
                created.id(),
                new CustomerSegmentUpdateRequest(updatedName, "Updated Bronze Tier")
        ).block();

        assertNotNull(updated);
        assertEquals(created.id(), updated.id(), "Segment ID should remain constant");
        assertEquals(updatedName, updated.name());
        assertEquals("Updated Bronze Tier", updated.description());
    }

    @Test
    void testUpdateCustomerSegmentDuplicateNameConflictFails() {
        String name = "Ruby_" + System.currentTimeMillis();
        CustomerSegmentResponse created = customerSegmentController.createSegment(
                new CustomerSegmentCreateRequest(null, name, "Ruby Tier")
        ).block();
        assertNotNull(created);

        // Attempt updating Ruby to 'Gold' which already exists
        CustomerSegmentUpdateRequest req = new CustomerSegmentUpdateRequest("Gold", "Conflict description");
        DomainException ex = assertThrows(DomainException.class, () -> customerSegmentController.updateSegment(created.id(), req).block());
        assertTrue(ex.getMessage().contains("already exists"), "Expected conflict error, got: " + ex.getMessage());
    }

    @Test
    void testUpdateDefaultCustomerSegmentRenamingFails() {
        // Default system segment SEG-SILVER cannot be renamed
        CustomerSegmentUpdateRequest req = new CustomerSegmentUpdateRequest("SilverRenamed", "Attempt rename");
        DomainException ex = assertThrows(DomainException.class, () -> customerSegmentController.updateSegment("SEG-SILVER", req).block());
        assertTrue(ex.getMessage().contains("cannot be renamed"), "Expected default rename error, got: " + ex.getMessage());
    }

    @Test
    void testDeleteDefaultCustomerSegmentFails() {
        // Default system segment SEG-SILVER cannot be deleted
        DomainException ex = assertThrows(DomainException.class, () -> customerSegmentController.deleteSegmentById("SEG-SILVER").block());
        assertTrue(ex.getMessage().contains("Cannot delete default system segment"), "Expected default delete error, got: " + ex.getMessage());
    }

    @Test
    void testDeleteCustomerSegmentInUseFails() {
        // 1. Create a distinct segment
        String segmentName = "InUseSeg_" + System.currentTimeMillis();
        CustomerSegmentResponse segment = customerSegmentController.createSegment(
                new CustomerSegmentCreateRequest(null, segmentName, "In Use Segment")
        ).block();
        assertNotNull(segment);

        // 2. Create customer assigned to this segment
        String username = "inuse_cust_" + System.currentTimeMillis();
        CustomerInitiateResponse customer = customerController.initiateCustomer(new CustomerInitiateRequest(
                username, "InUseFamily", "InUseGiven", username + "@example.com", "011223344", segment.name()
        )).block();
        assertNotNull(customer);

        // 3. Attempt deleting this in-use segment -> MUST throw DomainException
        DomainException ex = assertThrows(DomainException.class, () -> customerSegmentController.deleteSegmentById(segment.id()).block());
        assertTrue(ex.getMessage().contains("customers are currently assigned to it"), "Expected in-use error, got: " + ex.getMessage());
    }

    @Test
    void testDeleteCustomerSegmentSuccess() {
        // 1. Create a segment not assigned to anyone
        String segmentName = "UnusedSeg_" + System.currentTimeMillis();
        CustomerSegmentResponse segment = customerSegmentController.createSegment(
                new CustomerSegmentCreateRequest(null, segmentName, "Unused Segment")
        ).block();
        assertNotNull(segment);

        // 2. Delete it
        Boolean deleted = customerSegmentController.deleteSegmentById(segment.id()).block();
        assertTrue(Boolean.TRUE.equals(deleted), "Segment deletion should return true");

        // 3. Verify it no longer exists
        assertThrows(DomainException.class, () -> customerSegmentController.findSegmentById(segment.id()).block());
    }

    @Test
    void testChangeCustomerSegmentViaUseCaseAndController() {
        // 1. Create customer with default segment (Silver)
        String username = "change_seg_cust_" + System.currentTimeMillis();
        CustomerInitiateResponse customer = customerController.initiateCustomer(new CustomerInitiateRequest(
                username, "ChangeFamily", "ChangeGiven", username + "@example.com", "055667788"
        )).block();
        assertNotNull(customer);
        assertEquals("Silver", customer.customerSegment());

        // 2. Change customer segment to Gold via UseCase
        InitiateCustomerResult changedToGold = initiateChangeCustomerSegmentUseCase.execute(
                new ChangeCustomerSegmentCommand(customer.customerId(), "SEG-GOLD")
        ).block();
        assertNotNull(changedToGold);
        assertEquals("Gold", changedToGold.customerSegment());

        // 3. Change customer segment to Premium via Controller PATCH /{customerId}/segment
        CustomerInitiateResponse changedToPremium = customerController.changeCustomerSegment(
                customer.customerId(),
                new CustomerChangeSegmentRequest("SEG-PREMIUM")
        ).block();
        assertNotNull(changedToPremium);
        assertEquals("Premium", changedToPremium.customerSegment());

        // 4. Verify in DB via repository findById
        Customer persisted = customerRepository.findById(new CustomerId(customer.customerId())).block();
        assertNotNull(persisted);
        assertEquals("Premium", persisted.getCustomerSegment().name());
    }

    @Test
    void testChangeCustomerSegmentInvalidSegmentFails() {
        String username = "inv_seg_cust_" + System.currentTimeMillis();
        CustomerInitiateResponse customer = customerController.initiateCustomer(new CustomerInitiateRequest(
                username, "InvFamily", "InvGiven", username + "@example.com", "099887766"
        )).block();
        assertNotNull(customer);

        CustomerChangeSegmentRequest request = new CustomerChangeSegmentRequest("NON_EXISTING_SEGMENT_123");
        DomainException ex = assertThrows(DomainException.class, () ->
                customerController.changeCustomerSegment(customer.customerId(), request).block()
        );
        assertTrue(ex.getMessage().contains("Customer segment not found"), "Expected not found error, got: " + ex.getMessage());
    }

    @Test
    void testChangeCustomerSegmentInvalidCustomerFails() {
        CustomerChangeSegmentRequest request = new CustomerChangeSegmentRequest("SEG-GOLD");
        DomainException ex = assertThrows(DomainException.class, () ->
                customerController.changeCustomerSegment(UUID.randomUUID(), request).block()
        );
        assertTrue(ex.getMessage().contains("Customer not found"), "Expected customer not found error, got: " + ex.getMessage());
    }

    // ==========================================
    // Aggregation / Statistics Tests
    // ==========================================

    @Test
    void testCustomerStatisticsAggregationViaUseCase() {
        CustomerStatisticsResult stats = customerStatisticsUseCase.execute().block();
        assertNotNull(stats);
        assertTrue(stats.totalCustomers() > 0, "Total customers must be > 0");

        // Verify byStatus
        assertNotNull(stats.byStatus());
        assertFalse(stats.byStatus().isEmpty());
        long statusSum = stats.byStatus().stream().mapToLong(CustomerStatisticsResult.StatusCount::total).sum();
        assertEquals(stats.totalCustomers(), statusSum, "Sum of byStatus counts must match totalCustomers");

        boolean hasActive = stats.byStatus().stream().anyMatch(s -> "ACTIVE".equals(s.status()));
        boolean hasInactive = stats.byStatus().stream().anyMatch(s -> "INACTIVE".equals(s.status()));
        assertTrue(hasActive, "Must have ACTIVE status in statistics");
        assertTrue(hasInactive, "Must have INACTIVE status in statistics");

        // Verify bySegment
        assertNotNull(stats.bySegment());
        assertFalse(stats.bySegment().isEmpty());
        long segmentSum = stats.bySegment().stream().mapToLong(CustomerStatisticsResult.SegmentCount::total).sum();
        assertEquals(stats.totalCustomers(), segmentSum, "Sum of bySegment counts must match totalCustomers");

        // Verify byStatusAndSegment
        assertNotNull(stats.byStatusAndSegment());
        assertFalse(stats.byStatusAndSegment().isEmpty());
        long statusSegmentSum = stats.byStatusAndSegment().stream().mapToLong(CustomerStatisticsResult.StatusSegmentCount::total).sum();
        assertEquals(stats.totalCustomers(), statusSegmentSum, "Sum of byStatusAndSegment counts must match totalCustomers");

        for (CustomerStatisticsResult.StatusSegmentCount item : stats.byStatusAndSegment()) {
            assertNotNull(item.status());
            assertNotNull(item.segmentId());
            assertNotNull(item.segmentName());
            assertTrue(item.total() > 0);
        }
    }

    @Test
    void testCustomerStatisticsEndpointsViaControllers() {
        // Test CustomerController /customers/statistics
        CustomerStatisticsResponse resp1 = customerController.getCustomerStatistics().block();
        assertNotNull(resp1);
        assertTrue(resp1.totalCustomers() > 0);
        assertFalse(resp1.byStatus().isEmpty());
        assertFalse(resp1.bySegment().isEmpty());
        assertFalse(resp1.byStatusAndSegment().isEmpty());

        // Test CustomerStatisticsController /statistics
        CustomerStatisticsResponse resp2 = customerStatisticsController.getStatistics().block();
        assertNotNull(resp2);
        assertEquals(resp1.totalCustomers(), resp2.totalCustomers());
        assertEquals(resp1.byStatus().size(), resp2.byStatus().size());
        assertEquals(resp1.bySegment().size(), resp2.bySegment().size());
        assertEquals(resp1.byStatusAndSegment().size(), resp2.byStatusAndSegment().size());
    }

    @Test
    void testCustomerStatisticsIncrementsOnNewCustomer() {
        CustomerStatisticsResult before = customerStatisticsUseCase.execute().block();
        assertNotNull(before);
        long initialTotal = before.totalCustomers();

        // Create new customer with Gold segment
        String username = "stats_gold_" + System.currentTimeMillis();
        customerController.initiateCustomer(new CustomerInitiateRequest(
                username, "StatsFamily", "StatsGiven", username + "@example.com", "091122334", "Gold"
        )).block();

        CustomerStatisticsResult after = customerStatisticsUseCase.execute().block();
        assertNotNull(after);
        assertEquals(initialTotal + 1, after.totalCustomers(), "Total customer count should increase by 1");

        long goldBefore = before.bySegment().stream()
                .filter(s -> "Gold".equalsIgnoreCase(s.segmentName()) || "SEG-GOLD".equalsIgnoreCase(s.segmentId()))
                .mapToLong(CustomerStatisticsResult.SegmentCount::total)
                .findFirst().orElse(0L);

        long goldAfter = after.bySegment().stream()
                .filter(s -> "Gold".equalsIgnoreCase(s.segmentName()) || "SEG-GOLD".equalsIgnoreCase(s.segmentId()))
                .mapToLong(CustomerStatisticsResult.SegmentCount::total)
                .findFirst().orElse(0L);

        assertEquals(goldBefore + 1, goldAfter, "Gold segment total must increase by 1");
    }
}
