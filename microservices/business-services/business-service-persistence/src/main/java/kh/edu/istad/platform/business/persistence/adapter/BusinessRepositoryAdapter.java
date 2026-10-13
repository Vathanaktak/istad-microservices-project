package kh.edu.istad.platform.business.persistence.adapter;

import kh.edu.istad.platform.business.domain.entity.Business;
import kh.edu.istad.platform.business.domain.port.out.BusinessRepository;
import kh.edu.istad.platform.business.domain.valueobject.BusinessContact;
import kh.edu.istad.platform.business.domain.valueobject.BusinessId;
import kh.edu.istad.platform.business.domain.valueobject.BusinessName;
import kh.edu.istad.platform.business.persistence.entity.BusinessDocument;
import kh.edu.istad.platform.business.persistence.repository.ReactiveMongoBusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor

public class BusinessRepositoryAdapter implements BusinessRepository {

    private final ReactiveMongoBusinessRepository businessRepository;

    @Override
    public Mono<Business> save(Business business) {

        BusinessDocument document = BusinessDocument.builder()
                .id(business.getId().value())
                .active(business.isActive())
                .email(business.getBusinessContact().email())
                .displayName(business.getBusinessName().displayName())
                .legalName(business.getBusinessName().legalName())
                .phoneNumber(business.getBusinessContact().phoneNumber())
                .build();

        return businessRepository.save(document).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByLegalName(String legalName) {
        return businessRepository.existsByLegalName(legalName);
    }

    private Business toDomain(BusinessDocument doc){
        return Business.builder()
                .businessId(new BusinessId(doc.getId()))
                .businessName(new BusinessName(doc.getLegalName(), doc.getDisplayName()))
                .businessContact(new BusinessContact(doc.getEmail(), doc.getPhoneNumber()))
                .active(doc.isActive())
                .build();
    }
}
