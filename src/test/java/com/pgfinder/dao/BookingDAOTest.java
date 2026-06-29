package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.config.SchemaMigrator;
import com.pgfinder.model.BedStatus;
import com.pgfinder.model.Booking;
import com.pgfinder.model.BookingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BookingDAOTest {

    private static final int STUDENT_ID = 3;
    private static final int SECOND_STUDENT_ID = 4;
    private static final int VACANT_BED_ID = 2;

    private BookingDAO bookingDAO;
    private BedDAO bedDAO;

    @BeforeAll
    static void migrateSchema() throws SQLException {
        SchemaMigrator.runMigration();
    }

    @BeforeEach
    void setUp() {
        bookingDAO = new BookingDAO();
        bedDAO = new BedDAO();
        cleanupTestBookings();
        resetBed(VACANT_BED_ID, BedStatus.VACANT);
    }

    @AfterEach
    void tearDown() {
        cleanupTestBookings();
        resetBed(VACANT_BED_ID, BedStatus.VACANT);
    }

    @Test
    void testCreateBooking() {
        Booking booking = buildPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int id = bookingDAO.createBooking(conn, booking);
            conn.commit();
            assertTrue(id > 0);
        } catch (SQLException e) {
            fail("Should create booking without SQLException: " + e.getMessage());
        }

        Booking saved = bookingDAO.findById(booking.getId());
        assertNotNull(saved);
        assertEquals(BookingStatus.PENDING_OWNER, saved.getStatus());
        assertEquals(STUDENT_ID, saved.getStudentId());
        assertEquals(VACANT_BED_ID, saved.getBedId());
    }

    @Test
    void testApproveBooking() throws SQLException {
        int bookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            assertTrue(bookingDAO.approveBooking(conn, bookingId, "Welcome"));
            conn.commit();
        }

        Booking approved = bookingDAO.findById(bookingId);
        assertEquals(BookingStatus.AWAITING_STUDENT_CONFIRMATION, approved.getStatus());
        assertEquals("Welcome", approved.getOwnerRemarks());
        assertNotNull(approved.getDecidedAt());
        assertTrue(bedDAO.findById(VACANT_BED_ID).isVacant());
    }

    @Test
    void testConfirmBooking() throws SQLException {
        int bookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            bookingDAO.approveBooking(conn, bookingId, null);
            bookingDAO.confirmBooking(conn, bookingId);
            bedDAO.updateStatus(conn, VACANT_BED_ID, BedStatus.OCCUPIED);
            conn.commit();
        }

        Booking confirmed = bookingDAO.findById(bookingId);
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertTrue(bedDAO.findById(VACANT_BED_ID).isOccupied());
    }

    @Test
    void testRejectBooking() throws SQLException {
        int bookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            assertTrue(bookingDAO.rejectBooking(conn, bookingId, "Not available"));
            conn.commit();
        }

        Booking rejected = bookingDAO.findById(bookingId);
        assertEquals(BookingStatus.REJECTED, rejected.getStatus());
        assertTrue(bedDAO.findById(VACANT_BED_ID).isVacant());
    }

    @Test
    void testCancelBooking() throws SQLException {
        int bookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            assertTrue(bookingDAO.cancelBooking(conn, bookingId));
            conn.commit();
        }

        Booking cancelled = bookingDAO.findById(bookingId);
        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testDoubleBookingPrevention() throws SQLException {
        int firstBookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            bookingDAO.approveBooking(conn, firstBookingId, null);
            bookingDAO.confirmBooking(conn, firstBookingId);
            bedDAO.updateStatus(conn, VACANT_BED_ID, BedStatus.OCCUPIED);
            conn.commit();
        }

        assertTrue(bookingDAO.existsActiveBookingForBed(VACANT_BED_ID));
        assertTrue(bookingDAO.existsStudentBooking(STUDENT_ID));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            if (bookingDAO.existsActiveBookingForBed(conn, VACANT_BED_ID)) {
                conn.rollback();
                assertTrue(true, "Second booking correctly blocked for occupied bed");
                return;
            }
            bookingDAO.createBooking(conn, buildPendingBooking(SECOND_STUDENT_ID, VACANT_BED_ID));
            conn.commit();
            fail("Second booking should have been prevented");
        }
    }

    @Test
    void testTransactionRollback() throws SQLException {
        int bookingId = insertPendingBooking(STUDENT_ID, VACANT_BED_ID);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            bookingDAO.confirmBooking(conn, bookingId);
            bedDAO.updateStatus(conn, VACANT_BED_ID, BedStatus.OCCUPIED);
            conn.rollback();
        }

        Booking booking = bookingDAO.findById(bookingId);
        assertEquals(BookingStatus.PENDING_OWNER, booking.getStatus());
        assertTrue(bedDAO.findById(VACANT_BED_ID).isVacant());
    }

    @Test
    void testRejectPendingForBedExcept() throws SQLException {
        int bedId = 4;
        resetBed(bedId, BedStatus.VACANT);
        int approvedId = insertPendingBooking(STUDENT_ID, bedId);
        int pendingId = insertPendingBooking(SECOND_STUDENT_ID, bedId);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            bookingDAO.approveBooking(conn, approvedId, null);
            int rejectedCount = bookingDAO.rejectPendingForBedExcept(conn, bedId, approvedId);
            conn.commit();
            assertEquals(1, rejectedCount);
        }

        assertEquals(BookingStatus.AWAITING_STUDENT_CONFIRMATION, bookingDAO.findById(approvedId).getStatus());
        assertEquals(BookingStatus.REJECTED, bookingDAO.findById(pendingId).getStatus());
        resetBed(bedId, BedStatus.VACANT);
    }

    private Booking buildPendingBooking(int studentId, int bedId) {
        Booking booking = new Booking();
        booking.setStudentId(studentId);
        booking.setBedId(bedId);
        booking.setStatus(BookingStatus.PENDING_OWNER);
        booking.setStartDate(LocalDate.now().plusDays(7));
        return booking;
    }

    private int insertPendingBooking(int studentId, int bedId) throws SQLException {
        Booking booking = buildPendingBooking(studentId, bedId);
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            int id = bookingDAO.createBooking(conn, booking);
            conn.commit();
            return id;
        }
    }

    private void cleanupTestBookings() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM booking_requests WHERE student_id IN (?, ?)")) {
            ps.setInt(1, STUDENT_ID);
            ps.setInt(2, SECOND_STUDENT_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Cleanup failed: " + e.getMessage());
        }
    }

    private void resetBed(int bedId, String status) {
        bedDAO.updateStatus(bedId, status);
    }
}
