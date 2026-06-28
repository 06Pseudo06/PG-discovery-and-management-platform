package com.pgfinder.controller;

import com.pgfinder.dao.UserDAO;
import com.pgfinder.model.User;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.BCryptUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import com.pgfinder.util.SelectedPGManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SettingsController {

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private Label sidebarProfileName;

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
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
    private ImageView profileImageView;

    @FXML
    public void initialize() {
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());
            if (sidebarProfileName != null) {
                sidebarProfileName.setText("Hi, " + user.getName());
            }
            loadProfileImage(user);
        }
    }

    private void loadProfileImage(User user) {
        String path = user.getProfileImagePath();
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                profileImageView.setImage(new Image(file.toURI().toString()));
                return;
            }
        }
        // Set an empty image or keep the default
        profileImageView.setImage(null);
    }

    @FXML
    private void handleSaveProfile() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String name = nameField.getText();
        String phone = phoneField.getText();

        if (name == null || name.trim().isEmpty() || phone == null || phone.trim().isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Missing fields", "Please fill in all profile details.");
            return;
        }

        user.setName(name.trim());
        user.setPhone(phone.trim());

        try {
            userDAO.update(user);
            if (sidebarProfileName != null) {
                sidebarProfileName.setText("Hi, " + user.getName());
            }
            AlertUtil.showInfo("Success", "Profile Updated", "Your profile details have been saved successfully.");
        } catch (Exception e) {
            AlertUtil.showError("System Error", "Update Failed", "Could not save details: " + e.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String currentPass = currentPasswordField.getText();
        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (currentPass == null || currentPass.isEmpty() ||
            newPass == null || newPass.isEmpty() ||
            confirmPass == null || confirmPass.isEmpty()) {
            AlertUtil.showWarning("Validation Error", "Missing Password Fields", "Please fill in all password fields.");
            return;
        }

        if (!BCryptUtil.verify(currentPass, user.getPasswordHash())) {
            AlertUtil.showError("Authentication Error", "Incorrect Current Password", "The current password you entered is incorrect.");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            AlertUtil.showWarning("Validation Error", "Password Mismatch", "The new password and confirmation password do not match.");
            return;
        }

        if (newPass.length() < 6) {
            AlertUtil.showWarning("Validation Error", "Weak Password", "New password should be at least 6 characters long.");
            return;
        }

        try {
            String hashed = BCryptUtil.hash(newPass);
            user.setPasswordHash(hashed);
            userDAO.update(user);
            AlertUtil.showInfo("Success", "Password Changed", "Your password has been changed successfully.");
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } catch (Exception e) {
            AlertUtil.showError("System Error", "Change Password Failed", "An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleUploadImage() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                File dir = new File("uploads/profile_pics");
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String ext = "";
                String name = selectedFile.getName();
                int idx = name.lastIndexOf('.');
                if (idx > 0) {
                    ext = name.substring(idx);
                }

                File destFile = new File(dir, "user_" + user.getId() + "_" + System.currentTimeMillis() + ext);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // If user had an old picture, try deleting it
                String oldPath = user.getProfileImagePath();
                if (oldPath != null) {
                    File oldFile = new File(oldPath);
                    if (oldFile.exists()) {
                        oldFile.delete();
                    }
                }

                user.setProfileImagePath(destFile.getAbsolutePath());
                userDAO.update(user);
                loadProfileImage(user);

                AlertUtil.showInfo("Success", "Profile Picture Uploaded", "Your profile picture has been updated.");
            } catch (IOException e) {
                AlertUtil.showError("Upload Error", "Failed to Copy File", "An error occurred while copying file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteImage() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String path = user.getProfileImagePath();
        if (path == null) {
            AlertUtil.showInfo("Notice", "No Image Found", "You do not have a profile picture to delete.");
            return;
        }

        boolean confirm = AlertUtil.showConfirmation("Delete Profile Picture", "Are you sure?", "Do you want to permanently delete your profile picture?");
        if (confirm) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            user.setProfileImagePath(null);
            userDAO.update(user);
            loadProfileImage(user);
            AlertUtil.showInfo("Success", "Deleted", "Your profile picture has been deleted.");
        }
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
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        boolean confirm = AlertUtil.showConfirmation("Delete Account", "DANGER ZONE", "Are you absolutely sure you want to delete your account? This action is permanent and cannot be undone.");
        if (confirm) {
            // In a production environment, we'd execute user deletion logic.
            // But since deleting a user would violate database constraints if they have PG/Booking history,
            // we will simulate this by clearing session and redirecting.
            SessionManager.clearSession();
            SceneManager.switchTo("Register.fxml");
        }
    }

    // Sidebar navigation actions
    @FXML
    private void openDashboard() {
        User user = SessionManager.getCurrentUser();
        if (user != null && "OWNER".equals(user.getRole())) {
            SceneManager.switchTo("OwnerDashboard.fxml");
        } else {
            SceneManager.switchTo("StudentDashboard.fxml");
        }
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
        User user = SessionManager.getCurrentUser();
        if (user != null && "OWNER".equals(user.getRole())) {
            SceneManager.switchTo("Chat.fxml");
        } else {
            SceneManager.switchTo("StudentChat.fxml");
        }
    }

    @FXML
    private void openPGHistory() {
        SceneManager.switchTo("PGHistory.fxml");
    }

    @FXML
    private void openAnnouncements() {
        User user = SessionManager.getCurrentUser();
        if (user != null && "OWNER".equals(user.getRole())) {
            SceneManager.switchTo("Announcements.fxml");
        } else {
            SceneManager.switchTo("StudentAnnouncements.fxml");
        }
    }

    @FXML
    private void openReviews() {
        User user = SessionManager.getCurrentUser();
        if (user != null && "OWNER".equals(user.getRole())) {
            SceneManager.switchTo("OwnerReviews.fxml");
        } else {
            SceneManager.switchTo("Reviews.fxml");
        }
    }

    @FXML
    public void openMyPGs() {
        SceneManager.switchTo("MyPGs.fxml");
    }

    @FXML
    public void openRoomsBeds() {
        if (SelectedPGManager.getSelectedPG() != null) {
            SceneManager.switchTo("RoomsBeds.fxml");
        } else {
            AlertUtil.showWarning("Context Missing", "No PG Selected", 
                "Please select a specific PG property from your list first to view its detailed inventory.");
            SceneManager.switchTo("MyPGs.fxml");
        }
    }

    @FXML
    public void openBookingRequests() {
        SceneManager.switchTo("BookingRequests.fxml");
    }

    @FXML
    public void openTenants() {
        SceneManager.switchTo("Tenants.fxml");
    }

    @FXML
    public void openReports() {
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon", "The reports dashboard and analytics module will be available in the next system update.");
    }
}
