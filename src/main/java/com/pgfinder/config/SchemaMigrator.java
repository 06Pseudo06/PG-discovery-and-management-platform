package com.pgfinder.config;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class SchemaMigrator {

    public static void main(String[] args) {
        System.out.println("Starting Database Schema Migration...");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Alter users table
            try {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN profile_image_path VARCHAR(255) DEFAULT NULL");
                System.out.println("Added profile_image_path column to users table.");
            } catch (SQLException e) {
                if (e.getErrorCode() == 1060) { // Duplicate column name
                    System.out.println("Column profile_image_path already exists in users table.");
                } else {
                    throw e;
                }
            }

            // 2. Alter pgs table for extra amenities
            String[] pgColumns = {
                "ac_available BOOLEAN NOT NULL DEFAULT FALSE",
                "laundry_available BOOLEAN NOT NULL DEFAULT FALSE",
                "gym_available BOOLEAN NOT NULL DEFAULT FALSE",
                "parking_available BOOLEAN NOT NULL DEFAULT FALSE"
            };
            for (String col : pgColumns) {
                String colName = col.split(" ")[0];
                try {
                    stmt.executeUpdate("ALTER TABLE pgs ADD COLUMN " + col);
                    System.out.println("Added column " + colName + " to pgs table.");
                } catch (SQLException e) {
                    if (e.getErrorCode() == 1060) {
                        System.out.println("Column " + colName + " already exists in pgs table.");
                    } else {
                        throw e;
                    }
                }
            }

            // 3. Create pg_images table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS pg_images (" +
                "    id INT AUTO_INCREMENT PRIMARY KEY," +
                "    pg_id INT NOT NULL," +
                "    image_path VARCHAR(255) NOT NULL," +
                "    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE" +
                ")"
            );
            System.out.println("Table pg_images checked/created successfully.");

            // 4. Create wishlist table
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS wishlist (" +
                "    student_id INT NOT NULL," +
                "    pg_id INT NOT NULL," +
                "    PRIMARY KEY (student_id, pg_id)," +
                "    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
                "    FOREIGN KEY (pg_id) REFERENCES pgs(id) ON DELETE CASCADE" +
                ")"
            );
            System.out.println("Table wishlist checked/created successfully.");

            // 5. Create notifications table
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
            System.out.println("Table notifications checked/created successfully.");

            System.out.println("Database Schema Migration completed successfully.");

        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
