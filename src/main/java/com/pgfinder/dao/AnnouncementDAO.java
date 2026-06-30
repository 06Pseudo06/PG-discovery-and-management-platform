package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Announcement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementDAO {

    public int insert(Connection conn, int pgId, int ownerId, String title, String message) throws SQLException {
        String sql = "INSERT INTO announcements (pg_id, owner_id, title, message, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pgId);
            ps.setInt(2, ownerId);
            ps.setString(3, title);
            ps.setString(4, message);
            if (ps.executeUpdate() == 0) {
                return -1;
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean update(int id, int pgId, String title, String message) {
        String sql = "UPDATE announcements SET pg_id = ?, title = ?, message = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pgId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Announcement> findByOwnerId(int ownerId) {
        String sql = "SELECT a.*, p.name AS pg_name FROM announcements a " +
                     "JOIN pgs p ON a.pg_id = p.id " +
                     "WHERE a.owner_id = ? ORDER BY a.created_at DESC";
        return queryAnnouncements(sql, ownerId);
    }

    public List<Announcement> findForStudent(int studentId) {
        String sql = "SELECT a.*, p.name AS pg_name FROM announcements a " +
                     "JOIN pgs p ON a.pg_id = p.id " +
                     "JOIN rooms r ON r.pg_id = p.id " +
                     "JOIN beds b ON b.room_id = r.id " +
                     "JOIN booking_requests br ON br.bed_id = b.id " +
                     "WHERE br.student_id = ? AND br.status IN ('approved', 'confirmed') " +
                     "ORDER BY a.created_at DESC";
        return queryAnnouncements(sql, studentId);
    }

    public List<Integer> findStudentIdsForPg(int pgId) {
        List<Integer> studentIds = new ArrayList<>();
        String sql = "SELECT DISTINCT br.student_id FROM booking_requests br " +
                     "JOIN beds b ON br.bed_id = b.id " +
                     "JOIN rooms r ON b.room_id = r.id " +
                     "WHERE r.pg_id = ? AND br.status IN ('approved', 'confirmed')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pgId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getInt("student_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding students for PG.", e);
        }
        return studentIds;
    }

    private List<Announcement> queryAnnouncements(String sql, int param) {
        List<Announcement> announcements = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    announcements.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading announcements.", e);
        }
        return announcements;
    }

    private Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("id"));
        announcement.setPgId(rs.getInt("pg_id"));
        announcement.setOwnerId(rs.getInt("owner_id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setMessage(rs.getString("message"));
        announcement.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        announcement.setPgName(rs.getString("pg_name"));
        return announcement;
    }
}
 