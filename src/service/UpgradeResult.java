package service;

/**
 * Result of a plan upgrade attempt.
 * Contains success status, message, and visits needed if not eligible.
 */
public record UpgradeResult(boolean success, String message, int visitsNeeded) {

    /**
     * Creates a success result.
     */
    public static UpgradeResult success(String message) {
        return new UpgradeResult(true, message, 0);
    }

    /**
     * Creates a failure result with visits needed.
     */
    public static UpgradeResult failure(String message, int visitsNeeded) {
        return new UpgradeResult(false, message, visitsNeeded);
    }
}
