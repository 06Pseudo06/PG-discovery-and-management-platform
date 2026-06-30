package com.pgfinder.controller;

import com.pgfinder.model.Announcement;
import com.pgfinder.model.PG;
import com.pgfinder.service.AnnouncementService;
import com.pgfinder.service.PGService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnouncementsController {

    private final AnnouncementService announcementService = new AnnouncementService();
    private final PGService pgService = new PGService();
    private final Map<String, Integer> pgNameToId = new HashMap<>();
    private int currentEditingId = -1;

    @FXML private ComboBox<String> pgSelect;
    @FXML private TextField titleField;
    @FXML private TextArea messageArea;
    @FXML private VBox announcementsHistoryContainer;
    @FXML private Button submitBtn;
    @FXML private Button cancelEditBtn;

    @FXML
    public void initialize() {
        loadPgOptions();
        refreshHistoryFeed();
    }

    private void loadPgOptions() {
        if (!SessionManager.isLoggedIn()) return;
        int ownerId = SessionManager.getCurrentUser().getId();
        List<PG> pgs = pgService.getOwnerPGs(ownerId);
        pgNameToId.clear();
        for (PG p : pgs) {
            pgNameToId.put(p.getName(), p.getId());
        }
        pgSelect.setItems(FXCollections.observableArrayList(pgNameToId.keySet()));
    }

    private void refreshHistoryFeed() {
        announcementsHistoryContainer.getChildren().clear();
        if (!SessionManager.isLoggedIn()) return;

        int ownerId = SessionManager.getCurrentUser().getId();
        List<Announcement> history = announcementService.getOwnerAnnouncements(ownerId);

        if (history.isEmpty()) {
            Label placeholder = new Label("No announcements broadcasted yet.");
            placeholder.setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
            announcementsHistoryContainer.getChildren().add(placeholder);
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        for (Announcement ann : history) {
            VBox card = new VBox(8);
            card.getStyleClass().add("owner-large-card");

            HBox header = new HBox();
            VBox titles = new VBox(2);
            Label titleLbl = new Label(ann.getTitle());
            titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");
            
            Label targetLbl = new Label("Sent to: " + ann.getPgName() + " • Recipients: All Active Tenants");
            targetLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
            titles.getChildren().addAll(titleLbl, targetLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label dateLbl = new Label(ann.getCreatedAt().format(formatter));
            dateLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
            header.getChildren().addAll(titles, spacer, dateLbl);

            Label bodyLbl = new Label(ann.getMessage());
            bodyLbl.setWrapText(true);
            bodyLbl.setStyle("-fx-text-fill: #4B5563; -fx-font-size: 13px;");

            Separator sep = new Separator();

            HBox actions = new HBox(10);
            actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            Button editBtn = new Button("✏ Edit");
            editBtn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-padding: 5 12; -fx-cursor: hand;");
            editBtn.setOnAction(e -> startEditMode(ann));

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-padding: 5 12; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> handleDelete(ann.getId()));

            actions.getChildren().addAll(editBtn, deleteBtn);
            card.getChildren().addAll(header, bodyLbl, sep, actions);
            announcementsHistoryContainer.getChildren().add(card);
        }
    }

    @FXML
    private void handlePostAnnouncement() {
        String pgName = pgSelect.getValue();
        String title = titleField.getText();
        String message = messageArea.getText();

        if (pgName == null || title == null || title.trim().isEmpty() || message == null || message.trim().isEmpty()) {
            AlertUtil.showError("Input Failure", "Missing Fields", "Please complete all mandatory properties before broadcasting.");
            return;
        }

        int pgId = pgNameToId.get(pgName);

        if (currentEditingId != -1) {
            // Edit Operation
            if (announcementService.updateAnnouncement(currentEditingId, pgId, title, message)) {
                AlertUtil.showInfo("Success", "Announcement Updated", "The selected notice has been altered successfully.");
                handleCancelEdit();
            }
        } else {
            // New Post Operation
            try {
                int ownerId = SessionManager.getCurrentUser().getId();
                announcementService.postAnnouncement(ownerId, pgId, title, message);
                AlertUtil.showInfo("Success", "Broadcast Delivered", "Your announcement has been safely logged and pushed to tenant notification trays.");
                clearInputs();
            } catch (Exception ex) {
                AlertUtil.showError("Error", "Broadcast Failed", ex.getMessage());
            }
        }
        refreshHistoryFeed();
    }

    private void startEditMode(Announcement ann) {
        currentEditingId = ann.getId();
        pgSelect.setValue(ann.getPgName());
        titleField.setText(ann.getTitle());
        messageArea.setText(ann.getMessage());
        submitBtn.setText("Save Changes");
        cancelEditBtn.setVisible(true);
    }

    @FXML
    private void handleCancelEdit() {
        currentEditingId = -1;
        clearInputs();
        submitBtn.setText("Send Broadcast");
        cancelEditBtn.setVisible(false);
    }

    private void handleDelete(int id) {
        if (announcementService.deleteAnnouncement(id)) {
            AlertUtil.showInfo("Success", "Deleted", "Announcement removed successfully.");
            if (currentEditingId == id) {
                handleCancelEdit();
            }
            refreshHistoryFeed();
        }
    }

    private void clearInputs() {
        pgSelect.setValue(null);
        titleField.clear();
        messageArea.clear();
    }

    // Sidebar navigation
    @FXML private void openDashboard() { SceneManager.switchTo("OwnerDashboard.fxml"); }
    @FXML private void openMyPGs() { SceneManager.switchTo("MyPGs.fxml"); }
    @FXML private void openRoomsBeds() {
        if (SelectedPGManager.getSelectedPG() != null) {
            SceneManager.switchTo("RoomsBeds.fxml");
        } else {
            SceneManager.switchTo("MyPGs.fxml");
        }
    }
    @FXML private void openBookingRequests() { SceneManager.switchTo("BookingRequests.fxml"); }
    @FXML private void openTenants() { SceneManager.switchTo("Tenants.fxml"); }
    @FXML private void openChat() { SceneManager.switchTo("Chat.fxml"); }
    @FXML private void openReviews() { SceneManager.switchTo("OwnerReviews.fxml"); }
    @FXML private void openSettings() { SceneManager.switchTo("OwnerSettings.fxml"); }
    @FXML private void handleLogout() { SessionManager.logout(); SceneManager.switchTo("Login.fxml"); }
} 