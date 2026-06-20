package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public Room findById(int id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for Room: " + id, e);
        }
        return null;
    }

    public List<Room> findAll() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for Rooms", e);
        }
        return rooms;
    }

    public List<Room> findByPgId(int pgId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms WHERE pg_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pgId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRowToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findByPgId for PG: " + pgId, e);
        }
        return rooms;
    }

    private Room mapRowToRoom(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("id"),
            rs.getInt("pg_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getDouble("rent")
        );
    }
}
