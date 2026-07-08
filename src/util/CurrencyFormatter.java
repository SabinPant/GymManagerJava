package util;

import java.util.Locale;

/**
 * Canonical paisa-to-rupee display formatter, shared by the View layer.
 * Mirrors the exact formatting logic used in service.PaymentResult so that
 * table display and payment-result messages never drift apart.
 */
public final class CurrencyFormatter {

    private CurrencyFormatter() {}

    /**
     * Formats a paisa amount as a currency string, e.g. "Rs.6500.00".
     * Handles negative values with a leading minus sign before "Rs."
     */
    public static String format(long paisa) {
        boolean isNegative = paisa < 0;
        long absolutePaisa = Math.abs(paisa);
        long rupees = absolutePaisa / 100;
        long paisaRemainder = absolutePaisa % 100;
        String formatted = String.format(Locale.US, "%d.%02d", rupees, paisaRemainder);
        return (isNegative ? "-Rs." : "Rs.") + formatted;
    }
}
