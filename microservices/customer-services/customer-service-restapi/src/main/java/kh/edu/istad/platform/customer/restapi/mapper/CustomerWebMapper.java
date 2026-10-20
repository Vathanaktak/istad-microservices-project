package kh.edu.istad.platform.customer.restapi.mapper;

import kh.edu.istad.platform.customer.domain.dto.*;
import kh.edu.istad.platform.customer.restapi.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CustomerWebMapper {

    InitiateCustomerCommand toCommand(CustomerInitiateRequest request);
    CustomerInitiateResponse toResponse(InitiateCustomerResult result);

    @Mapping(target = "customerId", source = "customerId")
    InitiateUpdateCustomerCommand toUpdateCommand(UUID customerId, CustomerUpdateInitiateRequest request);

    CustomerUpdateInitiateResponse toUpdateResponse(InitiateUpdateCustomerResult result);

    CustomerDeactivateInitiateResponse toDeactivateResponse(InitiateDeactivateCustomerResult result);

    CreateCustomerSegmentCommand toSegmentCommand(CustomerSegmentCreateRequest request);

    @Mapping(target = "id", source = "id")
    UpdateCustomerSegmentCommand toSegmentUpdateCommand(String id, CustomerSegmentUpdateRequest request);

    CustomerSegmentResponse toSegmentResponse(CustomerSegmentResult result);

    @Mapping(target = "customerId", source = "customerId")
    @Mapping(target = "segmentId", source = "request.segmentId")
    ChangeCustomerSegmentCommand toChangeSegmentCommand(UUID customerId, CustomerChangeSegmentRequest request);

    CustomerStatisticsResponse toStatisticsResponse(CustomerStatisticsResult result);
}
