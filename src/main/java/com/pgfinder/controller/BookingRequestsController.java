package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;

public class BookingRequestsController {

    @FXML
    public void initialize() {
        // Init logic for requests if needed
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("OwnerDashboard.fxml");
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
    private void openTenants() {
        SceneManager.switchTo("Tenants.fxml");
    }

    @FXML
    private void openAnnouncements() {
        SceneManager.switchTo("Announcements.fxml");
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