package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SettingsController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private CheckBox emailNotificationsToggle;
    @FXML
    private CheckBox chatNotificationsToggle;

    @FXML
    private ToggleButton themeToggleButton;

    @FXML
    public void initialize() {
        // Init logic
    }

    @FXML
    private void handleSaveProfile() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            return;
        }
        // Save logic placeholder
    }

    @FXML
    private void handleChangePassword() {
        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (currentPass == null || currentPass.isEmpty() ||
            newPass == null || newPass.isEmpty() ||
            confirmPass == null || confirmPass.isEmpty()) {
            return;
        }

        if (!newPass.equals(confirmPass)) {
            // Validation warning
            return;
        }

        // Save new password logic
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleToggleTheme() {
        if (themeToggleButton.isSelected()) {
            themeToggleButton.setText("Dark Mode Active");
            themeToggleButton.getStyleClass().removeAll("btn-secondary");
            themeToggleButton.getStyleClass().add("btn-primary");
        } else {
            themeToggleButton.setText("Light Mode Active");
            themeToggleButton.getStyleClass().removeAll("btn-primary");
            themeToggleButton.getStyleClass().add("btn-secondary");
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("Login.fxml");
    }

    @FXML
    private void handleDeleteAccount() {
        // Mock dialog trigger / confirm delete account
        SessionManager.clearSession();
        SceneManager.switchTo("Register.fxml");
    }

    // Sidebar navigation actions
    @FXML
    private void openDashboard() {
        SceneManager.switchTo("StudentDashboard.fxml");
    }

    @FXML
    private void openBrowsePG() {
        SceneManager.switchTo("BrowsePG.fxml");
    }

    @FXML
    private void openMyStay() {
        SceneManager.switchTo("MyStay.fxml");
    }

    @FXML
    private void openChat() {
        SceneManager.switchTo("StudentChat.fxml");
    }

    @FXML
    private void openPGHistory() {
        SceneManager.switchTo("PGHistory.fxml");
    }

    @FXML
    private void openReviews() {
        SceneManager.switchTo("Reviews.fxml");
    }
}
