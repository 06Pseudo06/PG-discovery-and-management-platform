package com.pgfinder.model;

public final class BookingStatus {

    public static final String PENDING_OWNER = "pending_owner";
    public static final String AWAITING_STUDENT_CONFIRMATION = "awaiting_student_confirmation";
    public static final String CONFIRMED = "confirmed";
    public static final String REJECTED = "rejected";
    public static final String CANCELLED = "cancelled";

    private BookingStatus() {
    }

    public static boolean isActive(String status) {
        return PENDING_OWNER.equalsIgnoreCase(status)
                || AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(status)
                || CONFIRMED.equalsIgnoreCase(status);
    }

    public static boolean isOccupying(String status) {
        return CONFIRMED.equalsIgnoreCase(status);
    }

    public static String normalize(String status) {
        if (status == null) {
            return null;
        }
        return switch (status.toLowerCase()) {
            case "pending" -> PENDING_OWNER;
            case "approved" -> CONFIRMED;
            default -> status.toLowerCase();
        };
    }
}
