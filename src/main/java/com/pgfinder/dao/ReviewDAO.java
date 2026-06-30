package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    /**
     * Fetches all reviews across properties owned by a specific landlord/owner.
     * Traces relation path: reviews -> verifications -> booking_requests -> users (student)
     */
    public List<Review> getReviewsByOwner(int ownerId) {
        List<Review> reviews = new ArrayList<>();
        
        String sql = "SELECT r.*, u.name AS student_name, p.name AS pg_name " +
                     "FROM reviews r " +
                     "JOIN pgs p ON r.pg_id = p.id " +
                     "JOIN verifications v ON r.verification_id = v.id " +
                     "JOIN booking_requests b ON v.booking_id = b.id " +
                     "JOIN users u ON b.student_id = u.id " +
                     "WHERE p.owner_id = ? " +
                     "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, ownerId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = mapRowToReview(rs);
                    review.setStudentName(rs.getString("student_name"));
                    review.setPgName(rs.getString("pg_name"));
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ReviewDAO: Failed to execute getReviewsByOwner for owner ID " + ownerId);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return reviews;
    }

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

    // =========================================================================
    //                      STUDENT DYNAMIC METHODS
    // =========================================================================

    /**
     * Fetches names and IDs of PGs where the student has an approved or confirmed booking request.
     */
    public List<String[]> getStudentStayedPGs(int studentId) {
        List<String[]> pgs = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id, p.name FROM pgs p " +
                     "JOIN rooms r ON r.pg_id = p.id " +
                     "JOIN beds bd ON bd.room_id = r.id " +
                     "JOIN booking_requests b ON b.bed_id = bd.id " +
                     "WHERE b.student_id = ? AND b.status IN ('approved', 'confirmed')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pgs.add(new String[]{String.valueOf(rs.getInt("id")), rs.getString("name")});
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ReviewDAO: Failed to fetch stayed PGs for student " + studentId);
            e.printStackTrace();
        }
        return pgs;
    }

    /**
     * Fetches all reviews submitted by a specific student.
     */
    public List<Review> getReviewsByStudent(int studentId) {
        List<Review> list = new ArrayList<>();
        String sql = "SELECT r.*, p.name AS pg_name FROM reviews r " +
                     "JOIN pgs p ON r.pg_id = p.id " +
                     "JOIN verifications v ON r.verification_id = v.id " +
                     "JOIN booking_requests b ON v.booking_id = b.id " +
                     "WHERE b.student_id = ? " +
                     "ORDER BY r.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = mapRowToReview(rs);
                    review.setPgName(rs.getString("pg_name"));
                    list.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ReviewDAO: Failed to fetch reviews for student " + studentId);
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Inserts or updates a review instance inside the database cleanly.
     */
    public boolean saveOrUpdateReview(Review r) {
        String checkSql = "SELECT id FROM reviews WHERE id = ? OR (verification_id = ? AND pg_id = ?)";
        try (Connection conn = DBConnection.getConnection()) {
            
            int existingId = -1;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, r.getId());
                ps.setInt(2, r.getVerificationId());
                ps.setInt(3, r.getPgId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getInt("id");
                    }
                }
            }

            if (existingId != -1) {
                String updateSql = "UPDATE reviews SET food_rating=?, cleanliness_rating=?, wifi_rating=?, " +
                                   "owner_behavior_rating=?, comment=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, r.getFoodRating());
                    ps.setInt(2, r.getCleanlinessRating());
                    ps.setInt(3, r.getWifiRating());
                    ps.setInt(4, r.getOwnerBehaviorRating());
                    ps.setString(5, r.getComment());
                    ps.setInt(6, existingId);
                    return ps.executeUpdate() > 0;
                }
            } else {
                String insertSql = "INSERT INTO reviews (verification_id, pg_id, food_rating, cleanliness_rating, " +
                                   "wifi_rating, owner_behavior_rating, comment, created_at) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, r.getVerificationId());
                    ps.setInt(2, r.getPgId());
                    ps.setInt(3, r.getFoodRating());
                    ps.setInt(4, r.getCleanlinessRating());
                    ps.setInt(5, r.getWifiRating());
                    ps.setInt(6, r.getOwnerBehaviorRating());
                    ps.setString(7, r.getComment());
                    return ps.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ReviewDAO: Failed to save/update review record.");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper to trace and fetch a matching verification ID for a student's stay at a PG.
     */
    public int getVerificationIdForStay(int studentId, int pgId) {
        String sql = "SELECT v.id FROM verifications v " +
                     "JOIN booking_requests b ON v.booking_id = b.id " +
                     "JOIN beds bd ON b.bed_id = bd.id " +
                     "JOIN rooms r ON bd.room_id = r.id " +
                     "WHERE b.student_id = ? AND r.pg_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, pgId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ReviewDAO: Verification tracking lookup failure.");
            e.printStackTrace();
        }
        return 0; 
    }

    /**
     * Helper method to map basic database column values directly into a Review instance object.
     */
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