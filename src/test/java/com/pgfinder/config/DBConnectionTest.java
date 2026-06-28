package com.pgfinder.config;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

public class DBConnectionTest {
    @Test
    public void testGetConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
        } catch (Exception e) {
            fail("Exception occurred while trying to connect to the database: " + e.getMessage());
        }
    }

    @Test
    public void cleanupTestPG() {
        try (Connection conn = DBConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            boolean auto = conn.getAutoCommit();
            System.out.println("AutoCommit status: " + auto);
            
            // Delete associated bookings, beds, rooms if any exist for test PGs (id > 9)
            stmt.executeUpdate("DELETE FROM beds WHERE room_id IN (SELECT id FROM rooms WHERE pg_id > 9)");
            stmt.executeUpdate("DELETE FROM rooms WHERE pg_id > 9");
            int deleted = stmt.executeUpdate("DELETE FROM pgs WHERE id > 9");
            System.out.println("Deleted Test PG records count: " + deleted);
            
            // Restore Andheri Elite PG (id=9) if it was deleted in previous runs
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT id FROM pgs WHERE id = 9")) {
                if (!rs.next()) {
                    System.out.println("Restoring Andheri Elite PG (id=9) and its rooms/beds...");
                    stmt.executeUpdate("INSERT INTO pgs (id, owner_id, name, address, city, area, description, gender_preference, food_available, wifi_available) VALUES (9, 1, 'Andheri Elite PG', 'Veera Desai Road, Andheri West', 'Mumbai', 'Andheri', 'Premium girls PG near cinemas and cafes.', 'female', TRUE, TRUE)");
                    stmt.executeUpdate("INSERT INTO rooms (id, pg_id, room_number, room_type, rent) VALUES (17, 9, '901', 'Double Sharing', 11000.00), (18, 9, '902', 'Single Sharing', 14500.00)");
                    stmt.executeUpdate("INSERT INTO beds (id, room_id, bed_label, status, deposit) VALUES (31, 17, '901-A', 'vacant', 22000.00), (32, 17, '901-B', 'vacant', 22000.00), (33, 18, '902-A', 'vacant', 25000.00)");
                }
            }
            
            if (!auto) {
                conn.commit();
                System.out.println("Committed transaction.");
            }
            
            // Print other PGs in Pune to verify
            try (java.sql.ResultSet rs = stmt.executeQuery("SELECT id, name, city, gender_preference FROM pgs WHERE city = 'Pune'")) {
                while (rs.next()) {
                    System.out.println("PUNE_PG: ID=" + rs.getInt("id") + ", Name=" + rs.getString("name") + ", Gender=" + rs.getString("gender_preference"));
                }
            }
        } catch (Exception e) {
            fail("Exception during db cleanup: " + e.getMessage());
        }
    }
}
