package service;

import model.GymMember;
import model.Plan;
import model.PremiumMember;
import model.RegularMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service layer containing gym membership business rules.
 */
public class MemberService {

    private static final int LOYALTY_POINTS_PER_VISIT = RegularMember.LOYALTY_POINTS_PER_VISIT;
    private static final int ATTENDANCE_LIMIT_FOR_UPGRADE = RegularMember.ATTENDANCE_LIMIT_FOR_UPGRADE;
    private static final long PREMIUM_CHARGE_PAISA = PremiumMember.PREMIUM_CHARGE_PAISA;
    private static final int DISCOUNT_PERCENTAGE = PremiumMember.DISCOUNT_PERCENTAGE;

    private final List<GymMember> members = new ArrayList<>();

    /**
     * Adds a member to the system.
     * CRITICAL: Checks ID uniqueness across ALL member types.
     *
     * @throws IllegalArgumentException if member is null or ID already exists
     */
    public void addMember(GymMember member) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }

        int id = member.getId();
        boolean idExists = members.stream().anyMatch(m -> m.getId() == id);
        if (idExists) {
            throw new IllegalArgumentException("Member ID already exists: " + id);
        }

        members.add(member);
    }

    /**
     * Finds a member by ID.
     * @return Optional containing the member, or empty if not found
     */
    public Optional<GymMember> findMemberById(int id) {
        for (GymMember member : members) {
            if (member.getId() == id) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns an unmodifiable list of all members (including removed).
     */
    public List<GymMember> getAllMembers() {
        return Collections.unmodifiableList(members);
    }

    /**
     * Returns only active, non-removed members.
     * Uses member.isEligible() from the Model.
     */
    public List<GymMember> getActiveMembers() {
        List<GymMember> activeMembers = new ArrayList<>();
        for (GymMember member : members) {
            if (member.isEligible()) {
                activeMembers.add(member);
            }
        }
        return Collections.unmodifiableList(activeMembers);
    }

    /**
     * Returns only removed members.
     */
    public List<GymMember> getRemovedMembers() {
        List<GymMember> removedMembers = new ArrayList<>();
        for (GymMember member : members) {
            if (member.isRemoved()) {
                removedMembers.add(member);
            }
        }
        return Collections.unmodifiableList(removedMembers);
    }

    /**
     * Marks attendance for a member.
     *
     * @throws IllegalArgumentException if member not found, removed, or inactive
     */
    public void markAttendance(int id) {
        GymMember member = getMemberOrThrow(id);

        if (member.isRemoved()) {
            throw new IllegalArgumentException("Member is removed");
        }
        if (!member.isActive()) {
            throw new IllegalArgumentException("Member is inactive");
        }

        member.setAttendance(member.getAttendance() + 1);

        if (member instanceof RegularMember rm) {
            rm.setLoyaltyPoints(rm.getLoyaltyPoints() + LOYALTY_POINTS_PER_VISIT);
            int newVisits = rm.getVisitsSinceLastUpgrade() + 1;
            rm.setVisitsSinceLastUpgrade(newVisits);
            if (newVisits >= ATTENDANCE_LIMIT_FOR_UPGRADE) {
                rm.setEligibleForUpgrade(true);
            }
        }

        if (member instanceof PremiumMember pm) {
            pm.setLoyaltyPoints(pm.getLoyaltyPoints() + 10);
        }
    }

    /**
     * Upgrades a Regular member's plan.
     *
     * <p><strong>Check Order Intentional:</strong> Payment status is checked
     * before eligibility (visits). This means a member who is both unpaid and
     * ineligible will see "Must be fully paid" before "Need X more visits."
     * This is intentional - payment is a prerequisite for upgrade.</p>
     *
     * @return UpgradeResult with success/failure and message
     * @throws IllegalArgumentException if member not found, not Regular, removed, or inactive
     */
    public UpgradeResult upgradePlan(int id, Plan newPlan) {
        GymMember member = getMemberOrThrow(id);
        if (!(member instanceof RegularMember rm)) {
            throw new IllegalArgumentException("Not a regular member");
        }
        if (rm.isRemoved()) {
            throw new IllegalArgumentException("Member is removed");
        }
        if (!rm.isActive()) {
            throw new IllegalArgumentException("Member is inactive");
        }
        if (!rm.isFullyPaid()) {
            return UpgradeResult.failure("Must be fully paid on current plan to upgrade", 0);
        }
        if (newPlan == null) {
            throw new IllegalArgumentException("Plan cannot be null");
        }
        if (!newPlan.isUpgradeFrom(rm.getCurrentPlan())) {
            return UpgradeResult.failure(
                    "Cannot downgrade. Current plan: " + rm.getCurrentPlan().getDisplayName(),
                    0
            );
        }
        if (!rm.isEligibleForUpgrade()) {
            int needed = ATTENDANCE_LIMIT_FOR_UPGRADE - rm.getVisitsSinceLastUpgrade();
            return UpgradeResult.failure("Not eligible. Need " + needed + " more visits", needed);
        }

        rm.setCurrentPlan(newPlan);
        rm.setEligibleForUpgrade(false);
        rm.setVisitsSinceLastUpgrade(0);
        return UpgradeResult.success("Upgraded to " + newPlan.getDisplayName());
    }

    /**
     * Processes a payment for a member.
     *
     * <p><strong>Note:</strong> Payment is allowed even if the member is not
     * yet active, since payment typically precedes activation. This is intentional
     * asymmetry from markAttendance() which requires active status.</p>
     *
     * @return PaymentResult with status and remaining balance
     * @throws IllegalArgumentException if member not found or removed
     */
    public PaymentResult payDueAmount(int id, long amountPaisa) {
        GymMember member = getMemberOrThrow(id);
        if (member.isRemoved()) {
            throw new IllegalArgumentException("Member is removed");
        }

        long totalCostPaisa;
        if (member instanceof RegularMember rm) {
            totalCostPaisa = rm.getCurrentPlan().getPriceInPaisa();
        } else if (member instanceof PremiumMember) {
            totalCostPaisa = PREMIUM_CHARGE_PAISA;
        } else {
            throw new IllegalArgumentException("Unsupported member type");
        }

        if (amountPaisa <= 0) {
            return new PaymentResult(
                    PaymentStatus.REJECTED_INVALID_AMOUNT,
                    totalCostPaisa - member.getPaidAmountPaisa()
            );
        }
        if (member.isFullyPaid()) {
            return new PaymentResult(PaymentStatus.REJECTED_ALREADY_PAID, 0);
        }
        if (member.getPaidAmountPaisa() + amountPaisa > totalCostPaisa) {
            return new PaymentResult(
                    PaymentStatus.REJECTED_EXCEEDS_TOTAL,
                    totalCostPaisa - member.getPaidAmountPaisa()
            );
        }

        member.setPaidAmountPaisa(member.getPaidAmountPaisa() + amountPaisa);
        long remaining = totalCostPaisa - member.getPaidAmountPaisa();
        if (remaining == 0) {
            member.setFullyPaid(true);
            if (member instanceof PremiumMember pm) {
                calculateDiscount(pm.getId());
            }
            return new PaymentResult(PaymentStatus.COMPLETED, 0);
        }

        return new PaymentResult(PaymentStatus.ACCEPTED, remaining);
    }

    /**
     * Calculates and sets the discount for a Premium member.
     *
     * @throws IllegalArgumentException if member not found, not Premium, or not fully paid
     */
    public void calculateDiscount(int id) {
        GymMember member = getMemberOrThrow(id);
        if (!(member instanceof PremiumMember pm)) {
            throw new IllegalArgumentException("Not a premium member");
        }
        if (!pm.isFullyPaid()) {
            throw new IllegalArgumentException("Discount only available for fully paid members");
        }
        long discount = (PREMIUM_CHARGE_PAISA * DISCOUNT_PERCENTAGE) / 100;
        pm.setDiscountAmountPaisa(discount);
    }

    /**
     * Activates a member's membership.
     * @throws IllegalArgumentException if member not found or removed
     */
    public void activateMember(int id) {
        GymMember member = getMemberOrThrow(id);
        if (member.isRemoved()) {
            throw new IllegalArgumentException("Cannot activate removed member");
        }
        member.activateMembership();
    }

    /**
     * Deactivates a member's membership.
     *
     * <p><strong>Note:</strong> This method does NOT check if the member is
     * already removed. Deactivating a removed member is a harmless no-op
     * (they are already forced inactive by softDelete()). This is intentional
     * to avoid throwing an exception for an idempotent operation.</p>
     *
     * @throws IllegalArgumentException if member not found
     */
    public void deactivateMember(int id) {
        GymMember member = getMemberOrThrow(id);
        member.deactivateMembership();
    }

    /**
     * Soft-deletes a member.
     * @throws IllegalArgumentException if member not found, already removed, or reason empty
     */
    public void softDeleteMember(int id, String reason) {
        GymMember member = getMemberOrThrow(id);
        if (member.isRemoved()) {
            throw new IllegalStateException("Member is already removed");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Reason cannot be empty");
        }
        member.softDelete(reason);
    }

    /**
     * Restores a soft-deleted member.
     * IMPORTANT: Member stays inactive after restore - caller must explicitly activate.
     *
     * @throws IllegalArgumentException if member not found or not removed
     */
    public void restoreMember(int id) {
        GymMember member = getMemberOrThrow(id);
        if (!member.isRemoved()) {
            throw new IllegalArgumentException("Member is not removed");
        }
        member.restore();
    }

    /**
     * Updates editable contact/identity fields on an existing member.
     * Does NOT touch plan, payment, attendance, or status fields —
     * those must go through their dedicated Service methods.
     *
     * <p><strong>Fields updated:</strong> name, location, phone, email, gender,
     * referralSource (Regular only), personalTrainer (Premium only).</p>
     *
     * <p><strong>Fields NOT updated:</strong> id (immutable), dateOfBirth,
     * membershipStartDate, currentPlan, paidAmountPaisa, fullyPaid,
     * attendance, loyaltyPoints, activeStatus, isRemoved.</p>
     *
     * @param id Member ID to update
     * @param name New name (cannot be null or empty)
     * @param location New location (cannot be null or empty)
     * @param phone New phone (must match phone regex)
     * @param email New email (must match email regex)
     * @param gender New gender (must be Male/Female/Other/Prefer not to say)
     * @param referralSourceOrNull New referral source (Regular only, pass null for Premium or to keep unchanged on Regular)
     * @param personalTrainerOrNull New personal trainer (Premium only, pass null for Regular or to keep unchanged on Premium)
     * @throws IllegalArgumentException if member not found, or if
     *         referralSource is passed for a Premium member (or vice versa)
     */
    public void updateMemberDetails(int id, String name, String location, String phone,
                                    String email, String gender,
                                    String referralSourceOrNull, String personalTrainerOrNull) {
        GymMember member = findMemberById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));

        // Update common fields (validation happens inside setters)
        member.setName(name);
        member.setLocation(location);
        member.setPhone(phone);
        member.setEmail(email);
        member.setGender(gender);

        // Update type-specific fields
        if (member instanceof RegularMember rm) {
            // Regular members cannot have a personal trainer
            if (personalTrainerOrNull != null && !personalTrainerOrNull.trim().isEmpty()) {
                throw new IllegalArgumentException("Personal trainer is only for Premium members");
            }
            // Only update referral source if provided (null means "keep as is")
            if (referralSourceOrNull != null) {
                rm.setReferralSource(referralSourceOrNull);
            }
        } else if (member instanceof PremiumMember pm) {
            // Premium members cannot have a referral source
            if (referralSourceOrNull != null && !referralSourceOrNull.trim().isEmpty()) {
                throw new IllegalArgumentException("Referral source is only for Regular members");
            }
            // Only update personal trainer if provided (null means "keep as is")
            if (personalTrainerOrNull != null) {
                pm.setPersonalTrainer(personalTrainerOrNull);
            }
        } else {
            // Should never happen (GymMember is abstract, only Regular/Premium exist)
            throw new IllegalArgumentException("Unsupported member type: " + member.getClass().getSimpleName());
        }
    }

    private GymMember getMemberOrThrow(int id) {
        return findMemberById(id).orElseThrow(() ->
                new IllegalArgumentException("Member not found: " + id));
    }
    /**
     * Clears all members from the service.
     * Used before loading data from file to avoid duplicates.
     */
    public void clearAllMembers() {
        members.clear();
    }
}
