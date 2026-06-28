package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() {
        userDAO = new UserDAO();
        cleanupTestUser();
    }

    @AfterEach
    public void tearDown() {
        cleanupTestUser();
    }

    private void cleanupTestUser() {
        String sql = "DELETE FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "profile_test@example.com");
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Teardown clean failed: " + e.getMessage());
        }
    }

    @Test
    public void testUpdateUserAndProfileImage() {
        // 1. Insert a user
        User user = new User(0, "Original Name", "profile_test@example.com", "hash123", "STUDENT", "1111111111", null);
        user = userDAO.insert(user);
        assertNotNull(user);
        assertTrue(user.getId() > 0);

        // Verify initial state
        User dbUser = userDAO.findById(user.getId());
        assertNotNull(dbUser);
        assertEquals("Original Name", dbUser.getName());
        assertEquals("1111111111", dbUser.getPhone());
        assertNull(dbUser.getProfileImagePath());

        // 2. Modify properties and call update
        dbUser.setName("Updated Name");
        dbUser.setPhone("2222222222");
        dbUser.setProfileImagePath("/uploads/pics/test.png");
        userDAO.update(dbUser);

        // 3. Verify changes are persisted
        User updatedUser = userDAO.findById(dbUser.getId());
        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("2222222222", updatedUser.getPhone());
        assertEquals("/uploads/pics/test.png", updatedUser.getProfileImagePath());
    }
}
