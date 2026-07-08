package model;

/**
 * Represents a regular gym member with a standard membership plan.
 * This class holds data and basic validation only.
 * All business logic (attendance tracking, upgrades, payments)
 * should be in the Service layer.
 *
 * <p>Plans and Pricing (sourced from Plan enum):
 * <ul>
 *   <li>Basic: Rs. 6,500.00</li>
 *   <li>Standard: Rs. 12,500.00</li>
 *   <li>Deluxe: Rs. 18,500.00</li>
 * </ul>
 */
public class RegularMember extends GymMember {

    // ==============================================
    // Constants (data, not business logic)
    // ==============================================

    /**
     * Number of visits required before a member becomes eligible for upgrade.
     * This is a business rule but stored here as a constant for clarity.
     * The Service layer will use this value.
     */
    public static final int ATTENDANCE_LIMIT_FOR_UPGRADE = 30;

    /**
     * Loyalty points awarded per visit.
     * This is a business rule but stored here as a constant for clarity.
     * The Service layer will use this value.
     */
    public static final int LOYALTY_POINTS_PER_VISIT = 5;

    // ==============================================
    // Fields
    // ==============================================

    private String referralSource;
    private Plan currentPlan;
    private boolean eligibleForUpgrade;
    private int visitsSinceLastUpgrade;

    // ==============================================
    // Constructor
    // ==============================================

    /**
     * Creates a new RegularMember with the specified details.
     * New regular members start with the BASIC plan.
     *
     * @param id Unique member identifier
     * @param name Member's full name
     * @param location Member's location/address
     * @param phone Member's phone number
     * @param email Member's email address
     * @param gender Member's gender
     * @param dateOfBirth Member's date of birth (format: DD-MMM-YYYY)
     * @param membershipStartDate Date membership started (format: DD-MMM-YYYY)
     * @param referralSource How the member heard about the gym
     * @throws IllegalArgumentException if any validation fails
     */
    public RegularMember(int id, String name, String location, String phone,
                         String email, String gender, String dateOfBirth,
                         String membershipStartDate, String referralSource) {
        super(id, name, location, phone, email, gender, dateOfBirth, membershipStartDate);

        if (referralSource == null || referralSource.trim().isEmpty()) {
            throw new IllegalArgumentException("Referral source cannot be null or empty");
        }
        this.referralSource = referralSource.trim();
        this.currentPlan = Plan.BASIC;
        this.eligibleForUpgrade = false;
        this.visitsSinceLastUpgrade = 0;
    }

    // ==============================================
    // Getters
    // ==============================================

    public String getReferralSource() { return referralSource; }
    public Plan getCurrentPlan() { return currentPlan; }
    public boolean isEligibleForUpgrade() { return eligibleForUpgrade; }
    public int getVisitsSinceLastUpgrade() { return visitsSinceLastUpgrade; }

    // ==============================================
    // Public Setters (for Service layer)
    // ==============================================

    /**
     * Sets the current plan.
     * Public so Service layer can update it.
     *
     * @param plan The new plan (cannot be null)
     * @throws IllegalArgumentException if plan is null
     */
    public void setCurrentPlan(Plan plan) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan cannot be null");
        }
        this.currentPlan = plan;
    }

    /**
     * Sets the eligibility for upgrade.
     * public so Service layer can update it.
     *
     * @param eligible true if member is eligible for upgrade
     */
    public void setEligibleForUpgrade(boolean eligible) {
        this.eligibleForUpgrade = eligible;
    }

    /**
     * Sets the number of visits since last upgrade.
     * public so Service layer can update it.
     *
     * @param visits Number of visits (must be >= 0)
     * @throws IllegalArgumentException if visits < 0
     */
    public void setVisitsSinceLastUpgrade(int visits) {
        if (visits < 0) {
            throw new IllegalArgumentException("Visits cannot be negative");
        }
        this.visitsSinceLastUpgrade = visits;
    }

    /**
     * Sets the referral source.
     * public so Service layer can update it.
     *
     * @param referralSource The referral source (cannot be null or empty)
     * @throws IllegalArgumentException if referralSource is null or empty
     */
    public void setReferralSource(String referralSource) {
        if (referralSource == null || referralSource.trim().isEmpty()) {
            throw new IllegalArgumentException("Referral source cannot be null or empty");
        }
        this.referralSource = referralSource.trim();
    }

    // ==============================================
    // Member Type
    // ==============================================

    /**
     * Returns the member type as a string.
     * @return "Regular"
     */
    @Override
    public String getMemberType() {
        return "Regular";
    }

    // ==============================================
    // Soft Delete Override
    // ==============================================

    /**
     * Soft-deletes this regular member.
     * Resets plan to BASIC, clears eligibility, and resets visit counter.
     * Keeps attendance and loyalty points intact for historical records.
     *
     * <p><strong>Design Note:</strong> Referral source is NOT cleared on deletion
     * because it's valuable historical data for marketing analytics.
     * PII anonymization should be handled by a separate batch process
     * after a retention period (e.g., 30 days).</p>
     *
     * @param reason The reason for removal
     * @throws IllegalArgumentException if reason is null or empty
     */
    @Override
    public void softDelete(String reason) {
        super.softDelete(reason);
        // Reset regular member specific fields
        this.currentPlan = Plan.BASIC;
        this.eligibleForUpgrade = false;
        this.visitsSinceLastUpgrade = 0;
        // ReferralSource is KEPT (historical marketing data)
        // Attendance and loyalty are KEPT (historical data)
    }

    /**
     * Restores a soft-deleted regular member.
     *
     * <p><strong>Design Note:</strong> Members are restored with BASIC plan
     * regardless of their previous plan tier. This is intentional - removed
     * members lose their tier privileges and must re-earn eligibility.
     * Their attendance, loyalty points, and referral source are preserved
     * as historical data.</p>
     */
    @Override
    public void restore() {
        super.restore();
        // Restore default state
        this.currentPlan = Plan.BASIC;
        this.eligibleForUpgrade = false;
        this.visitsSinceLastUpgrade = 0;
        // ReferralSource is preserved (never cleared)
    }

    // ==============================================
    // toString Override
    // ==============================================

    @Override
    public String toString() {
        return String.format(
                "RegularMember[id=%d, name=%s, plan=%s, attendance=%d, eligible=%s, visitsSinceUpgrade=%d]",
                getId(), getName(), currentPlan.getDisplayName(),
                getAttendance(), eligibleForUpgrade, visitsSinceLastUpgrade
        );
    }
}