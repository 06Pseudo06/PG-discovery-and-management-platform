package com.pgfinder.controller;

import com.pgfinder.service.AuthService;
import com.pgfinder.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private TextField phoneField;

    @FXML
    private Button registerButton;

    @FXML
    private Label errorLabel;

    @FXML
    private Hyperlink goToLoginLink;

    @FXML
    public void initialize() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
        if (roleComboBox != null) {
            roleComboBox.setItems(FXCollections.observableArrayList("STUDENT", "OWNER"));
        }
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = roleComboBox.getValue();
        String phone = phoneField.getText();

        // 1. All fields non-empty check
        if (name == null || name.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty() ||
            role == null || role.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty()) {
            errorLabel.setText("All fields are required");
            return;
        }

        name = name.trim();
        email = email.trim();
        phone = phone.trim();

        // 2. Passwords match check
        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        // 3. Basic email format check (@ symbol)
        if (!email.contains("@")) {
            errorLabel.setText("Invalid email address");
            return;
        }

        try {
            authService.register(name, email, password, role, phone);
            // On success, go to Login
            SceneManager.switchTo("Login.fxml");
        } catch (Exception e) {
            // Do not clear the form on failure
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void handleGoToLogin() {
        SceneManager.switchTo("Login.fxml");
    }
}
