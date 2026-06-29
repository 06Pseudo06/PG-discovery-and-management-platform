package com.pgfinder.controller;

import com.pgfinder.model.PG;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.BookingRefreshHelper;
import com.pgfinder.util.DashboardBadgeHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;

import java.net.URL;
import java.util.ResourceBundle;

public class OwnerDashboardController implements Initializable {

    @FXML
    private ProgressIndicator occupancyIndicator;
    @FXML
    private Label pendingBadgeLabel;
    @FXML
    private Label chatBadgeLabel;
    @FXML
    private Label pendingRequestsStatLabel;
    @FXML
    private Label confirmedTenantsStatLabel;

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
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        int ownerId = SessionManager.getCurrentUser().getId();
        DashboardBadgeHelper.updatePendingRequestsBadge(pendingBadgeLabel, ownerId);
        DashboardBadgeHelper.updateChatBadge(chatBadgeLabel, ownerId);

        int pending = BookingRefreshHelper.getPendingRequestCount(ownerId);
        int confirmed = BookingRefreshHelper.getApprovedBookingCount(ownerId);
        if (pendingRequestsStatLabel != null) {
            pendingRequestsStatLabel.setText(String.valueOf(pending));
        }
        if (confirmedTenantsStatLabel != null) {
            confirmedTenantsStatLabel.setText(String.valueOf(confirmed));
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
    public void openReports() {
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon",
                "The reports dashboard and analytics module will be available in the next system update.");
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
