package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Locale;

/**
 * Abstract base class representing a gym member.
 * This class handles common attributes and basic validation.
 * All business logic should be in the Service layer.
 *
 * <p>All monetary values are stored in paisa (1/100th of a rupee) to avoid
 * floating-point precision issues.</p>
 *
 * <p><strong>Important:</strong> This class uses ID as the natural identity.
 * ID is immutable after construction - no setter exists. Therefore, instances
 * can safely be used as keys in hash-based collections (HashSet, HashMap).</p>
 */
public abstract class GymMember {

    // ==============================================
    // Private Fields
    // ==============================================

    private final int id;
    private String name;
    private String location;
    private String phone;
    private String email;
    private String gender;
    private String dateOfBirth;
    private String membershipStartDate;

    // Membership Tracking
    private int attendance;
    private int loyaltyPoints;
    private boolean activeStatus;

    // Soft Delete Fields
    private boolean isRemoved;
    private String removalReason;

    // Payment Tracking (in paisa)
    private long paidAmountPaisa;
    private boolean fullyPaid;

    // ==============================================
    // Validation Constants
    // ==============================================

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9+\\-\\s]{10,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d-MMM-uuuu", Locale.US)
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern GENDER_PATTERN =
            Pattern.compile("^(Male|Female|Other|Prefer not to say)$", Pattern.CASE_INSENSITIVE);

    // ==============================================
    // Constructor
    // ==============================================

    public GymMember(int id, String name, String location, String phone,
                     String email, String gender, String dateOfBirth,
                     String membershipStartDate) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID must be positive");
        }
        this.id = id;

        setName(name);
        setLocation(location);
        setPhone(phone);
        setEmail(email);
        setGender(gender);
        setDateOfBirth(dateOfBirth);
        setMembershipStartDate(membershipStartDate);

        this.attendance = 0;
        this.loyaltyPoints = 0;
        this.activeStatus = false;
        this.isRemoved = false;
        this.removalReason = "";
        this.paidAmountPaisa = 0;
        this.fullyPaid = false;
    }

    // ==============================================
    // Getters (all public)
    // ==============================================

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getMembershipStartDate() { return membershipStartDate; }
    public int getAttendance() { return attendance; }
    public int getLoyaltyPoints() { return loyaltyPoints; }
    public boolean isActive() { return activeStatus; }
    public boolean isRemoved() { return isRemoved; }
    public String getRemovalReason() { return removalReason; }
    public long getPaidAmountPaisa() { return paidAmountPaisa; }
    public boolean isFullyPaid() { return fullyPaid; }

    // ==============================================
    // Public Setters (for subclasses and Service layer)
    // ==============================================

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        this.location = location.trim();
    }

    public void setPhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.phone = phone.trim();
    }

    public void setEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim().toLowerCase();
    }

    public void setGender(String gender) {
        if (gender == null || !GENDER_PATTERN.matcher(gender).matches()) {
            throw new IllegalArgumentException(
                    "Gender must be: Male, Female, Other, or Prefer not to say"
            );
        }
        this.gender = gender.substring(0, 1).toUpperCase() +
                gender.substring(1).toLowerCase();
    }

    /**
     * Sets the member's date of birth with strict validation.
     * Validates format, actual calendar date, and reasonable ranges.
     * Also ensures DOB is before membership start date (if already set).
     *
     * @param dateOfBirth Date of birth (format: DD-MMM-YYYY, e.g., 15-Jan-1990)
     * @throws IllegalArgumentException if date format is invalid, date doesn't exist,
     *         date is outside reasonable range, or DOB is after membership start date
     */
    public void setDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Date of birth cannot be null");
        }
        try {
            LocalDate parsed = LocalDate.parse(dateOfBirth, DATE_FORMATTER);
            LocalDate today = LocalDate.now();

            // Check: DOB cannot be in the future
            if (parsed.isAfter(today)) {
                throw new IllegalArgumentException("Date of birth cannot be in the future");
            }

            // Check: DOB cannot be more than 100 years ago (reasonable limit)
            LocalDate oneHundredYearsAgo = today.minusYears(100);
            if (parsed.isBefore(oneHundredYearsAgo)) {
                throw new IllegalArgumentException(
                        "Date of birth cannot be more than 100 years ago. Please check the year."
                );
            }

            // Bidirectional check: If membershipStartDate is already set, DOB must be before it
            if (this.membershipStartDate != null) {
                LocalDate start = LocalDate.parse(this.membershipStartDate, DATE_FORMATTER);
                if (parsed.isAfter(start) || parsed.isEqual(start)) {
                    throw new IllegalArgumentException(
                            "Date of birth must be before membership start date"
                    );
                }
            }

            this.dateOfBirth = dateOfBirth;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format or date does not exist. Use DD-MMM-YYYY (e.g., 15-Jan-1990)"
            );
        }
    }

    /**
     * Sets the membership start date with strict validation.
     * Validates format, actual calendar date, and ensures it's after date of birth.
     *
     * @param membershipStartDate Start date (format: DD-MMM-YYYY, e.g., 01-Jan-2024)
     * @throws IllegalArgumentException if date format is invalid, date doesn't exist,
     *         or start date is before or equal to date of birth
     */
    public void setMembershipStartDate(String membershipStartDate) {
        if (membershipStartDate == null) {
            throw new IllegalArgumentException("Membership start date cannot be null");
        }
        try {
            LocalDate parsed = LocalDate.parse(membershipStartDate, DATE_FORMATTER);

            // Bidirectional check: If DOB is already set, membership start must be after it
            if (this.dateOfBirth != null) {
                LocalDate dob = LocalDate.parse(this.dateOfBirth, DATE_FORMATTER);
                if (parsed.isBefore(dob) || parsed.isEqual(dob)) {
                    throw new IllegalArgumentException(
                            "Membership start date must be after date of birth"
                    );
                }
            }

            this.membershipStartDate = membershipStartDate;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Invalid date format or date does not exist. Use DD-MMM-YYYY (e.g., 01-Jan-2024)"
            );
        }
    }

    /**
     * Public setter for attendance (used by Service layer).
     * @param attendance New attendance count (must be >= 0)
     * @throws IllegalArgumentException if attendance < 0
     */
    public void setAttendance(int attendance) {
        if (attendance < 0) {
            throw new IllegalArgumentException("Attendance cannot be negative");
        }
        this.attendance = attendance;
    }

    /**
     * Public setter for loyalty points (used by Service layer).
     * @param loyaltyPoints New loyalty points (must be >= 0)
     * @throws IllegalArgumentException if loyaltyPoints < 0
     */
    public void setLoyaltyPoints(int loyaltyPoints) {
        if (loyaltyPoints < 0) {
            throw new IllegalArgumentException("Loyalty points cannot be negative");
        }
        this.loyaltyPoints = loyaltyPoints;
    }

    /**
     * public setter for paid amount in paisa (used by Service layer).
     * @param paidAmountPaisa New paid amount (must be >= 0)
     * @throws IllegalArgumentException if amount < 0
     */
    public void setPaidAmountPaisa(long paidAmountPaisa) {
        if (paidAmountPaisa < 0) {
            throw new IllegalArgumentException("Paid amount cannot be negative");
        }
        this.paidAmountPaisa = paidAmountPaisa;
    }

    /**
     * public setter for fully paid status (used by Service layer).
     * @param fullyPaid New fully paid status
     */
    public void setFullyPaid(boolean fullyPaid) {
        this.fullyPaid = fullyPaid;
    }

    // ==============================================
    // Member Type (abstract – must be implemented by subclasses)
    // ==============================================

    /**
     * Returns the member type as a string.
     * Subclasses must override this with their specific type.
     *
     * @return The member type (e.g., "Regular", "Premium")
     */
    public abstract String getMemberType();

    // ==============================================
    // Simple State Methods (non-business logic)
    // ==============================================

    /**
     * Activates the membership.
     * Can only activate if member is not removed.
     *
     * @throws IllegalStateException if member is removed
     */
    public void activateMembership() {
        if (isRemoved) {
            throw new IllegalStateException("Cannot activate a removed member");
        }
        this.activeStatus = true;
    }

    /**
     * Deactivates the membership.
     * Does nothing if already inactive.
     */
    public void deactivateMembership() {
        this.activeStatus = false;
    }

    /**
     * Soft-deletes this member by marking them as removed.
     * Keeps ALL historical data intact (attendance, loyalty points, payment history).
     * Only resets membership status to inactive.
     *
     * @param reason The reason for removal (cannot be null or empty)
     * @throws IllegalArgumentException if reason is null or empty
     */
    public void softDelete(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Removal reason cannot be empty");
        }
        this.isRemoved = true;
        this.removalReason = reason.trim();
        this.activeStatus = false;
        // Attendance, loyaltyPoints, paidAmountPaisa, fullyPaid are KEPT (historical data)
    }

    /**
     * Restores a soft-deleted member.
     * Clears the removal reason and sets removed flag to false.
     * Member remains inactive until explicitly activated.
     */
    public void restore() {
        this.isRemoved = false;
        this.removalReason = "";
        // Do not automatically reactivate - caller must explicitly activate
    }

    // ==============================================
    // Utility Methods
    // ==============================================

    /**
     * Gets a summary of the member's status.
     * Useful for display in tables and logs.
     *
     * @return A formatted status string
     */
    public String getStatusSummary() {
        if (isRemoved) {
            return "REMOVED: " + removalReason;
        }
        return activeStatus ? "ACTIVE" : "INACTIVE";
    }

    /**
     * Checks if this member is active and not removed.
     * @return true if member can use gym services
     */
    public boolean isEligible() {
        return activeStatus && !isRemoved;
    }

    // ==============================================
    // Object Overrides
    // ==============================================

    /**
     * Compares two members based on their ID.
     * This is the natural identity for a gym member.
     *
     * <p><strong>Design Note:</strong> RegularMember and PremiumMember with
     * the same ID will NOT be considered equal because they are different types.
     * This is intentional - IDs should be unique across all member types,
     * so this scenario should never occur in a properly maintained system.</p>
     *
     * @param obj The object to compare with
     * @return true if both members have the same ID and same type
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GymMember that = (GymMember) obj;
        return id == that.id;
    }

    /**
     * Returns a hash code based on the member's ID.
     * ID is immutable (final field), so hash code is stable.
     * This makes instances safe for use in hash-based collections.
     *
     * @return Hash code of the member ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the member.
     * This is intended for debugging/logging only, not for display to users.
     *
     * @return A debug-friendly string
     */
    @Override
    public String toString() {
        return String.format("Member[id=%d, name=%s, type=%s, status=%s]",
                id, name, getMemberType(), getStatusSummary());
    }
}