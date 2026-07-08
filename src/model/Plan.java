package model;

import java.util.Locale;
import java.util.Optional;

/**
 * Enum representing the different membership plans available in the gym.
 * This is the single source of truth for Regular member plan names and prices.
 * Premium members use PremiumMember.PREMIUM_CHARGE_PAISA as their pricing source.
 */
public enum Plan {
    BASIC("Basic", 650000),      // Rs. 6500.00
    STANDARD("Standard", 1250000), // Rs. 12500.00
    DELUXE("Deluxe", 1850000);    // Rs. 18500.00

    private final String displayName;
    private final long priceInPaisa;  // Stored as paisa (1 rupee = 100 paisa)
    private final String formattedDisplay;  // Precomputed for performance

    /**
     * Constructor for Plan enum
     * @param displayName User-friendly name for the plan (e.g., "Basic")
     * @param priceInPaisa The monthly price in paisa (must be positive)
     */
    Plan(String displayName, long priceInPaisa) {
        if (priceInPaisa <= 0) {
            throw new IllegalArgumentException("Price must be positive: " + priceInPaisa);
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Display name cannot be null or empty");
        }
        this.displayName = displayName;
        this.priceInPaisa = priceInPaisa;
        this.formattedDisplay = displayName + " (Rs." + formatPrice(priceInPaisa) + ")";
    }

    /**
     * Gets the display name of this plan (e.g., "Basic")
     * @return The user-friendly plan name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the price of this plan in paisa
     * @return The monthly price in paisa
     */
    public long getPriceInPaisa() {
        return priceInPaisa;
    }

    /**
     * Gets the plan from a string name (case-insensitive, trims whitespace)
     * @param name The plan name (e.g., "basic", "Standard")
     * @return Optional containing the matching Plan, or empty if not found
     */
    public static Optional<Plan> fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Plan.valueOf(name.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Checks if this plan is an upgrade from another plan
     * Compares by price rather than ordinal to avoid fragility if enum is reordered.
     * @param other The plan to compare against (must not be null)
     * @return true if this plan is higher tier (more expensive) than other
     * @throws NullPointerException if other is null
     */
    public boolean isUpgradeFrom(Plan other) {
        return this.priceInPaisa > other.priceInPaisa;
    }

    /**
     * Formats a price in paisa to a string with rupees and paisa
     * @param priceInPaisa The price in paisa
     * @return Formatted string like "6500.00"
     */
    private static String formatPrice(long priceInPaisa) {
        long rupees = priceInPaisa / 100;
        long paisa = Math.abs(priceInPaisa % 100);  // Use Math.abs for safety
        return String.format(Locale.US, "%d.%02d", rupees, paisa);
    }


    @Override
    public String toString() {
        return formattedDisplay;
    }
}