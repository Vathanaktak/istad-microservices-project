package kh.edu.istad.platform.customer.domain.valueObject;

public record CustomerSegment(
        String id,
        String name,
        String description
) {
    public static final CustomerSegment SILVER = new CustomerSegment("SEG-SILVER", "Silver", "Silver Tier Customer");
    public static final CustomerSegment GOLD = new CustomerSegment("SEG-GOLD", "Gold", "Gold Tier Customer");
    public static final CustomerSegment PREMIUM = new CustomerSegment("SEG-PREMIUM", "Premium", "Premium Tier Customer");

    public static CustomerSegment fromName(String name) {
        if (name == null || name.isBlank()) {
            return SILVER;
        }
        return switch (name.trim().toUpperCase()) {
            case "GOLD" -> GOLD;
            case "PREMIUM" -> PREMIUM;
            case "SILVER" -> SILVER;
            default -> new CustomerSegment("SEG-" + name.trim().toUpperCase(), name.trim(), name.trim() + " Tier Customer");
        };
    }
}
