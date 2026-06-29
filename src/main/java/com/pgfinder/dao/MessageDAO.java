package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public int insert(Connection conn, int senderId, int receiverId, Integer bookingId, String body)
            throws SQLException {
        String sql = """
                INSERT INTO messages (sender_id, receiver_id, booking_id, body, is_read)
                VALUES (?, ?, ?, ?, FALSE)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            if (bookingId != null) {
                ps.setInt(3, bookingId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, body);
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

    public List<Message> findConversation(int userId, int otherUserId) {
        String sql = """
                SELECT m.*, s.name AS sender_name, r.name AS receiver_name
                FROM messages m
                JOIN users s ON m.sender_id = s.id
                JOIN users r ON m.receiver_id = r.id
                WHERE (m.sender_id = ? AND m.receiver_id = ?)
                   OR (m.sender_id = ? AND m.receiver_id = ?)
                ORDER BY m.created_at ASC
                """;
        List<Message> messages = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading conversation.", e);
        }
        return messages;
    }

    public void markConversationRead(int userId, int otherUserId) {
        String sql = """
                UPDATE messages
                SET is_read = TRUE
                WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, otherUserId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking messages read.", e);
        }
    }

    public int countUnreadForUser(int userId) {
        String sql = "SELECT COUNT(*) FROM messages WHERE receiver_id = ? AND is_read = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread messages.", e);
        }
        return 0;
    }

    public List<Integer> findConversationPartnerIds(int userId) {
        String sql = """
                SELECT DISTINCT
                    CASE WHEN sender_id = ? THEN receiver_id ELSE sender_id END AS partner_id
                FROM messages
                WHERE sender_id = ? OR receiver_id = ?
                """;
        List<Integer> partners = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    partners.add(rs.getInt("partner_id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding conversation partners.", e);
        }
        return partners;
    }

    public Message findLatestBetween(int userId, int otherUserId) {
        String sql = """
                SELECT m.*, s.name AS sender_name, r.name AS receiver_name
                FROM messages m
                JOIN users s ON m.sender_id = s.id
                JOIN users r ON m.receiver_id = r.id
                WHERE (m.sender_id = ? AND m.receiver_id = ?)
                   OR (m.sender_id = ? AND m.receiver_id = ?)
                ORDER BY m.created_at DESC
                LIMIT 1
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, otherUserId);
            ps.setInt(3, otherUserId);
            ps.setInt(4, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest message.", e);
        }
        return null;
    }

    public int countUnreadFromSender(int userId, int senderId) {
        String sql = """
                SELECT COUNT(*)
                FROM messages
                WHERE receiver_id = ? AND sender_id = ? AND is_read = FALSE
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, senderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread from sender.", e);
        }
        return 0;
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getInt("id"));
        message.setSenderId(rs.getInt("sender_id"));
        message.setReceiverId(rs.getInt("receiver_id"));
        int bookingId = rs.getInt("booking_id");
        if (!rs.wasNull()) {
            message.setBookingId(bookingId);
        }
        message.setBody(rs.getString("body"));
        message.setRead(rs.getBoolean("is_read"));
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        message.setSenderName(rs.getString("sender_name"));
        message.setReceiverName(rs.getString("receiver_name"));
        return message;
    }
}
