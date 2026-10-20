package kh.edu.istad.platform.customer.domain.valueObject;

import kh.edu.istad.platform.customer.domain.exception.CustomerDomainException;

public record CustomerSegment(
        String id,
        String name,
        String description
) {
    public static final CustomerSegment SILVER = new CustomerSegment("SEG-SILVER", "Silver", "Silver Tier Customer");
    public static final CustomerSegment GOLD = new CustomerSegment("SEG-GOLD", "Gold", "Gold Tier Customer");
    public static final CustomerSegment PREMIUM = new CustomerSegment("SEG-PREMIUM", "Premium", "Premium Tier Customer");

    public boolean isDefault() {
        return "SEG-SILVER".equalsIgnoreCase(id) || "Silver".equalsIgnoreCase(name);
    }

    public void validateCanDelete() {
        if (isDefault()) {
            throw new CustomerDomainException("Cannot delete default system segment: " + name);
        }
    }

    public void validateCanRename(String newName) {
        if (isDefault() && !name.equalsIgnoreCase(newName)) {
            throw new CustomerDomainException("Default customer segment '" + name + "' cannot be renamed");
        }
    }

    public static String generateId(String name) {
        if (name == null || name.isBlank()) {
            return "SEG-CUSTOM";
        }
        String clean = name.trim().toUpperCase().replaceAll("[^A-Z0-9]", "_");
        return "SEG-" + clean;
    }

    public static CustomerSegment fromName(String name) {
        if (name == null || name.isBlank()) {
            return SILVER;
        }
        return switch (name.trim().toUpperCase()) {
            case "GOLD" -> GOLD;
            case "PREMIUM" -> PREMIUM;
            case "SILVER" -> SILVER;
            default -> new CustomerSegment(generateId(name), name.trim(), name.trim() + " Tier Customer");
        };
    }
}
