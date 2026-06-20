package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Bed;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    public Bed findById(int id) {
        String sql = "SELECT * FROM beds WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBed(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for Bed: " + id, e);
        }
        return null;
    }

    public List<Bed> findAll() {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT * FROM beds";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                beds.add(mapRowToBed(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for Beds", e);
        }
        return beds;
    }

    public List<Bed> findByRoomId(int roomId) {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT * FROM beds WHERE room_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    beds.add(mapRowToBed(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findByRoomId for Room: " + roomId, e);
        }
        return beds;
    }

    private Bed mapRowToBed(ResultSet rs) throws SQLException {
        return new Bed(
            rs.getInt("id"),
            rs.getInt("room_id"),
            rs.getString("bed_label"),
            rs.getString("status"),
            rs.getDouble("deposit")
        );
    }
}
