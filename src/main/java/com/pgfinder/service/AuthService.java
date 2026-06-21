package com.pgfinder.service;

import com.pgfinder.dao.UserDAO;
import com.pgfinder.model.User;
import com.pgfinder.util.BCryptUtil;

public class AuthService {
    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String plainPassword) throws AuthenticationException {
        User user = userDAO.findByEmail(email);
        if (user == null || !BCryptUtil.verify(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password");
        }
        return user;
    }

    public User register(String name, String email, String plainPassword, String role, String phone) throws Exception {
        // Validate role
        if (!"STUDENT".equals(role) && !"OWNER".equals(role)) {
            throw new IllegalArgumentException("Invalid role: must be STUDENT or OWNER");
        }

        // Check if email already taken
        if (userDAO.findByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        // Hash password and save user
        String hashedPassword = BCryptUtil.hash(plainPassword);
        User user = new User(0, name, email, hashedPassword, role, phone);
        return userDAO.insert(user);
    }
}
