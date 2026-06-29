package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Tenant;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    public List<Tenant> getTenantsByOwner(int ownerId) {
        List<Tenant> tenants = new ArrayList<>();
        

        // The running query
String query = "SELECT u.name, u.email, u.phone, " +
               "IFNULL(p.name, 'Premium Staying PG') AS pg_name, " +
               "IFNULL(r.room_number, '101') AS room_number, " +
               "IFNULL(r.room_type, 'Single') AS room_type, " +
               "IFNULL(r.rent, 5000.0) AS rent, " +
               "b.requested_move_in_date, " +
               "b.end_date " +
               "FROM booking_requests b " +
               "JOIN users u ON b.student_id = u.id " +
               "LEFT JOIN beds bd ON b.bed_id = bd.id " +
               "LEFT JOIN rooms r ON bd.room_id = r.id " +
               "LEFT JOIN pgs p ON r.pg_id = p.id " +
               "WHERE TRIM(LOWER(b.status)) IN ('approved', 'confirmed')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            try (ResultSet rs = pstmt.executeQuery()) {
                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    Date moveInSql = rs.getDate("requested_move_in_date");
                    Date endSql = rs.getDate("end_date");

                    java.time.LocalDate moveInDate = moveInSql != null ? moveInSql.toLocalDate() : java.time.LocalDate.now();
                    java.time.LocalDate endDate = endSql != null ? endSql.toLocalDate() : moveInDate.plusMonths(6);

                    tenants.add(new Tenant(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("pg_name"),
                        rs.getString("room_number"),
                        rs.getString("room_type"),
                        rs.getDouble("rent"),
                        moveInDate,
                        endDate
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] TenantDAO: SQL Exception occurred!");
            e.printStackTrace();
        }
        return tenants;
    }
}