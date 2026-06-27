package com.pgfinder.controller;

import java.net.URL;
import java.util.ResourceBundle;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressIndicator;

public class OwnerDashboardController implements Initializable {

    @FXML
    private ProgressIndicator occupancyIndicator;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (occupancyIndicator != null) {
            occupancyIndicator.skinProperty().addListener((obs, oldSkin, newSkin) -> {
                if (newSkin != null) {
                    javafx.scene.Node textNode = occupancyIndicator.lookup(".percentage");
                    if (textNode != null) {
                        textNode.setVisible(false);
                    }
                }
            });
        }
    }

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
    public void openChat() {
        SceneManager.switchTo("Chat.fxml");
    }

    @FXML
    public void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("Login.fxml");
    }
}
