package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.PG;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PGDAO {

    public PG findById(int id) {
        String sql = "SELECT * FROM pgs WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPG(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById for PG: " + id, e);
        }
        return null;
    }

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
            rs.getBoolean("wifi_available")
        );
    }
}
