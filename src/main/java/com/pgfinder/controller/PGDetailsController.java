package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class PGDetailsController {

    @FXML
    public void initialize() {
        // Init details if needed
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("BrowsePG.fxml");
    }

    @FXML
    public void goBack(ActionEvent event) {
        goBack();
    }

    @FXML
    public void openBooking() {
        SceneManager.switchTo("Booking.fxml");
    }

    @FXML
    public void openBooking(ActionEvent event) {
        openBooking();
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
    private void openAnnouncements() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
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

    @FXML
    private void openSettings() {
        SceneManager.switchTo("Settings.fxml");
    }
}