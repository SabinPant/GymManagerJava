package model;

/**
 * Represents a premium gym member with a fixed premium membership.
 * Premium members pay a fixed charge (Rs. 50,000) and receive a personal trainer.
 * They do not have plans (Basic/Standard/Deluxe) - they are already at the top tier.
 *
 * <p>All monetary values are stored in paisa (1/100th of a rupee) to avoid
 * floating-point precision issues.</p>
 *
 * <p><strong>Business Rules (handled by Service layer):</strong>
 * <ul>
 *   <li>Fixed premium charge: Rs. 50,000.00 (5,000,000 paisa)</li>
 *   <li>10% discount if full payment is made upfront</li>
 *   <li>Attendance tracking and loyalty points</li>
 * </ul>
 */
public class PremiumMember extends GymMember {

    // ==============================================
    // Constants
    // ==============================================

    /**
     * Fixed premium membership charge: Rs. 50,000.00
     * This is the total cost for premium membership.
     */
    public static final long PREMIUM_CHARGE_PAISA = 5000000L;

    /**
     * Discount rate for full payment: 10%
     * Stored as a percentage (e.g., 10 means 10%).
     * The Service layer will use this to calculate discount amount.
     */
    public static final int DISCOUNT_PERCENTAGE = 10;

    // ==============================================
    // Fields
    // ==============================================

    private String personalTrainer;
    private long discountAmountPaisa;

    // ==============================================
    // Constructor
    // ==============================================

    /**
     * Creates a new PremiumMember with the specified details.
     *
     * @param id Unique member identifier
     * @param name Member's full name
     * @param location Member's location/address
     * @param phone Member's phone number
     * @param email Member's email address
     * @param gender Member's gender
     * @param dateOfBirth Member's date of birth (format: DD-MMM-YYYY)
     * @param membershipStartDate Date membership started (format: DD-MMM-YYYY)
     * @param personalTrainer Name of the assigned personal trainer (cannot be null or empty)
     * @throws IllegalArgumentException if any validation fails
     */
    public PremiumMember(int id, String name, String location, String phone,
                         String email, String gender, String dateOfBirth,
                         String membershipStartDate, String personalTrainer) {
        super(id, name, location, phone, email, gender, dateOfBirth, membershipStartDate);

        if (personalTrainer == null || personalTrainer.trim().isEmpty()) {
            throw new IllegalArgumentException("Personal trainer cannot be null or empty");
        }
        this.personalTrainer = personalTrainer.trim();
        this.discountAmountPaisa = 0;
    }

    // ==============================================
    // Getters
    // ==============================================

    public String getPersonalTrainer() { return personalTrainer; }
    public long getDiscountAmountPaisa() { return discountAmountPaisa; }

    // ==============================================
    // Public Setters (for Service layer)
    // ==============================================

    /**
     * Sets the personal trainer name.
     * public so Service layer can update it.
     *
     * @param personalTrainer Name of the personal trainer (cannot be null or empty)
     * @throws IllegalArgumentException if personalTrainer is null or empty
     */
    public void setPersonalTrainer(String personalTrainer) {
        if (personalTrainer == null || personalTrainer.trim().isEmpty()) {
            throw new IllegalArgumentException("Personal trainer cannot be null or empty");
        }
        this.personalTrainer = personalTrainer.trim();
    }

    /**
     * Sets the discount amount in paisa.
     * public so Service layer can update it.
     *
     * <p><strong>Design Note:</strong> Once set, discount is NOT cleared
     * during soft-delete or restore. It's considered historical financial data
     * that must be preserved for accurate records.</p>
     *
     * @param discountAmountPaisa Discount amount (must be >= 0)
     * @throws IllegalArgumentException if discountAmountPaisa < 0
     */
    public void setDiscountAmountPaisa(long discountAmountPaisa) {
        if (discountAmountPaisa < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.discountAmountPaisa = discountAmountPaisa;
    }

    // ==============================================
    // Member Type
    // ==============================================

    /**
     * Returns the member type as a string.
     * @return "Premium"
     */
    @Override
    public String getMemberType() {
        return "Premium";
    }

    // ==============================================
    // Soft Delete Override
    // ==============================================

    /**
     * Soft-deletes this premium member.
     *
     * <p><strong>Design Note:</strong> Personal trainer name and discount amount
     * are PRESERVED as historical data for accurate financial records.
     * The discount is NOT reset because it's part of the member's financial history
     * and would cause incorrect billing if the member is later restored.</p>
     *
     * @param reason The reason for removal
     * @throws IllegalArgumentException if reason is null or empty
     */
    @Override
    public void softDelete(String reason) {
        super.softDelete(reason);
        // DO NOT reset discountAmountPaisa - it's financial history
        // DO NOT clear personalTrainer - it's historical data
    }

    /**
     * Restores a soft-deleted premium member.
     *
     * <p><strong>Design Note:</strong> Premium members remain Premium
     * when restored (they don't get downgraded to BASIC like Regular members).
     * Their trainer name and discount amount are preserved as historical data.</p>
     */
    @Override
    public void restore() {
        super.restore();
        // DO NOT reset discountAmountPaisa - it's financial history
        // DO NOT clear personalTrainer - it's historical data
        // Member remains PREMIUM (no plans to reset)
    }

    // ==============================================
    // toString Override
    // ==============================================

    @Override
    public String toString() {
        return String.format(
                "PremiumMember[id=%d, name=%s, trainer=%s, paid=%s, discount=%d paisa]",
                getId(), getName(), personalTrainer,
                isFullyPaid() ? "Fully Paid" : "Partial",
                discountAmountPaisa
        );
    }
}