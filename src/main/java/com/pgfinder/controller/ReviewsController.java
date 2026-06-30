package com.pgfinder.controller;

import com.pgfinder.dao.ReviewDAO;
import com.pgfinder.model.Review;
import com.pgfinder.util.SessionManager;
import com.pgfinder.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewsController {

    @FXML private ComboBox<String> pgSelect;
    @FXML private Button star1, star2, star3, star4, star5;
    @FXML private Label ratingLabel;
    @FXML private TextArea reviewComments;
    @FXML private VBox pastReviewsContainer;

    // Metrics Overview Elements
    @FXML private Label avgRatingLabel;
    @FXML private Label avgStarsLabel;
    @FXML private Label totalReviewsLabel;

    private int selectedRating = 0;
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private List<String[]> stayedPgsList = new ArrayList<>();
    private int currentEditingReviewId = -1;

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            int studentId = SessionManager.getCurrentUser().getId();
            loadStayedProperties(studentId);
            loadPastReviewsFeed(studentId);
        }
        updateStars();
    }

    private void loadStayedProperties(int studentId) {
        stayedPgsList = reviewDAO.getStudentStayedPGs(studentId);
        List<String> pgNames = new ArrayList<>();
        for (String[] record : stayedPgsList) {
            pgNames.add(record[1]); 
        }
        pgSelect.setItems(FXCollections.observableArrayList(pgNames));
    }

    private void loadPastReviewsFeed(int studentId) {
        pastReviewsContainer.getChildren().clear();
        List<Review> pastReviews = reviewDAO.getReviewsByStudent(studentId);

        int total = pastReviews.size();
        double sum = 0;

        for (Review rev : pastReviews) {
            VBox card = new VBox(10);
            card.getStyleClass().add("content-card");

            HBox header = new HBox();
            VBox titles = new VBox(2);
            Label pgTitle = new Label(rev.getPgName());
            pgTitle.getStyleClass().add("chat-name");
            pgTitle.setStyle("-fx-font-size: 15px;");
            
            Label timeLabel = new Label("Submitted: " + rev.getCreatedAt().toLocalDate().toString());
            timeLabel.getStyleClass().add("chat-time");
            titles.getChildren().addAll(pgTitle, timeLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            double combined = (rev.getFoodRating() + rev.getCleanlinessRating() + rev.getWifiRating() + rev.getOwnerBehaviorRating()) / 4.0;
            sum += combined;

            Label starLabel = new Label(generateStarString((int) Math.round(combined)));
            starLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px;");

            header.getChildren().addAll(titles, spacer, starLabel);

            Label body = new Label(rev.getComment());
            body.setWrapText(true);
            body.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");

            Separator sep = new Separator();
            HBox actions = new HBox();
            actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            Button editBtn = new Button("✏ Edit Review");
            editBtn.getStyleClass().add("btn-outline");
            editBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 11px;");
            
            editBtn.setOnAction(e -> populateEditorForEdit(rev));
            actions.getChildren().add(editBtn);

            card.getChildren().addAll(header, body, sep, actions);
            pastReviewsContainer.getChildren().add(card);
        }

        // Programmatically populate overview metric card components
        double avg = total > 0 ? (sum / total) : 0.0;
        avgRatingLabel.setText(String.format("%.1f", avg));
        avgStarsLabel.setText(generateStarString((int) Math.round(avg)));
        totalReviewsLabel.setText("Based on " + total + " past stays");
    }

    private void populateEditorForEdit(Review rev) {
        pgSelect.setValue(rev.getPgName());
        double combined = (rev.getFoodRating() + rev.getCleanlinessRating() + rev.getWifiRating() + rev.getOwnerBehaviorRating()) / 4.0;
        selectedRating = (int) Math.round(combined);
        updateStars();
        reviewComments.setText(rev.getComment());
        currentEditingReviewId = rev.getId(); // Retain ID to flag an edit operation
    }

    @FXML private void rate1() { selectedRating = 1; updateStars(); }
    @FXML private void rate2() { selectedRating = 2; updateStars(); }
    @FXML private void rate3() { selectedRating = 3; updateStars(); }
    @FXML private void rate4() { selectedRating = 4; updateStars(); }
    @FXML private void rate5() { selectedRating = 5; updateStars(); }

    private void updateStars() {
        Button[] stars = {star1, star2, star3, star4, star5};
        for (int i = 0; i < 5; i++) {
            if (i < selectedRating) {
                stars[i].setStyle("-fx-text-fill: #F59E0B;");
            } else {
                stars[i].setStyle("-fx-text-fill: #D1D5DB;");
            }
        }
        ratingLabel.setText("(" + selectedRating + ".0 / 5.0)");
    }

    @FXML
    private void handleSubmitReview() {
        String selectedPgName = pgSelect.getValue();
        if (selectedPgName == null || selectedRating == 0 || reviewComments.getText().trim().isEmpty()) {
            return; 
        }

        int targetPgId = -1;
        for (String[] record : stayedPgsList) {
            if (record[1].equals(selectedPgName)) {
                targetPgId = Integer.parseInt(record[0]);
                break;
            }
        }

        if (targetPgId == -1) return;

        int studentId = SessionManager.getCurrentUser().getId();
        int verificationId = reviewDAO.getVerificationIdForStay(studentId, targetPgId);

        Review rev = new Review();
        if (currentEditingReviewId != -1) {
            rev.setId(currentEditingReviewId);
        }
        rev.setPgId(targetPgId);
        rev.setVerificationId(verificationId);
        
        // Spread selected score over the criteria sub-fields
        rev.setFoodRating(selectedRating);
        rev.setCleanlinessRating(selectedRating);
        rev.setWifiRating(selectedRating);
        rev.setOwnerBehaviorRating(selectedRating);
        rev.setComment(reviewComments.getText().trim());

        if (reviewDAO.saveOrUpdateReview(rev)) {
            reviewComments.clear();
            selectedRating = 0;
            currentEditingReviewId = -1; // Reset state tracking
            updateStars();
            pgSelect.setValue(null);
            loadPastReviewsFeed(studentId); 
        }
    }

    private String generateStarString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? "★" : "☆");
        }
        return sb.toString();
    }

    // Sidebar navigation actions
    @FXML private void openDashboard() { SceneManager.switchTo("StudentDashboard.fxml"); }
    @FXML private void openBrowsePG() { SceneManager.switchTo("BrowsePG.fxml"); }
    @FXML private void openMyStay() { SceneManager.switchTo("MyStay.fxml"); }
    @FXML private void openChat() { SceneManager.switchTo("StudentChat.fxml"); }
    @FXML private void openPGHistory() { SceneManager.switchTo("PGHistory.fxml"); }
    @FXML private void openAnnouncements() { SceneManager.switchTo("StudentAnnouncements.fxml"); }
    @FXML private void openSettings() { SceneManager.switchTo("Settings.fxml"); }
} 
