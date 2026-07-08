package service;

import util.CurrencyFormatter;

/**
 * Result of a payment attempt containing status and remaining balance.
 */
public record PaymentResult(PaymentStatus status, long remainingPaisa) {

    /**
     * Returns a human-readable message for this result.
     */
    public String getMessage() {
        return switch (status) {
            case ACCEPTED -> "Payment accepted. Remaining: " + formatRupees(remainingPaisa);
            case COMPLETED -> "Payment complete! Full amount paid.";
            case REJECTED_INVALID_AMOUNT -> "Payment amount must be positive";
            case REJECTED_ALREADY_PAID -> "Membership is already fully paid";
            case REJECTED_EXCEEDS_TOTAL -> "Payment exceeds total cost";
        };
    }

    /**
     * Formats paisa as a currency string (matches Plan.java style).
     * Handles negative values correctly.
     */
    private static String formatRupees(long paisa) {
        return CurrencyFormatter.format(paisa);
    }
}
