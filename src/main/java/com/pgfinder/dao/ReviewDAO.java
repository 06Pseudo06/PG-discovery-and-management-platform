package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public Review findById(int id) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReview(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for Review: " + id, e);
        }
        return null;
    }

    public List<Review> findAll() {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reviews.add(mapRowToReview(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for Reviews", e);
        }
        return reviews;
    }

    private Review mapRowToReview(ResultSet rs) throws SQLException {
        return new Review(
            rs.getInt("id"),
            rs.getInt("verification_id"),
            rs.getInt("pg_id"),
            rs.getInt("food_rating"),
            rs.getInt("cleanliness_rating"),
            rs.getInt("wifi_rating"),
            rs.getInt("owner_behavior_rating"),
            rs.getString("comment"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
