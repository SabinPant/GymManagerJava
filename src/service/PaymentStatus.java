package service;

/**
 * Represents the status of a payment attempt.
 */
public enum PaymentStatus {
    /** Payment was accepted, but remaining balance exists */
    ACCEPTED,
    /** Payment completed the full amount */
    COMPLETED,
    /** Payment amount was zero or negative */
    REJECTED_INVALID_AMOUNT,
    /** Member is already fully paid */
    REJECTED_ALREADY_PAID,
    /** Payment would exceed the total cost */
    REJECTED_EXCEEDS_TOTAL
}
