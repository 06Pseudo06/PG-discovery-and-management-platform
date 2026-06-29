package com.pgfinder.util;

import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.BookingDAO;
import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.model.BookingStatus;

/**
 * Shared refresh helpers to avoid duplicating booking-related statistics queries across controllers.
 */
public final class BookingRefreshHelper {

    private static final BookingDAO bookingDAO = new BookingDAO();
    private static final BedDAO bedDAO = new BedDAO();
    private static final NotificationDAO notificationDAO = new NotificationDAO();

    private BookingRefreshHelper() {
    }

    public static int getPendingRequestCount(int ownerId) {
        return bookingDAO.countPendingForOwner(ownerId);
    }

    public static int getVacantBedCountForPg(int pgId) {
        return bedDAO.countVacantBedsByPgId(pgId);
    }

    public static int getUnreadNotificationCount(int userId) {
        return notificationDAO.countUnread(userId);
    }

    public static int getApprovedBookingCount(int ownerId) {
        return bookingDAO.countBookings(ownerId, BookingStatus.CONFIRMED);
    }
}
