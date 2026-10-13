package kh.edu.istad.platform.business.domain.event;

import kh.edu.istad.common.event.DomainEvent;
import kh.edu.istad.platform.business.domain.entity.Business;

import java.time.ZonedDateTime;

public record BusinessRegisteredEvent(
        Business business,
        ZonedDateTime createAt
) implements DomainEvent<Business> {
}
