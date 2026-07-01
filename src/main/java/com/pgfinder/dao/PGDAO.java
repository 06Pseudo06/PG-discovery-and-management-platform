package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.PG;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PGDAO {


    public List<PG> findAll() {
        List<PG> pgs = new ArrayList<>();
        String sql = "SELECT * FROM pgs";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pgs.add(mapRowToPG(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll for PGs", e);
        }
        return pgs;
    }

    public List<PG> search(SearchFilters filters) {
        List<PG> results = new ArrayList<>();
        if (filters == null) {
            return findAll();
        }

        StringBuilder sql = new StringBuilder("SELECT DISTINCT p.* FROM pgs p");
        boolean hasBudget = (filters.getMinBudget() != null || filters.getMaxBudget() != null);
        if (hasBudget) {
            sql.append(" JOIN rooms r ON p.id = r.pg_id");
        }
        sql.append(" WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (filters.getCity() != null && !filters.getCity().trim().isEmpty()) {
            sql.append(" AND p.city = ?");
            params.add(filters.getCity().trim());
        }

        if (filters.getGender() != null && !filters.getGender().trim().isEmpty() && !filters.getGender().equalsIgnoreCase("any")) {
            String gender = filters.getGender().toLowerCase().trim();
            if (gender.equals("female")) {
                sql.append(" AND p.gender_preference IN ('female', 'any')");
            } else if (gender.equals("male")) {
                sql.append(" AND p.gender_preference IN ('male', 'any')");
            } else {
                sql.append(" AND p.gender_preference = ?");
                params.add(gender);
            }
        }

        if (filters.getFoodRequired() != null && filters.getFoodRequired()) {
            sql.append(" AND p.food_available = ?");
            params.add(true);
        }

        if (filters.getWifiRequired() != null && filters.getWifiRequired()) {
            sql.append(" AND p.wifi_available = ?");
            params.add(true);
        }

        if (filters.getMinBudget() != null) {
            sql.append(" AND r.rent >= ?");
            params.add(filters.getMinBudget());
        }

        if (filters.getMaxBudget() != null) {
            sql.append(" AND r.rent <= ?");
            params.add(filters.getMaxBudget());
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRowToPG(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing search for PGs", e);
        }

        return results;
    }

    public List<String> findDistinctAreas() {
        List<String> areas = new ArrayList<>();
        String sql = "SELECT DISTINCT area FROM pgs";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String area = rs.getString("area");
                if (area != null && !area.trim().isEmpty()) {
                    areas.add(area.trim());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching distinct area names", e);
        }
        return areas;
    }

    public List<PG> findByOwnerId(int ownerId) {
        List<PG> ownerPgs = new ArrayList<>();
        String sql = "SELECT * FROM pgs WHERE owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ownerPgs.add(mapRowToPG(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findByOwnerId for owner: " + ownerId, e);
        }
        return ownerPgs;
    }

public PG findById(int pgId) {

    String sql = """
        SELECT *
        FROM pgs
        WHERE id = ?
        """;

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, pgId);

        try (ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return mapRowToPG(rs);
            }

        }

    } catch (SQLException e) {

        throw new RuntimeException(
                "Unable to load PG by id.",
                e
        );

    }

    return null;
} 

    public PG insert(PG pg) {
        String sql = "INSERT INTO pgs (owner_id, name, address, city, area, description, gender_preference, food_available, wifi_available, ac_available, laundry_available, gym_available, parking_available) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pg.getOwnerId());
            ps.setString(2, pg.getName());
            ps.setString(3, pg.getAddress());
            ps.setString(4, pg.getCity());
            ps.setString(5, pg.getArea());
            ps.setString(6, pg.getDescription());
            ps.setString(7, pg.getGenderPreference());
            ps.setBoolean(8, pg.isFoodAvailable());
            ps.setBoolean(9, pg.isWifiAvailable());
            ps.setBoolean(10, pg.isAcAvailable());
            ps.setBoolean(11, pg.isLaundryAvailable());
            ps.setBoolean(12, pg.isGymAvailable());
            ps.setBoolean(13, pg.isParkingAvailable());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating PG failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    pg.setId(generatedKeys.getInt(1));
                    return pg;
                } else {
                    throw new SQLException("Creating PG failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing insert for PG: " + pg.getName(), e);
        }
    }

    public void update(PG pg) {
        String sql = "UPDATE pgs SET name = ?, address = ?, city = ?, area = ?, description = ?, gender_preference = ?, food_available = ?, wifi_available = ?, ac_available = ?, laundry_available = ?, gym_available = ?, parking_available = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pg.getName());
            ps.setString(2, pg.getAddress());
            ps.setString(3, pg.getCity());
            ps.setString(4, pg.getArea());
            ps.setString(5, pg.getDescription());
            ps.setString(6, pg.getGenderPreference());
            ps.setBoolean(7, pg.isFoodAvailable());
            ps.setBoolean(8, pg.isWifiAvailable());
            ps.setBoolean(9, pg.isAcAvailable());
            ps.setBoolean(10, pg.isLaundryAvailable());
            ps.setBoolean(11, pg.isGymAvailable());
            ps.setBoolean(12, pg.isParkingAvailable());
            ps.setInt(13, pg.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing update for PG: " + pg.getId(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM pgs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error executing delete for PG: " + id, e);
        }
    }

    private PG mapRowToPG(ResultSet rs) throws SQLException {
        return new PG(
            rs.getInt("id"),
            rs.getInt("owner_id"),
            rs.getString("name"),
            rs.getString("address"),
            rs.getString("city"),
            rs.getString("area"),
            rs.getString("description"),
            rs.getString("gender_preference"),
            rs.getBoolean("food_available"),
            rs.getBoolean("wifi_available"),
            rs.getBoolean("ac_available"),
            rs.getBoolean("laundry_available"),
            rs.getBoolean("gym_available"),
            rs.getBoolean("parking_available")
        );
    }
}
