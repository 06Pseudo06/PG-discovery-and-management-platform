package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class ReviewsController {

    @FXML
    private ComboBox<String> pgSelect;

    @FXML
    private Button star1;
    @FXML
    private Button star2;
    @FXML
    private Button star3;
    @FXML
    private Button star4;
    @FXML
    private Button star5;

    @FXML
    private Label ratingLabel;

    @FXML
    private TextArea reviewComments;

    private int selectedRating = 0;

    @FXML
    public void initialize() {
        if (pgSelect != null) {
            pgSelect.setItems(FXCollections.observableArrayList(
                "Sunrise PG (Active Stay)",
                "Comfort Living Hostel (Past Stay)",
                "Student Nest Hostel (Past Stay)"
            ));
        }
        updateStars();
    }

    @FXML
    private void rate1() { selectedRating = 1; updateStars(); }
    @FXML
    private void rate2() { selectedRating = 2; updateStars(); }
    @FXML
    private void rate3() { selectedRating = 3; updateStars(); }
    @FXML
    private void rate4() { selectedRating = 4; updateStars(); }
    @FXML
    private void rate5() { selectedRating = 5; updateStars(); }

    private void updateStars() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < 5; i++) {
            if (stars[i] != null) {
                stars[i].getStyleClass().removeAll("star-active", "star-inactive");
                if (i < selectedRating) {
                    stars[i].getStyleClass().add("star-active");
                } else {
                    stars[i].getStyleClass().add("star-inactive");
                }
            }
        }
        if (ratingLabel != null) {
            if (selectedRating == 0) {
                ratingLabel.setText("(Select Stars)");
            } else {
                ratingLabel.setText("(" + selectedRating + ".0 / 5.0)");
            }
        }
    }

    @FXML
    private void handleSubmitReview() {
        if (pgSelect.getValue() == null) {
            // Validation
            return;
        }
        // Mock success submission logic
        reviewComments.clear();
        selectedRating = 0;
        updateStars();
    }

    @FXML
    private void handleEditReview() {
        // Mock loading previous review to editor
        pgSelect.setValue("Comfort Living Hostel (Past Stay)");
        selectedRating = 4;
        updateStars();
        reviewComments.setText("Great location and Mr. Aman was highly responsive. Housekeeping was super clean.");
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
    private void openChat() {
        SceneManager.switchTo("StudentChat.fxml");
    }

    @FXML
    private void openPGHistory() {
        SceneManager.switchTo("PGHistory.fxml");
    }

    @FXML
    private void openAnnouncements() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
    }

    @FXML
    private void openSettings() {
        SceneManager.switchTo("Settings.fxml");
    }
}
