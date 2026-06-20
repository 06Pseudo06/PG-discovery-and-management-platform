package com.pgfinder.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String PROPERTIES_FILE = "config.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find config.properties in the classpath. "
                        + "Please make sure config.properties is in src/main/resources/");
            }
            properties.load(input);
            // Load driver explicitly
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read database configuration properties file: " + PROPERTIES_FILE, e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found in dependencies.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String pass = properties.getProperty("db.password");

        if (url == null || user == null || pass == null) {
            throw new RuntimeException("Invalid config.properties: db.url, db.username, and db.password must be defined.");
        }

        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            throw new SQLException("Failed to establish a database connection to " + url + " as user " + user, e);
        }
    }
}
