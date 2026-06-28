package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    /**
     * Add a new room.
     */
    public boolean insert(Room room) {
        String sql = """
                INSERT INTO rooms (pg_id, room_number, room_type, rent)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, room.getPgId());
            ps.setString(2, room.getRoomNumber());
            ps.setString(3, room.getRoomType());
            ps.setDouble(4, room.getRent());

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        room.setId(rs.getInt(1));
                    }
                }
                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting room.", e);
        }
    }

    /**
     * Update room details.
     */
    public boolean update(Room room) {

        String sql = """
                UPDATE rooms
                SET room_number = ?,
                    room_type = ?,
                    rent = ?
                WHERE id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getRoomType());
            ps.setDouble(3, room.getRent());
            ps.setInt(4, room.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating room: " + room.getId(), e);
        }
    }

    /**
     * Delete room.
     */
    public boolean delete(int roomId) {

        String sql = "DELETE FROM rooms WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting room: " + roomId, e);
        }
    }

    /**
     * Check duplicate room number inside same PG.
     */
    public boolean existsRoomNumber(int pgId, String roomNumber) {

        String sql = """
                SELECT COUNT(*)
                FROM rooms
                WHERE pg_id = ?
                AND room_number = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pgId);
            ps.setString(2, roomNumber);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking duplicate room.", e);
        }

        return false;
    }

    /**
     * Find room by ID.
     */
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
            throw new RuntimeException("Error finding room by ID: " + id, e);
        }

        return null;
    }

    /**
     * Find every room.
     */
    public List<Room> findAll() {

        List<Room> rooms = new ArrayList<>();

        String sql = """
                SELECT *
                FROM rooms
                ORDER BY room_number
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving rooms.", e);
        }

        return rooms;
    }

    /**
     * Find all rooms of one PG.
     */
    public List<Room> findByPgId(int pgId) {

        List<Room> rooms = new ArrayList<>();

        String sql = """
                SELECT *
                FROM rooms
                WHERE pg_id = ?
                ORDER BY room_number
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pgId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    rooms.add(mapRowToRoom(rs));
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving rooms for PG: " + pgId, e);
        }

        return rooms;
    }

    /**
     * Count rooms inside a PG.
     */
    public int getRoomCount(int pgId) {

        String sql = """
                SELECT COUNT(*)
                FROM rooms
                WHERE pg_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pgId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error counting rooms.", e);
        }

        return 0;
    }

    /**
     * Maps database row to Room object.
     */
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