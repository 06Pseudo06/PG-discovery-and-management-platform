package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean insert(Connection conn, int userId, String message) throws SQLException {
        String sql = """
                INSERT INTO notifications (user_id, message, is_read)
                VALUES (?, ?, FALSE)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean insert(int userId, String message) {
        String sql = """
                INSERT INTO notifications (user_id, message, is_read)
                VALUES (?, ?, FALSE)
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting notification.", e);
        }
    }

    public List<Notification> findByUserId(int userId) {
        List<Notification> notifications = new ArrayList<>();
        String sql = """
                SELECT *
                FROM notifications
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving notifications for user: " + userId, e);
        }
        return notifications;
    }

    public int countUnread(int userId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread notifications.", e);
        }
        return 0;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return notification;
    }
}
