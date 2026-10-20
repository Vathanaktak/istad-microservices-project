package kh.edu.istad.platform.customer.domain.mapper;

import kh.edu.istad.platform.customer.domain.dto.CustomerSegmentResult;
import kh.edu.istad.platform.customer.domain.dto.FindAllResult;
import kh.edu.istad.platform.customer.domain.dto.InitiateCustomerResult;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueObject.CustomerSegment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerDataMapper {

    @Mapping(target = "customerId", source = "id.value")
    @Mapping(target = "phoneNumber", source = "phoneNumber.value")
    @Mapping(target = "customerSegment", source = "customerSegment.name")
    InitiateCustomerResult toInitiateCustomerResult(Customer customer);

    @Mapping(target = "customerId", source = "id.value")
    @Mapping(target = "phoneNumber", source = "phoneNumber.value")
    @Mapping(target = "customerSegment", source = "customerSegment.name")
    FindAllResult toFindAllResult(Customer customer);

    CustomerSegmentResult toCustomerSegmentResult(CustomerSegment segment);
}
