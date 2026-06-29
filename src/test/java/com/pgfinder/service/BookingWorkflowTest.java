package com.pgfinder.service;

import com.pgfinder.config.SchemaMigrator;
import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.BookingDAO;
import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.model.Bed;
import com.pgfinder.model.BedStatus;
import com.pgfinder.model.Booking;
import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.BookingStatus;
import com.pgfinder.model.PG;
import com.pgfinder.model.User;
import com.pgfinder.util.SessionManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingWorkflowTest {

    private static final int VACANT_BED_ID = 2;

    private final BookingService bookingService = new BookingService();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final ChatService chatService = new ChatService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final BedDAO bedDAO = new BedDAO();
    private final PGService pgService = new PGService();
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
        resetBed(VACANT_BED_ID, BedStatus.VACANT);
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
        resetBed(VACANT_BED_ID, BedStatus.VACANT);
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

    @Test
    void fullBookingLifecycleWithChatAndAnnouncements() {
        SessionManager.setCurrentUser(student);
        assertFalse(chatService.canChat(student.getId(), owner.getId()));

        Booking booking = bookingService.submitBooking(
                student.getId(), VACANT_BED_ID, LocalDate.now().plusDays(3), null, "Need quiet room");
        createdBookingId = booking.getId();
        assertEquals(BookingStatus.PENDING_OWNER, booking.getStatus());

        SessionManager.setCurrentUser(owner);
        bookingService.approveBooking(owner.getId(), createdBookingId, "Welcome");
        Booking awaiting = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.AWAITING_STUDENT_CONFIRMATION, awaiting.getStatus());
        assertFalse(chatService.canChat(student.getId(), owner.getId()));

        SessionManager.setCurrentUser(student);
        bookingService.confirmBooking(student.getId(), createdBookingId);
        Booking confirmed = bookingDAO.findById(createdBookingId);
        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());

        Bed bed = bedDAO.findById(VACANT_BED_ID);
        assertNotNull(bed);
        assertEquals(BedStatus.OCCUPIED, bed.getStatus().toLowerCase());

        BookingDetail activeStay = bookingService.getActiveStayForStudent(student.getId());
        assertNotNull(activeStay);
        assertEquals(BookingStatus.CONFIRMED, activeStay.getStatus());

        assertTrue(chatService.canChat(student.getId(), owner.getId()));
        chatService.sendMessage(student.getId(), owner.getId(), "Hello owner");
        assertEquals(1, chatService.getMessages(student.getId(), owner.getId()).size());

        List<PG> ownerPgs = pgService.getOwnerPGs(owner.getId());
        PG pg = ownerPgs.stream().filter(p -> p.getId() == 1).findFirst().orElse(ownerPgs.get(0));
        announcementService.postAnnouncement(owner.getId(), pg.getId(), "Water outage", "Tank cleaning tomorrow");

        List<com.pgfinder.model.Announcement> studentFeed =
                announcementService.getStudentAnnouncements(student.getId());
        assertTrue(studentFeed.stream().anyMatch(a -> "Water outage".equals(a.getTitle())));

        assertFalse(notificationDAO.findByUserId(student.getId()).isEmpty());
    }

    private void cleanupStudentBookings(int studentId) {
        List<BookingDetail> history = bookingService.getStudentBookingHistory(studentId);
        for (BookingDetail detail : history) {
            if (BookingStatus.isActive(detail.getStatus())) {
                try {
                    SessionManager.setCurrentUser(student);
                    bookingService.cancelBooking(studentId, detail.getBookingId());
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void resetBed(int bedId, String status) {
        bedDAO.updateStatus(bedId, status);
    }
}
