package mediroute.model;

/**
 * Represents the urgency level of a hospital medical supply request.
 */
public enum UrgencyLevel {
    CRITICAL(1, "Critical Emergency - Immediate Dispatch"),
    URGENT(2, "Urgent - Priority Allocation"),
    NORMAL(3, "Normal - Standard Routine Supply");

    private final int level;
    private final String description;

    UrgencyLevel(int level, String description) {
        this.level = level;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public static UrgencyLevel fromString(String text) {
        if (text == null) return NORMAL;
        String upper = text.trim().toUpperCase();
        if (upper.contains("CRIT")) return CRITICAL;
        if (upper.contains("URG")) return URGENT;
        return NORMAL;
    }
}
