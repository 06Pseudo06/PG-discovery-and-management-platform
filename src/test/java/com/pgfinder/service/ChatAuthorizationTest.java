package com.pgfinder.service;

import com.pgfinder.config.SchemaMigrator;
import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.BookingDAO;
import com.pgfinder.model.BedStatus;
import com.pgfinder.model.Booking;
import com.pgfinder.model.BookingStatus;
import com.pgfinder.model.User;
import com.pgfinder.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ChatAuthorizationTest {

    private static final int TEST_BED_ID = 2; // Vacant bed

    private final BookingService bookingService = new BookingService();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ChatService chatService = new ChatService();
    private final BedDAO bedDAO = new BedDAO();
    private final AuthService authService = new AuthService();

    private User owner;
    private User student;
    private int createdBookingId = -1;

    @BeforeAll
    static void migrate() throws Exception {
        SchemaMigrator.runMigration();
    }

    @BeforeEach
    void setUp() throws Exception {
        owner = authService.login("owner1@pgfinder.com", "password123");
        student = authService.login("student2@pgfinder.com", "password123");
        SessionManager.setCurrentUser(student);
        cleanupStudentBookings(student.getId());
        cleanupMessages(student.getId(), owner.getId());
        resetBed(TEST_BED_ID, BedStatus.VACANT);
        createdBookingId = -1;
    }

    @AfterEach
    void tearDown() {
        if (student != null) {
            cleanupStudentBookings(student.getId());
            if (owner != null) {
                cleanupMessages(student.getId(), owner.getId());
            }
        }
        resetBed(TEST_BED_ID, BedStatus.VACANT);
    }

    @Test
    void testChatAuthorizationScenarios() {
        // Scenario 1: No booking
        assertFalse(chatService.canChat(student.getId(), owner.getId()));
        assertFalse(chatService.canChat(owner.getId(), student.getId()));
        assertThrows(BookingException.class, () -> chatService.getMessages(student.getId(), owner.getId()));
        assertThrows(BookingException.class, () -> chatService.getMessages(owner.getId(), student.getId()));

        // Scenario 2: Pending booking
        SessionManager.setCurrentUser(student);
        Booking booking = bookingService.submitBooking(
                student.getId(), TEST_BED_ID, LocalDate.now().plusDays(3), null, "Need room");
        createdBookingId = booking.getId();
        assertEquals(BookingStatus.PENDING_OWNER, booking.getStatus());

        assertFalse(chatService.canChat(student.getId(), owner.getId()));
        assertFalse(chatService.canChat(owner.getId(), student.getId()));

        // Scenario 3: Awaiting student confirmation
        SessionManager.setCurrentUser(owner);
        bookingService.approveBooking(owner.getId(), createdBookingId, "Approved");
        Booking awaiting = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.AWAITING_STUDENT_CONFIRMATION, awaiting.getStatus());

        assertFalse(chatService.canChat(student.getId(), owner.getId()));
        assertFalse(chatService.canChat(owner.getId(), student.getId()));

        // Scenario 4: Confirmed booking
        SessionManager.setCurrentUser(student);
        bookingService.confirmBooking(student.getId(), createdBookingId);
        Booking confirmed = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());

        assertTrue(chatService.canChat(student.getId(), owner.getId()));
        assertTrue(chatService.canChat(owner.getId(), student.getId()));
        assertDoesNotThrow(() -> chatService.getMessages(student.getId(), owner.getId()));
        assertDoesNotThrow(() -> chatService.getMessages(owner.getId(), student.getId()));

        // Scenario 5: Cancelled booking
        bookingService.cancelBooking(student.getId(), createdBookingId);
        Booking cancelled = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());

        assertFalse(chatService.canChat(student.getId(), owner.getId()));
        assertFalse(chatService.canChat(owner.getId(), student.getId()));
    }

    @Test
    void testRejectedBookingScenario() {
        // Scenario 6: Rejected booking
        SessionManager.setCurrentUser(student);
        Booking booking = bookingService.submitBooking(
                student.getId(), TEST_BED_ID, LocalDate.now().plusDays(3), null, "Need room");
        createdBookingId = booking.getId();

        SessionManager.setCurrentUser(owner);
        bookingService.rejectBooking(owner.getId(), createdBookingId, "Rejected");
        Booking rejected = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.REJECTED, rejected.getStatus());

        assertFalse(chatService.canChat(student.getId(), owner.getId()));
        assertFalse(chatService.canChat(owner.getId(), student.getId()));
    }

    private void cleanupStudentBookings(int studentId) {
        try {
            java.sql.Connection conn = com.pgfinder.config.DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM booking_requests WHERE student_id = ?");
            ps.setInt(1, studentId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private void cleanupMessages(int studentId, int ownerId) {
        try (java.sql.Connection conn = com.pgfinder.config.DBConnection.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM messages WHERE (sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)")) {
            ps.setInt(1, studentId);
            ps.setInt(2, ownerId);
            ps.setInt(3, ownerId);
            ps.setInt(4, studentId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }

    private void resetBed(int bedId, String status) {
        try {
            java.sql.Connection conn = com.pgfinder.config.DBConnection.getConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE beds SET status = ? WHERE id = ?");
            ps.setString(1, status);
            ps.setInt(2, bedId);
            ps.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
