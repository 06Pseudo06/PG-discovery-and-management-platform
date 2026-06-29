package com.pgfinder.controller;

import com.pgfinder.model.Announcement;
import com.pgfinder.model.Notification;
import com.pgfinder.service.AnnouncementService;
import com.pgfinder.util.DashboardBadgeHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentAnnouncementsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final AnnouncementService announcementService = new AnnouncementService();

    @FXML
    private VBox announcementsContainer;
    @FXML
    private VBox notificationsContainer;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label notificationBadgeLabel;

    @FXML
    public void initialize() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
        refreshContent();
    }

    private void refreshContent() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        int userId = SessionManager.getCurrentUser().getId();
        DashboardBadgeHelper.updateNotificationBadge(notificationBadgeLabel, userId);
        loadAnnouncements(userId);
        loadNotifications(userId);
    }

    private void loadAnnouncements(int studentId) {
        if (announcementsContainer == null) {
            return;
        }
        announcementsContainer.getChildren().clear();
        List<Announcement> announcements = announcementService.getStudentAnnouncements(studentId);

        if (announcements.isEmpty()) {
            announcementsContainer.getChildren().add(new Label("No announcements from your PG yet."));
            return;
        }

        for (Announcement announcement : announcements) {
            VBox card = new VBox(6);
            card.getStyleClass().add("content-card");
            Label title = new Label(announcement.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            Label pg = new Label(announcement.getPgName() + " • " + announcement.getCreatedAt().format(DATE_FORMAT));
            pg.getStyleClass().add("sub-heading");
            Label body = new Label(announcement.getMessage());
            body.setWrapText(true);
            card.getChildren().addAll(title, pg, body);
            announcementsContainer.getChildren().add(card);
        }
    }

    private void loadNotifications(int userId) {
        if (notificationsContainer == null) {
            return;
        }
        notificationsContainer.getChildren().clear();
        List<Notification> notifications = DashboardBadgeHelper.getRecentNotifications(userId, 10);

        if (notifications.isEmpty()) {
            notificationsContainer.getChildren().add(new Label("No notifications yet."));
            return;
        }

        for (Notification notification : notifications) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("announcement-item");
            Label message = new Label(notification.getMessage());
            message.setWrapText(true);
            message.getStyleClass().add("sub-heading");
            HBox.setHgrow(message, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().add(message);
            notificationsContainer.getChildren().add(row);
        }
    }

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
    private void openReviews() {
        SceneManager.switchTo("Reviews.fxml");
    }

    @FXML
    private void openSettings() {
        SceneManager.switchTo("Settings.fxml");
    }
}
