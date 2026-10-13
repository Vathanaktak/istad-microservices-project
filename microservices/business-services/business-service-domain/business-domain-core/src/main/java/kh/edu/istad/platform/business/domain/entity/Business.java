package kh.edu.istad.platform.business.domain.entity;

import kh.edu.istad.common.entity.AggregateRoot;
import kh.edu.istad.common.exception.DomainException;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;


public class Business extends AggregateRoot<BusinessId> {
    private BusinessName businessName;
    private BusinessContact businessContact;
    private boolean active;

//    public static Object builder() {
//    }

    public void validateBusiness(){
        if(super.getId() == null || getId().value() == null){
            throw new DomainException("Business ID must not be null");
        }
        if(businessName.displayName() == null || businessName.legalName()==null){
            throw new DomainException("Business name is required");
        }
        if(businessContact.phoneNumber() == null || businessContact.email() == null){
            throw new DomainException("Business contact is required");
        }
    }

    public void initializeBusiness() {
        setId(BusinessId.generate());
        this.active = true;
    }

    public BusinessName getBusinessName() {
        return businessName;
    }

    public BusinessContact getBusinessContact() {
        return businessContact;
    }

    public boolean isActive() {
        return active;
    }

    private Business(Builder builder) {
        super.setId(builder.businessId);
        businessName = builder.businessName;
        businessContact = builder.businessContact;
        active = builder.active;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private BusinessId businessId;
        private BusinessName businessName;
        private BusinessContact businessContact;
        private boolean active;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder businessId(BusinessId val) {
            businessId = val;
            return this;
        }

        public Builder businessName(BusinessName val) {
            businessName = val;
            return this;
        }

        public Builder businessContact(BusinessContact val) {
            businessContact = val;
            return this;
        }

        public Builder active(boolean val) {
            active = val;
            return this;
        }

        public Business build() {
            return new Business(this);
        }
    }
}
