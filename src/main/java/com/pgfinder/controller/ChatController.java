package com.pgfinder.controller;

import com.pgfinder.model.PG;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    public void initialize() {
        // Init chat elements if needed
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        VBox messageBox = new VBox(4);
        messageBox.setAlignment(Pos.TOP_RIGHT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String time = LocalTime.now().format(formatter);

        Label headerLabel = new Label("You • " + time);
        headerLabel.getStyleClass().add("stat-sublabel");

        Label bubbleLabel = new Label(text.trim());
        bubbleLabel.getStyleClass().add("message-sent");
        bubbleLabel.setWrapText(true);
        // Matching visual padding
        bubbleLabel.setStyle("-fx-padding: 10 14; -fx-font-size: 13px;");

        messageBox.getChildren().addAll(headerLabel, bubbleLabel);

        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.getChildren().add(messageBox);

        messagesContainer.getChildren().add(wrapper);
        messageInput.clear();
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
