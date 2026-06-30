package com.pgfinder.controller;

import com.pgfinder.dao.ReviewDAO;
import com.pgfinder.model.Review;
import com.pgfinder.util.SessionManager;
import com.pgfinder.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class OwnerReviewsController {

    @FXML private Label avgRatingLabel;
    @FXML private Label avgStarsLabel;
    @FXML private Label totalReviewsLabel;

    @FXML private ProgressBar progress5, progress4, progress3, progress2, progress1;
    @FXML private Label labelCount5, labelCount4, labelCount3, labelCount2, labelCount1;

    @FXML private VBox reviewsContainer;

    private final ReviewDAO reviewDAO = new ReviewDAO();

    @FXML
    public void initialize() {
        if (SessionManager.getCurrentUser() != null) {
            int currentOwnerId = SessionManager.getCurrentUser().getId();
            loadOwnerReviewsData(currentOwnerId);
        }
    }

    private void loadOwnerReviewsData(int ownerId) {
        List<Review> reviews = reviewDAO.getReviewsByOwner(ownerId);
        reviewsContainer.getChildren().clear();

        if (reviews.isEmpty()) {
            avgRatingLabel.setText("0.0 / 5.0");
            avgStarsLabel.setText("☆☆☆☆☆");
            totalReviewsLabel.setText("Based on 0 reviews");
            return;
        }

        int total = reviews.size();
        double sum = 0;
        int[] counts = new int[6]; // index 1 to 5 for star buckets

        for (Review r : reviews) {
            // Calculating overall aggregate star rating from sub-criteria
            double combinedRating = (r.getFoodRating() + r.getCleanlinessRating() + r.getWifiRating() + r.getOwnerBehaviorRating()) / 4.0;
            int score = (int) Math.round(combinedRating);
            if (score < 1) score = 1;
            if (score > 5) score = 5;
            
            sum += combinedRating;
            counts[score]++;
            
            // Build dynamic review UI card component container
            reviewsContainer.getChildren().add(createReviewCard(r, score));
        }

        // Compute average metrics
        double avg = sum / total;
        avgRatingLabel.setText(String.format("%.1f / 5.0", avg));
        avgStarsLabel.setText(generateStarString((int) Math.round(avg)));
        totalReviewsLabel.setText("Based on " + total + " reviews");

        // Update distribution bars
        labelCount5.setText(String.valueOf(counts[5]));
        progress5.setProgress((double) counts[5] / total);

        labelCount4.setText(String.valueOf(counts[4]));
        progress4.setProgress((double) counts[4] / total);

        labelCount3.setText(String.valueOf(counts[3]));
        progress3.setProgress((double) counts[3] / total);

        labelCount2.setText(String.valueOf(counts[2]));
        progress2.setProgress((double) counts[2] / total);

        labelCount1.setText(String.valueOf(counts[1]));
        progress1.setProgress((double) counts[1] / total);
    }

    private VBox createReviewCard(Review review, int mappedScore) {
        VBox card = new VBox(12);
        card.getStyleClass().add("owner-large-card");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Avatar circle element builder
        StackPane avatarCircle = new StackPane();
        avatarCircle.getStyleClass().add("avatar-circle");
        String initials = review.getStudentName() != null && review.getStudentName().length() >= 2 
                ? review.getStudentName().substring(0, 2).toUpperCase() : "ST";
        Label avatarLabel = new Label(initials);
        avatarLabel.getStyleClass().add("avatar-text");
        avatarCircle.getChildren().add(avatarLabel);

        VBox identityBox = new VBox(2);
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(review.getStudentName() != null ? review.getStudentName() : "Anonymous Student");
        nameLabel.getStyleClass().add("item-main-title");
        nameLabel.setStyle("-fx-font-size: 15px;");

        Label contextLabel = new Label("• Resident in " + (review.getPgName() != null ? review.getPgName() : "Property"));
        contextLabel.getStyleClass().add("item-subtitle");
        contextLabel.setStyle("-fx-padding: 0 0 0 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label starsLabel = new Label(generateStarString(mappedScore));
        starsLabel.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px;");

        topRow.getChildren().addAll(nameLabel, contextLabel, spacer, starsLabel);
        identityBox.getChildren().add(topRow);
        header.getChildren().addAll(avatarCircle, identityBox);
        HBox.setHgrow(identityBox, Priority.ALWAYS);

        Separator sep1 = new Separator();
        Label commentLabel = new Label(review.getComment());
        commentLabel.setWrapText(true);
        commentLabel.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13.5px;");

        Separator sep2 = new Separator();

        HBox footerAction = new HBox();
        footerAction.setAlignment(Pos.CENTER_RIGHT);
        Button quickReplyBtn = new Button("Quick Reply");
        quickReplyBtn.getStyleClass().add("review-action-btn");
        quickReplyBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 11px;");
        footerAction.getChildren().add(quickReplyBtn);

        card.getChildren().addAll(header, sep1, commentLabel, sep2, footerAction);
        return card;
    }

    private String generateStarString(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) sb.append("★");
            else sb.append("☆");
        }
        return sb.toString();
    }

    // Sidebar navigation handlers
    @FXML void openDashboard(ActionEvent event) { SceneManager.switchTo("OwnerDashboard.fxml"); }
    @FXML void openMyPGs(ActionEvent event) { SceneManager.switchTo("MyPGs.fxml"); }
    @FXML void openRoomsBeds(ActionEvent event) { SceneManager.switchTo("RoomsBeds.fxml"); }
    @FXML void openBookingRequests(ActionEvent event) { SceneManager.switchTo("BookingRequests.fxml"); }
    @FXML void openTenants(ActionEvent event) { SceneManager.switchTo("Tenants.fxml"); }
    @FXML void openAnnouncements(ActionEvent event) { SceneManager.switchTo("Announcements.fxml"); }
    @FXML void openChat(ActionEvent event) { SceneManager.switchTo("Chat.fxml"); }
    @FXML void openReviews(ActionEvent event) { /* Already on page */ }
    @FXML void openReports(ActionEvent event) { SceneManager.switchTo("OwnerDashboard.fxml"); }
    @FXML void openSettings(ActionEvent event) { SceneManager.switchTo("OwnerSettings.fxml"); }
    @FXML void goBack(ActionEvent event) { openDashboard(event); }
    @FXML void handleLogout(ActionEvent event) { SessionManager.logout(); SceneManager.switchTo("Login.fxml"); }
}
 