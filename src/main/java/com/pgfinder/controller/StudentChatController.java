package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class StudentChatController {

    @FXML
    private VBox messagesContainer;

    @FXML
    private TextField messageInput;

    @FXML
    public void initialize() {
        // Initialization code if needed
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }

        // Create message bubble
        VBox messageBox = new VBox(4);
        messageBox.setAlignment(Pos.TOP_RIGHT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        String time = LocalTime.now().format(formatter);

        Label headerLabel = new Label("You • " + time);
        headerLabel.getStyleClass().add("chat-time");

        Label bubbleLabel = new Label(text.trim());
        bubbleLabel.getStyleClass().add("message-sent");
        bubbleLabel.setWrapText(true);

        messageBox.getChildren().addAll(headerLabel, bubbleLabel);

        HBox wrapper = new HBox();
        wrapper.setAlignment(Pos.CENTER_RIGHT);
        wrapper.getChildren().add(messageBox);

        messagesContainer.getChildren().add(wrapper);
        messageInput.clear();
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
    private void openPGHistory() {
        SceneManager.switchTo("PGHistory.fxml");
    }

    @FXML
    private void openReviews() {
        SceneManager.switchTo("Reviews.fxml");
    }

    @FXML
    private void openSettings() {
        SceneManager.switchTo("Settings.fxml");
    }
}
