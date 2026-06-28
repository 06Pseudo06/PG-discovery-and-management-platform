package com.pgfinder.controller;

import com.pgfinder.model.PG;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AnnouncementsController {

    @FXML
    private ComboBox<String> pgSelect;

    @FXML
    private ComboBox<String> prioritySelect;

    @FXML
    private TextField titleField;

    @FXML
    private TextArea messageArea;

    @FXML
    private DatePicker scheduleDate;

    @FXML
    public void initialize() {
        if (pgSelect != null) {
            pgSelect.setItems(FXCollections.observableArrayList(
                "All Properties",
                "Sunrise PG",
                "Green Residency",
                "Student Hub"
            ));
            pgSelect.setValue("All Properties");
        }
        if (prioritySelect != null) {
            prioritySelect.setItems(FXCollections.observableArrayList(
                "High Priority",
                "Medium Priority",
                "Low Priority"
            ));
            prioritySelect.setValue("Medium Priority");
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    private void handlePostAnnouncement() {
        String title = titleField.getText();
        String msg = messageArea.getText();
        if (title == null || title.trim().isEmpty() || msg == null || msg.trim().isEmpty()) {
            return;
        }

        // Broadcast logic placeholder
        titleField.clear();
        messageArea.clear();
        if (scheduleDate != null) {
            scheduleDate.setValue(null);
        }
    }

    // Sidebar navigation actions
    @FXML
    public void openDashboard() {
        SceneManager.switchTo("OwnerDashboard.fxml");
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
    public void openRoomsBeds(PG pg) {
        SelectedPGManager.setSelectedPG(pg);
        SceneManager.switchTo("RoomsBeds.fxml");
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
    public void openAnnouncements() {
        SceneManager.switchTo("Announcements.fxml");
    }

    @FXML
    public void openReviews() {
        SceneManager.switchTo("OwnerReviews.fxml");
    }

    @FXML
    public void openReports() {
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon", "The reports dashboard and analytics module will be available in the next system update.");
    }

    @FXML
    public void openSettings() {
        SceneManager.switchTo("OwnerSettings.fxml");
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("Login.fxml");
    }
}