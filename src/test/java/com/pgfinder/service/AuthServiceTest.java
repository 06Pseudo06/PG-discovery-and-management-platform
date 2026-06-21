package com.pgfinder.service;

import com.pgfinder.config.DBConnection;
import com.pgfinder.dao.UserDAO;
import com.pgfinder.model.User;
import com.pgfinder.util.BCryptUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {
    private AuthService authService;
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        authService = new AuthService();
        userDAO = new UserDAO();
        // Clean up test emails just in case they were left over
        deleteUserByEmail("test_login@example.com");
        deleteUserByEmail("test_register@example.com");
    }

    @AfterEach
    public void tearDown() {
        // Clean up test emails after runs
        deleteUserByEmail("test_login@example.com");
        deleteUserByEmail("test_register@example.com");
    }

    private void deleteUserByEmail(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Warning during teardown cleanup: " + e.getMessage());
        }
    }

    @Test
    public void testLoginSuccess() throws Exception {
        // First register a valid user
        authService.register("Test User", "test_login@example.com", "password123", "STUDENT", "1234567890");

        // Attempt login
        User loggedInUser = authService.login("test_login@example.com", "password123");
        
        assertNotNull(loggedInUser, "User should be returned on successful login");
        assertEquals("test_login@example.com", loggedInUser.getEmail());
        assertEquals("Test User", loggedInUser.getName());
        assertEquals("STUDENT", loggedInUser.getRole());
        assertEquals("1234567890", loggedInUser.getPhone());
        assertTrue(loggedInUser.getId() > 0, "User ID should be generated and set");
    }

    @Test
    public void testLoginWrongPassword() throws Exception {
        // First register a valid user
        authService.register("Test User", "test_login@example.com", "password123", "STUDENT", "1234567890");

        // Attempt login with wrong password
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login("test_login@example.com", "wrongpassword");
        });

        assertEquals("Invalid email or password", exception.getMessage(), 
            "Error message must be identical to specification");
    }

    @Test
    public void testLoginNonExistentEmail() {
        // Attempt login with non-existent email
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login("test_login@example.com", "password123");
        });

        assertEquals("Invalid email or password", exception.getMessage(), 
            "Error message must be identical to specification");
    }

    @Test
    public void testRegistrationSuccess() throws Exception {
        User registeredUser = authService.register("Test Owner", "test_register@example.com", "ownerpass", "OWNER", "9876543210");

        assertNotNull(registeredUser, "User should be returned upon successful registration");
        assertEquals("test_register@example.com", registeredUser.getEmail());
        assertEquals("OWNER", registeredUser.getRole());
        assertTrue(registeredUser.getId() > 0, "User ID should be generated and set");

        // Verify the database record
        User dbUser = userDAO.findByEmail("test_register@example.com");
        assertNotNull(dbUser, "User should be persisted in database");
        assertEquals("Test Owner", dbUser.getName());
        assertTrue(BCryptUtil.verify("ownerpass", dbUser.getPasswordHash()), "Password hash should be valid BCrypt");
        assertNotEquals("ownerpass", dbUser.getPasswordHash(), "Plain password should not be stored");
    }

    @Test
    public void testRegistrationDuplicateEmail() throws Exception {
        // Register first time
        authService.register("Test Owner", "test_register@example.com", "ownerpass", "OWNER", "9876543210");

        // Attempt register with same email
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Another Owner", "test_register@example.com", "otherpass", "OWNER", "5555555555");
        });

        assertEquals("An account with this email already exists", exception.getMessage(),
            "Error message must be clear duplicate notification");
    }

    @Test
    public void testRegistrationInvalidRole() {
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register("Test Student", "test_register@example.com", "pass", "INVALID_ROLE", "1234567890");
        });
    }
}
