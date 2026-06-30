package com.pgfinder.service;

import com.pgfinder.config.DBConnection;
import com.pgfinder.dao.AnnouncementDAO;
import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.dao.PGDAO;
import com.pgfinder.model.Announcement;
import com.pgfinder.model.PG;
import com.pgfinder.util.NotificationMessages;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AnnouncementService {

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final PGDAO pgDAO = new PGDAO();



    public void postAnnouncement(int ownerId, int pgId, String title, String message) {
        if (title == null || title.trim().isEmpty()) throw new BookingException("Announcement title is required.");
        if (message == null || message.trim().isEmpty()) throw new BookingException("Announcement message is required.");

        PG pg = pgDAO.findById(pgId);
        if (pg == null || pg.getOwnerId() != ownerId) throw new BookingException("Unauthorized.");

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int id = announcementDAO.insert(conn, pgId, ownerId, title.trim(), message.trim());
                if (id <= 0) throw new BookingException("Unable to post announcement.");

                String text = NotificationMessages.ANNOUNCEMENT_POSTED + " [" + title.trim() + "]";
                for (int studentId : announcementDAO.findStudentIdsForPg(pgId)) {
                    notificationDAO.insert(conn, studentId, text);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new BookingException("Error saving announcement.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Database connection failure.");
        }
    }

    public boolean updateAnnouncement(int id, int pgId, String title, String message) {
        return announcementDAO.update(id, pgId, title.trim(), message.trim());
    }

    public boolean deleteAnnouncement(int id) {
        return announcementDAO.delete(id);
    }

    public List<Announcement> getOwnerAnnouncements(int ownerId) {
        return announcementDAO.findByOwnerId(ownerId);
    }

    public List<Announcement> getStudentAnnouncements(int studentId) {
        return announcementDAO.findForStudent(studentId);
    }
} 