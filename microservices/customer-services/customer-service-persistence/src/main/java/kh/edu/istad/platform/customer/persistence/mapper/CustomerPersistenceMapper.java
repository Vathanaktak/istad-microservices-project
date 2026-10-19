package kh.edu.istad.platform.customer.persistence.mapper;

import kh.edu.istad.common.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.valueObject.PhoneNumber;
import kh.edu.istad.platform.customer.persistence.document.CustomerDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.UUID;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerPersistenceMapper {

    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createAt", ignore = true)
    @Mapping(target = "modifyBy", ignore = true)
    @Mapping(target = "modifyAt", ignore = true)
    CustomerDocument toDocument(Customer customer);

    Customer toDomain(CustomerDocument customerDocument);

    default String mapCustomerIdToString(CustomerId customerId) {
        return customerId != null && customerId.value() != null ? customerId.value().toString() : null;
    }

    default CustomerId mapStringToCustomerId(String id) {
        return id != null ? new CustomerId(UUID.fromString(id)) : null;
    }

    default String mapPhoneNumberToString(PhoneNumber phoneNumber) {
        return phoneNumber != null ? phoneNumber.value() : null;
    }

    default PhoneNumber mapStringToPhoneNumber(String phoneNumber) {
        return phoneNumber != null ? new PhoneNumber(phoneNumber) : null;
    }
}
