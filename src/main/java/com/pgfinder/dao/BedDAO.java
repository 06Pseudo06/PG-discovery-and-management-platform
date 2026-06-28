package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Bed;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    /* ==========================
       CREATE
       ========================== */

    public boolean insert(Bed bed) {

        String sql = """
                INSERT INTO beds(room_id, bed_label, status, deposit)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, bed.getRoomId());
            ps.setString(2, bed.getBedLabel());
            ps.setString(3, bed.getStatus());
            ps.setDouble(4, bed.getDeposit());

            int rows = ps.executeUpdate();

            if (rows > 0) {

                try (ResultSet rs = ps.getGeneratedKeys()) {

                    if (rs.next()) {
                        bed.setId(rs.getInt(1));
                    }

                }

                return true;
            }

            return false;

        } catch (SQLException e) {
            throw new RuntimeException("Error inserting Bed.", e);
        }
    }

    /* ==========================
       UPDATE
       ========================== */

    public boolean update(Bed bed) {

        String sql = """
                UPDATE beds
                SET bed_label=?,
                    status=?,
                    deposit=?
                WHERE id=?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, bed.getBedLabel());
            ps.setString(2, bed.getStatus());
            ps.setDouble(3, bed.getDeposit());
            ps.setInt(4, bed.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating Bed: " + bed.getId(), e);
        }
    }

    /* ==========================
       DELETE
       ========================== */

    public boolean delete(int bedId) {

        String sql = "DELETE FROM beds WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bedId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Bed: " + bedId, e);
        }
    }

    /* ==========================
       FIND ONE
       ========================== */

    public Bed findById(int id) {

        String sql = "SELECT * FROM beds WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapRowToBed(rs);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding Bed: " + id, e);
        }

        return null;
    }

    /* ==========================
       FIND ALL
       ========================== */

    public List<Bed> findAll() {

        List<Bed> beds = new ArrayList<>();

        String sql = """
                SELECT *
                FROM beds
                ORDER BY bed_label
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                beds.add(mapRowToBed(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving Beds.", e);
        }

        return beds;
    }

    /* ==========================
       FIND BY ROOM
       ========================== */

    public List<Bed> findByRoomId(int roomId) {

        List<Bed> beds = new ArrayList<>();

        String sql = """
                SELECT *
                FROM beds
                WHERE room_id=?
                ORDER BY bed_label
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    beds.add(mapRowToBed(rs));
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving Beds.", e);
        }

        return beds;
    }

    /* ==========================
       CHECK DUPLICATE LABEL
       ========================== */

    public boolean existsBedLabel(int roomId, String bedLabel) {

        String sql = """
                SELECT COUNT(*)
                FROM beds
                WHERE room_id=?
                AND bed_label=?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setString(2, bedLabel);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error checking Bed Label.", e);
        }

        return false;
    }

    /* ==========================
       UPDATE STATUS
       ========================== */

    public boolean updateStatus(int bedId, String status) {

        String sql = """
                UPDATE beds
                SET status=?
                WHERE id=?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, bedId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating Bed Status.", e);
        }
    }

    /* ==========================
       ROOM STATISTICS
       ========================== */

    public int countBedsByRoom(int roomId) {

        String sql = "SELECT COUNT(*) FROM beds WHERE room_id=?";

        return executeCountQuery(sql, roomId);
    }

    public int countOccupiedBeds(int roomId) {

        String sql = """
                SELECT COUNT(*)
                FROM beds
                WHERE room_id=?
                AND status='occupied'
                """;

        return executeCountQuery(sql, roomId);
    }

    public int countVacantBeds(int roomId) {

        String sql = """
                SELECT COUNT(*)
                FROM beds
                WHERE room_id=?
                AND status='vacant'
                """;

        return executeCountQuery(sql, roomId);
    }

    /* ==========================
       PRIVATE HELPERS
       ========================== */

    private int executeCountQuery(String sql, int roomId) {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1);
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Error executing count query.", e);
        }

        return 0;
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

