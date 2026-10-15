package kh.edu.istad.platform.payment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CreditEntryId(
        UUID value
) {
    public CreditEntryId{
        Objects.requireNonNull(value,"CreditEntryId cannot be null");
    }

}
