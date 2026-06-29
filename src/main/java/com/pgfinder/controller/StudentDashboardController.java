package com.pgfinder.controller;

import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.Notification;
import com.pgfinder.service.BookingService;
import com.pgfinder.util.DashboardBadgeHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboardController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final BookingService bookingService = new BookingService();

    @FXML
    private Label greetingLabel;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label currentStayLabel;
    @FXML
    private Label roomBedLabel;
    @FXML
    private Label activePgLabel;
    @FXML
    private Label moveInLabel;
    @FXML
    private Label notificationBadgeLabel;
    @FXML
    private VBox notificationsPreviewBox;
    @FXML
    private Button notificationButton;

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        var user = SessionManager.getCurrentUser();
        if (profileNameLabel != null) {
            profileNameLabel.setText("Hi, " + user.getName());
        }
        if (greetingLabel != null) {
            greetingLabel.setText("Good Morning, " + user.getName() + "! ☀️");
        }

        DashboardBadgeHelper.updateNotificationBadge(notificationBadgeLabel, user.getId());
        loadStaySummary(user.getId());
        loadNotificationPreview(user.getId());
    }

    private void loadStaySummary(int studentId) {
        BookingDetail stay = bookingService.getActiveStayForStudent(studentId);
        if (stay == null) {
            if (currentStayLabel != null) {
                currentStayLabel.setText("No active stay");
            }
            if (roomBedLabel != null) {
                roomBedLabel.setText("Browse PGs to book");
            }
            if (activePgLabel != null) {
                activePgLabel.setText("—");
            }
            if (moveInLabel != null) {
                moveInLabel.setText("—");
            }
            return;
        }

        if (currentStayLabel != null) {
            currentStayLabel.setText(stay.getPgName());
        }
        if (roomBedLabel != null) {
            roomBedLabel.setText("Room " + stay.getRoomNumber() + " • " + stay.getBedLabel());
        }
        if (activePgLabel != null) {
            activePgLabel.setText(stay.getPgName());
        }
        if (moveInLabel != null) {
            moveInLabel.setText(stay.getStartDate().format(DATE_FORMAT));
        }
    }

    private void loadNotificationPreview(int userId) {
        if (notificationsPreviewBox == null) {
            return;
        }
        notificationsPreviewBox.getChildren().clear();
        List<Notification> notifications = DashboardBadgeHelper.getRecentNotifications(userId, 3);
        if (notifications.isEmpty()) {
            notificationsPreviewBox.getChildren().add(new Label("No new notifications."));
            return;
        }
        for (Notification notification : notifications) {
            Label label = new Label("• " + notification.getMessage());
            label.setWrapText(true);
            label.getStyleClass().add("sub-heading");
            notificationsPreviewBox.getChildren().add(label);
        }
    }

    @FXML
    private void openNotifications() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
    }

    @FXML
    private void openDashboard() {
        SceneManager.switchTo("StudentDashboard.fxml");
    }

    @FXML
    public void openBrowsePG() {
        SceneManager.switchTo("BrowsePG.fxml");
    }

    @FXML
    public void openBrowsePG(ActionEvent event) {
        openBrowsePG();
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
