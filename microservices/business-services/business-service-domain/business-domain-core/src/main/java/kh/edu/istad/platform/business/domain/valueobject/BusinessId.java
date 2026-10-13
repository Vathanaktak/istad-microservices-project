package kh.edu.istad.platform.business.domain.valueobject;

import java.util.UUID;

public record BusinessId(
        UUID value
) {
    public static BusinessId generate(){
        return new BusinessId(UUID.randomUUID());
    }
}
