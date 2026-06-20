package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Verification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VerificationDAO {

    public Verification findById(int id) {
        String sql = "SELECT * FROM verifications WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToVerification(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for Verification: " + id, e);
        }
        return null;
    }

    public List<Verification> findAll() {
        List<Verification> verifications = new ArrayList<>();
        String sql = "SELECT * FROM verifications";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                verifications.add(mapRowToVerification(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for Verifications", e);
        }
        return verifications;
    }

    private Verification mapRowToVerification(ResultSet rs) throws SQLException {
        Timestamp confirmedAtTs = rs.getTimestamp("confirmed_at");
        return new Verification(
            rs.getInt("id"),
            rs.getInt("booking_id"),
            rs.getString("code"),
            rs.getTimestamp("generated_at").toLocalDateTime(),
            rs.getTimestamp("expires_at").toLocalDateTime(),
            confirmedAtTs != null ? confirmedAtTs.toLocalDateTime() : null,
            rs.getString("status")
        );
    }
}
