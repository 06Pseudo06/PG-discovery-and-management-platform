package com.pgfinder.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaMigrator {

    public static void main(String[] args) {
        System.out.println("Starting Database Schema Migration...");
        try {
            runMigration();
            System.out.println("Database Schema Migration completed successfully.");
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void runMigration() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            addColumnIfMissing(stmt, "users", "profile_image_path VARCHAR(255) DEFAULT NULL");

            String[] pgColumns = {
                "ac_available BOOLEAN NOT NULL DEFAULT FALSE",
                "laundry_available BOOLEAN NOT NULL DEFAULT FALSE",
                "gym_available BOOLEAN NOT NULL DEFAULT FALSE",
                "parking_available BOOLEAN NOT NULL DEFAULT FALSE"
            };
            for (String col : pgColumns) {
                addColumnIfMissing(stmt, "pgs", col);
            }

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pg_images (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    pg_id INT NOT NULL," +
                "    image_path VARCHAR(255) NOT NULL," +
                "    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS wishlist (" +
                "    student_id INT NOT NULL," +
                "    pg_id INT NOT NULL," +
                "    PRIMARY KEY (student_id, pg_id)," +
                "    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE" +
                ")"
            );

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS notifications (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    user_id INT NOT NULL," +
                "    message TEXT NOT NULL," +
                "    is_read BOOLEAN NOT NULL DEFAULT FALSE," +
                "    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")"
            );

            migrateBookingRequestsTable(stmt);
            createMessagesTable(stmt);
            createAnnouncementsTable(stmt);
        }
    }

    private static void addColumnIfMissing(Statement stmt, String table, String columnDef) throws SQLException {
        String colName = columnDef.split(" ")[0];
        try {
            stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + columnDef);
            System.out.println("Added column " + colName + " to " + table + ".");
        } catch (SQLException e) {
            if (e.getErrorCode() == 1060) {
                System.out.println("Column " + colName + " already exists in " + table + ".");
            } else {
                throw e;
            }
        }
    }

    private static void migrateBookingRequestsTable(Statement stmt) throws SQLException {
        addColumnIfMissing(stmt, "booking_requests", "end_date DATE NULL");
        addColumnIfMissing(stmt, "booking_requests", "owner_remarks TEXT NULL");
        addColumnIfMissing(stmt, "booking_requests", "student_notes TEXT NULL");

        stmt.executeUpdate("UPDATE booking_requests SET status = 'pending' WHERE status IS NULL");
        stmt.executeUpdate("UPDATE booking_requests SET status = 'pending_owner' WHERE status = 'pending'");
        stmt.executeUpdate("UPDATE booking_requests SET status = 'confirmed' WHERE status = 'approved'");

        try {
            stmt.executeUpdate(
                "ALTER TABLE booking_requests MODIFY COLUMN status " +
                "ENUM('pending_owner', 'awaiting_student_confirmation', 'confirmed', 'rejected', 'cancelled') " +
                "NOT NULL DEFAULT 'pending_owner'"
            );
            System.out.println("Updated booking_requests.status enum.");
        } catch (SQLException e) {
            System.out.println("booking_requests.status enum update note: " + e.getMessage());
        }
    }

    private static void createMessagesTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS messages (" +
            "    id INT AUTO_INCREMENT PRIMARY KEY," +
            "    sender_id INT NOT NULL," +
            "    receiver_id INT NOT NULL," +
            "    booking_id INT NULL," +
            "    body TEXT NOT NULL," +
            "    is_read BOOLEAN NOT NULL DEFAULT FALSE," +
            "    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE," +
            "    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE," +
            "    FOREIGN KEY (booking_id) REFERENCES booking_requests(id) ON DELETE SET NULL" +
            ")"
        );
        System.out.println("Table messages checked/created successfully.");
    }

    private static void createAnnouncementsTable(Statement stmt) throws SQLException {
        stmt.executeUpdate(
            "CREATE TABLE IF NOT EXISTS announcements (" +
            "    id INT AUTO_INCREMENT PRIMARY KEY," +
            "    pg_id INT NOT NULL," +
            "    owner_id INT NOT NULL," +
            "    title VARCHAR(200) NOT NULL," +
            "    message TEXT NOT NULL," +
            "    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE," +
            "    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")"
        );
        System.out.println("Table announcements checked/created successfully.");
    }
}
