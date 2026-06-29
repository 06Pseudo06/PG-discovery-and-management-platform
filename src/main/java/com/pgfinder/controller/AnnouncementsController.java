package com.pgfinder.controller;

import com.pgfinder.model.PG;
import com.pgfinder.service.AnnouncementService;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.PGService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.DashboardBadgeHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementsController {

    private final AnnouncementService announcementService = new AnnouncementService();
    private final PGService pgService = new PGService();
    private final Map<String, Integer> pgNameToId = new HashMap<>();

    @FXML
    private ComboBox<String> pgSelect;
    @FXML
    private ComboBox<String> prioritySelect;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea messageArea;
    @FXML
    private DatePicker scheduleDate;
    @FXML
    private Label pendingBadgeLabel;
    @FXML
    private Label chatBadgeLabel;

    @FXML
    public void initialize() {
        loadPgOptions();
        if (SessionManager.isLoggedIn()) {
            DashboardBadgeHelper.updatePendingRequestsBadge(pendingBadgeLabel,
                    SessionManager.getCurrentUser().getId());
            DashboardBadgeHelper.updateChatBadge(chatBadgeLabel, SessionManager.getCurrentUser().getId());
        }
    }

    private void loadPgOptions() {
        if (pgSelect == null || !SessionManager.isLoggedIn()) {
            return;
        }
        pgNameToId.clear();
        List<PG> pgs = pgService.getOwnerPGs(SessionManager.getCurrentUser().getId());
        List<String> names = pgs.stream().map(PG::getName).toList();
        pgSelect.setItems(FXCollections.observableArrayList(names));
        if (!names.isEmpty()) {
            pgSelect.getSelectionModel().selectFirst();
            for (PG pg : pgs) {
                pgNameToId.put(pg.getName(), pg.getId());
            }
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    private void handlePostAnnouncement() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        String title = titleField.getText();
        String msg = messageArea.getText();
        String pgName = pgSelect != null ? pgSelect.getSelectionModel().getSelectedItem() : null;

        if (title == null || title.trim().isEmpty() || msg == null || msg.trim().isEmpty()) {
            AlertUtil.showWarning("Validation", "Missing Fields", "Title and message are required.");
            return;
        }
        if (pgName == null || !pgNameToId.containsKey(pgName)) {
            AlertUtil.showWarning("Validation", "Select PG", "Please select a PG to broadcast to.");
            return;
        }

        try {
            announcementService.postAnnouncement(
                    SessionManager.getCurrentUser().getId(),
                    pgNameToId.get(pgName),
                    title.trim(),
                    msg.trim());
            AlertUtil.showInfo("Posted", "Announcement Sent",
                    "Your announcement was broadcast to tenants of " + pgName + ".");
            titleField.clear();
            messageArea.clear();
            if (scheduleDate != null) {
                scheduleDate.setValue(null);
            }
        } catch (BookingException ex) {
            AlertUtil.showWarning("Post Failed", "Unable to Post", ex.getMessage());
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
                    "Please select a specific PG property from your list first.");
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
    public void openChat() {
        SceneManager.switchTo("Chat.fxml");
    }

    @FXML
    public void openReviews() {
        SceneManager.switchTo("OwnerReviews.fxml");
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
