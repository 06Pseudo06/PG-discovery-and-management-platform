package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;

public class RoomsBedsController {

    @FXML
    public void initialize() {
        // Init room statuses if needed
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("MyPGs.fxml");
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
    private void openBookingRequests() {
        SceneManager.switchTo("BookingRequests.fxml");
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