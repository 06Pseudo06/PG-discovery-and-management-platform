package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
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
    private void openDashboard() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    private void openMyPGs() {
        SceneManager.switchTo("MyPGs.fxml");
    }

    @FXML
    private void openRoomsBeds() {
        SceneManager.switchTo("RoomsBeds.fxml");
    }

    @FXML
    private void openBookingRequests() {
        SceneManager.switchTo("BookingRequests.fxml");
    }

    @FXML
    private void openTenants() {
        SceneManager.switchTo("Tenants.fxml");
    }

    @FXML
    private void openChat() {
        SceneManager.switchTo("Chat.fxml");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("Login.fxml");
    }
}