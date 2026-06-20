package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.BookingRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public BookingRequest findById(int id) {
        String sql = "SELECT * FROM booking_requests WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBookingRequest(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for BookingRequest: " + id, e);
        }
        return null;
    }

    public List<BookingRequest> findAll() {
        List<BookingRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM booking_requests";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                requests.add(mapRowToBookingRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for BookingRequests", e);
        }
        return requests;
    }

    private BookingRequest mapRowToBookingRequest(ResultSet rs) throws SQLException {
        Timestamp decidedAtTs = rs.getTimestamp("decided_at");
        return new BookingRequest(
            rs.getInt("id"),
            rs.getInt("student_id"),
            rs.getInt("bed_id"),
            rs.getString("status"),
            rs.getDate("requested_move_in_date").toLocalDate(),
            rs.getTimestamp("created_at").toLocalDateTime(),
            decidedAtTs != null ? decidedAtTs.toLocalDateTime() : null
        );
    }
}
