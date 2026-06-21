package com.pgfinder.controller;

import com.pgfinder.model.User;
import com.pgfinder.service.AuthService;
import com.pgfinder.service.AuthenticationException;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink goToRegisterLink;

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password");
            return;
        }

        try {
            User user = authService.login(email.trim(), password);
            SessionManager.setCurrentUser(user);
            
            if ("OWNER".equals(user.getRole())) {
                SceneManager.switchTo("OwnerDashboard.fxml");
            } else if ("STUDENT".equals(user.getRole())) {
                SceneManager.switchTo("StudentBrowse.fxml");
            } else {
                errorLabel.setText("Unknown user role: " + user.getRole());
            }
        } catch (AuthenticationException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        SceneManager.switchTo("Register.fxml");
    }
}
