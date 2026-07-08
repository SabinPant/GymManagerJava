package view;

/**
 * Immutable data-transfer object carrying raw form input from
 * MemberFormPanel to GymController. No validation logic here —
 * validation happens in Model (via Service) when the Controller
 * attempts to construct or update a member.
 * Fields not applicable to the selected member type are null:
 * referralSource is null when isPremium == true; personalTrainer is
 * null when isPremium == false; planName is null when isPremium == true.
 */
public record MemberFormData(
    Integer id,
    String name,
    String location,
    String phone,
    String email,
    String gender,
    String dateOfBirth,
    String membershipStartDate,
    boolean isPremium,
    String referralSource,
    String personalTrainer,
    String planName,
    long initialPaidAmountPaisa
) {}
