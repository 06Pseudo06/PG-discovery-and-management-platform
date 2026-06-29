package com.pgfinder.util;

public final class NotificationMessages {

    public static final String BOOKING_SUBMITTED_STUDENT =
            "Your booking request has been submitted and is awaiting owner approval.";
    public static final String BOOKING_SUBMITTED_OWNER =
            "A new booking request has been received for your property.";
    public static final String BOOKING_APPROVED =
            "Your booking request was approved. Please confirm to finalize your stay.";
    public static final String BOOKING_REJECTED =
            "Your booking request has been rejected by the property owner.";
    public static final String BOOKING_CONFIRMED =
            "Your booking has been confirmed. Welcome to your new stay!";
    public static final String BOOKING_CONFIRMED_OWNER =
            "A student has confirmed their booking. The bed is now occupied.";
    public static final String BOOKING_CANCELLED =
            "A booking has been cancelled.";
    public static final String BOOKING_AUTO_REJECTED =
            "Your booking request was automatically rejected because another request was approved for the same bed.";
    public static final String ANNOUNCEMENT_POSTED =
            "A new announcement has been posted for your PG.";
    public static final String CHAT_MESSAGE =
            "You have a new chat message.";

    private NotificationMessages() {
    }
}
