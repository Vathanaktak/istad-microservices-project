package kh.edu.istad.platform.payment.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record CreditHistoryId(UUID value) {

    public CreditHistoryId{
        Objects.requireNonNull(value,"CreditHistoryId cannot be null");
    }
}
