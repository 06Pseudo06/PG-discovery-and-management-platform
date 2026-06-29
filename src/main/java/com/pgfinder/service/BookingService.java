package com.pgfinder.service;

import com.pgfinder.config.DBConnection;
import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.BookingDAO;
import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.model.*;
import com.pgfinder.util.NotificationMessages;
import com.pgfinder.util.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BookingService {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public Booking submitBooking(int studentId, int bedId, LocalDate startDate,
                                 LocalDate endDate, String studentNotes) {
        validateSessionStudent(studentId);
        validateDates(startDate, endDate);

        Bed bed = bedDAO.findById(bedId);
        if (bed == null) {
            throw new BookingException("The selected bed could not be found.");
        }
        if (!bed.isVacant()) {
            throw new BookingException("This bed is no longer available. Please choose another bed.");
        }
        if (bookingDAO.existsActiveBookingForBed(bedId)) {
            throw new BookingException("This bed already has a confirmed booking.");
        }
        if (bookingDAO.existsStudentBooking(studentId)) {
            throw new BookingException("You already have an active booking request or stay.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (bookingDAO.existsStudentBooking(conn, studentId)) {
                    throw new BookingException("You already have an active booking request or stay.");
                }
                if (bookingDAO.existsPendingBookingForBedAndStudent(conn, bedId, studentId)) {
                    throw new BookingException("You already have a pending request for this bed.");
                }

                Bed lockedBed = bedDAO.findById(conn, bedId);
                if (lockedBed == null || !lockedBed.isVacant()) {
                    throw new BookingException("This bed is no longer available. Please choose another bed.");
                }
                if (bookingDAO.existsActiveBookingForBed(conn, bedId)) {
                    throw new BookingException("This bed already has a confirmed booking.");
                }

                Booking booking = new Booking();
                booking.setStudentId(studentId);
                booking.setBedId(bedId);
                booking.setStatus(BookingStatus.PENDING_OWNER);
                booking.setStartDate(startDate);
                booking.setEndDate(endDate);
                booking.setStudentNotes(studentNotes);

                int bookingId = bookingDAO.createBooking(conn, booking);
                if (bookingId <= 0) {
                    throw new BookingException("Unable to create your booking request. Please try again.");
                }

                BookingDetail detail = bookingDAO.findDetailById(bookingId);
                notificationDAO.insert(conn, studentId, NotificationMessages.BOOKING_SUBMITTED_STUDENT);
                if (detail != null) {
                    notificationDAO.insert(conn, detail.getOwnerId(), NotificationMessages.BOOKING_SUBMITTED_OWNER);
                }

                conn.commit();
                return bookingDAO.findById(bookingId);
            } catch (BookingException ex) {
                conn.rollback();
                throw ex;
            } catch (SQLException ex) {
                conn.rollback();
                throw new BookingException("Unable to submit your booking request. Please try again.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Unable to submit your booking request. Please try again.");
        }
    }

    public void approveBooking(int ownerId, int bookingId, String ownerRemarks) {
        validateSessionOwner(ownerId);
        executeOwnerDecision(ownerId, bookingId, ownerRemarks, true);
    }

    public void rejectBooking(int ownerId, int bookingId, String ownerRemarks) {
        validateSessionOwner(ownerId);
        executeOwnerDecision(ownerId, bookingId, ownerRemarks, false);
    }

    public void confirmBooking(int studentId, int bookingId) {
        validateSessionStudent(studentId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.findById(conn, bookingId);
                validateStudentOwnership(studentId, booking);

                if (!BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(booking.getStatus())) {
                    throw new BookingException("This booking is not awaiting your confirmation.");
                }

                Bed bed = bedDAO.findById(conn, booking.getBedId());
                if (bed == null || !bed.isVacant()) {
                    throw new BookingException("The bed is no longer available.");
                }
                if (bookingDAO.existsActiveBookingForBed(conn, booking.getBedId())) {
                    throw new BookingException("This bed has already been confirmed for another student.");
                }

                bookingDAO.confirmBooking(conn, booking.getId());
                bedDAO.updateStatus(conn, booking.getBedId(), BedStatus.OCCUPIED);
                notifyAutoRejectedStudents(conn, booking.getBedId(), booking.getId());

                BookingDetail detail = bookingDAO.findDetailById(booking.getId());
                notificationDAO.insert(conn, studentId, NotificationMessages.BOOKING_CONFIRMED);
                if (detail != null) {
                    notificationDAO.insert(conn, detail.getOwnerId(), NotificationMessages.BOOKING_CONFIRMED_OWNER);
                }

                conn.commit();
            } catch (BookingException ex) {
                conn.rollback();
                throw ex;
            } catch (SQLException ex) {
                conn.rollback();
                throw new BookingException("Unable to confirm your booking. Please try again.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Unable to confirm your booking. Please try again.");
        }
    }

    public void cancelBooking(int studentId, int bookingId) {
        validateSessionStudent(studentId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.findById(conn, bookingId);
                validateStudentOwnership(studentId, booking);
                validateCancellable(booking);

                BookingDetail detail = bookingDAO.findDetailById(bookingId);
                bookingDAO.cancelBooking(conn, bookingId);

                if (BookingStatus.CONFIRMED.equalsIgnoreCase(booking.getStatus())) {
                    bedDAO.updateStatus(conn, booking.getBedId(), BedStatus.VACANT);
                }

                notificationDAO.insert(conn, studentId, NotificationMessages.BOOKING_CANCELLED);
                if (detail != null) {
                    notificationDAO.insert(conn, detail.getOwnerId(), NotificationMessages.BOOKING_CANCELLED);
                }

                conn.commit();
            } catch (BookingException ex) {
                conn.rollback();
                throw ex;
            } catch (SQLException ex) {
                conn.rollback();
                throw new BookingException("Unable to cancel the booking. Please try again.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Unable to cancel the booking. Please try again.");
        }
    }

    public List<BookingDetail> getPendingRequestsForOwner(int ownerId) {
        validateSessionOwner(ownerId);
        return bookingDAO.findPendingRequests(ownerId);
    }

    public List<BookingDetail> getBookingsForOwner(int ownerId) {
        validateSessionOwner(ownerId);
        return bookingDAO.findByOwner(ownerId);
    }

    public List<BookingDetail> getStudentBookingHistory(int studentId) {
        validateSessionStudent(studentId);
        return bookingDAO.findDetailsByStudent(studentId);
    }

    public List<BookingDetail> getAwaitingConfirmationForStudent(int studentId) {
        validateSessionStudent(studentId);
        return bookingDAO.findAwaitingConfirmationForStudent(studentId);
    }

    public BookingDetail getActiveStayForStudent(int studentId) {
        validateSessionStudent(studentId);
        return bookingDAO.findDetailsByStudent(studentId).stream()
                .filter(d -> BookingStatus.CONFIRMED.equalsIgnoreCase(d.getStatus()))
                .findFirst()
                .orElse(null);
    }

    public BookingDetail getBookingDetail(int bookingId) {
        return bookingDAO.findDetailById(bookingId);
    }

    public int countPendingRequests(int ownerId) {
        return bookingDAO.countPendingForOwner(ownerId);
    }

    private void executeOwnerDecision(int ownerId, int bookingId, String ownerRemarks, boolean approve) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Booking booking = bookingDAO.findById(conn, bookingId);
                validateOwnerBookingAccess(ownerId, booking);

                if (!BookingStatus.PENDING_OWNER.equalsIgnoreCase(booking.getStatus())) {
                    throw new BookingException("This booking request has already been processed.");
                }

                if (approve) {
                    approveInTransaction(conn, booking, ownerRemarks);
                } else {
                    rejectInTransaction(conn, booking, ownerRemarks);
                }

                conn.commit();
            } catch (BookingException ex) {
                conn.rollback();
                throw ex;
            } catch (SQLException ex) {
                conn.rollback();
                throw new BookingException("Unable to process the booking request. Please try again.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Unable to process the booking request. Please try again.");
        }
    }

    private void approveInTransaction(Connection conn, Booking booking, String ownerRemarks)
            throws SQLException {
        Bed bed = bedDAO.findById(conn, booking.getBedId());
        if (bed == null || !bed.isVacant()) {
            throw new BookingException("The bed is no longer vacant and cannot be approved.");
        }
        if (bookingDAO.existsActiveBookingForBed(conn, booking.getBedId())) {
            throw new BookingException("This bed already has a confirmed booking.");
        }

        bookingDAO.approveBooking(conn, booking.getId(), ownerRemarks);
        notifyAutoRejectedStudents(conn, booking.getBedId(), booking.getId());
        notificationDAO.insert(conn, booking.getStudentId(), NotificationMessages.BOOKING_APPROVED);
    }

    private void notifyAutoRejectedStudents(Connection conn, int bedId, int approvedBookingId)
            throws SQLException {
        for (BookingDetail pending : bookingDAO.findPendingByBedExcept(conn, bedId, approvedBookingId)) {
            notificationDAO.insert(conn, pending.getStudentId(), NotificationMessages.BOOKING_AUTO_REJECTED);
        }
        bookingDAO.rejectPendingForBedExcept(conn, bedId, approvedBookingId);
    }

    private void rejectInTransaction(Connection conn, Booking booking, String ownerRemarks)
            throws SQLException {
        bookingDAO.rejectBooking(conn, booking.getId(), ownerRemarks);
        notificationDAO.insert(conn, booking.getStudentId(), NotificationMessages.BOOKING_REJECTED);
    }

    private void validateOwnerBookingAccess(int ownerId, Booking booking) {
        if (booking == null) {
            throw new BookingException("Booking request not found.");
        }
        BookingDetail detail = bookingDAO.findDetailById(booking.getId());
        if (detail == null || detail.getOwnerId() != ownerId) {
            throw new BookingException("You are not authorized to manage this booking request.");
        }
    }

    private void validateStudentOwnership(int studentId, Booking booking) {
        if (booking == null) {
            throw new BookingException("Booking not found.");
        }
        if (booking.getStudentId() != studentId) {
            throw new BookingException("You are not authorized to manage this booking.");
        }
    }

    private void validateCancellable(Booking booking) {
        String status = booking.getStatus();
        if (!BookingStatus.PENDING_OWNER.equalsIgnoreCase(status)
                && !BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(status)
                && !BookingStatus.CONFIRMED.equalsIgnoreCase(status)) {
            throw new BookingException("This booking cannot be cancelled.");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BookingException("Please select a move-in date.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BookingException("Move-in date cannot be in the past.");
        }
        if (endDate != null && !endDate.isAfter(startDate)) {
            throw new BookingException("End date must be after the move-in date.");
        }
    }

    private void validateSessionStudent(int studentId) {
        if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
            throw new BookingException("Please log in to continue.");
        }
        if (SessionManager.getCurrentUser().getId() != studentId) {
            throw new BookingException("Invalid session. Please log in again.");
        }
    }

    private void validateSessionOwner(int ownerId) {
        if (!SessionManager.isLoggedIn() || SessionManager.getCurrentUser() == null) {
            throw new BookingException("Please log in to continue.");
        }
        if (SessionManager.getCurrentUser().getId() != ownerId) {
            throw new BookingException("Invalid session. Please log in again.");
        }
    }
}
