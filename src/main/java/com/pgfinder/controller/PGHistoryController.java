package com.pgfinder.controller;

import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.BookingStatus;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.BookingService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PGHistoryController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final BookingService bookingService = new BookingService();

    @FXML
    private Label profileNameLabel;
    @FXML
    private VBox timelineContainer;

    @FXML
    public void initialize() {
        updateProfileLabel();
        refreshHistory();
    }

    private void updateProfileLabel() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
    }

    private void refreshHistory() {
        if (timelineContainer == null) {
            return;
        }
        timelineContainer.getChildren().clear();

        if (!SessionManager.isLoggedIn()) {
            timelineContainer.getChildren().add(new Label("Please log in to view your stay history."));
            return;
        }

        try {
            List<BookingDetail> history = bookingService.getStudentBookingHistory(
                    SessionManager.getCurrentUser().getId());

            if (history.isEmpty()) {
                timelineContainer.getChildren().add(new Label("No stay history yet. Browse PGs to get started."));
                return;
            }

            for (BookingDetail detail : history) {
                timelineContainer.getChildren().add(buildTimelineEntry(detail));
            }
        } catch (BookingException ex) {
            timelineContainer.getChildren().add(new Label(ex.getMessage()));
        }
    }

    private HBox buildTimelineEntry(BookingDetail detail) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        VBox nodeColumn = new VBox(5);
        nodeColumn.setAlignment(Pos.TOP_CENTER);
        StackPane node = new StackPane();
        node.getStyleClass().add("timeline-node");
        if (BookingStatus.CONFIRMED.equalsIgnoreCase(detail.getStatus())) {
            node.setStyle("-fx-background-color: #2ECC71;");
        } else if (BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(detail.getStatus())) {
            node.setStyle("-fx-background-color: #F59E0B;");
        }
        Region line = new Region();
        line.setPrefHeight(130);
        line.getStyleClass().add("timeline-line");
        nodeColumn.getChildren().addAll(node, line);

        VBox card = new VBox(10);
        card.getStyleClass().add("timeline-card");
        HBox.setHgrow(card, javafx.scene.layout.Priority.ALWAYS);
        if (BookingStatus.CONFIRMED.equalsIgnoreCase(detail.getStatus())) {
            card.setStyle("-fx-border-color: #2ECC71; -fx-border-width: 1.5px;");
        } else if (BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(detail.getStatus())) {
            card.setStyle("-fx-border-color: #F59E0B; -fx-border-width: 1.5px;");
        }

        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label pgName = new Label(detail.getPgName());
        pgName.getStyleClass().add("pg-name");
        pgName.setStyle("-fx-font-size: 20px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label statusTag = new Label(formatStatusLabel(detail.getStatus()));
        statusTag.getStyleClass().add("status-tag");
        titleRow.getChildren().addAll(pgName, spacer, statusTag);

        Label location = new Label("📍 " + detail.getPgArea() + ", " + detail.getPgCity()
                + "  •  Room " + detail.getRoomNumber() + ", Bed " + detail.getBedLabel());
        location.getStyleClass().add("sub-heading");

        HBox metaRow = new HBox(30);
        VBox durationBox = new VBox(2);
        durationBox.getChildren().addAll(
                new Label("Move-in"),
                new Label(detail.getStartDate().format(DATE_FORMAT))
        );
        VBox ownerBox = new VBox(2);
        ownerBox.getChildren().addAll(
                new Label("Owner"),
                new Label(detail.getOwnerName())
        );
        metaRow.getChildren().addAll(durationBox, ownerBox);

        HBox actions = new HBox();
        actions.setAlignment(Pos.CENTER_RIGHT);
        if (BookingStatus.CONFIRMED.equalsIgnoreCase(detail.getStatus())) {
            Button viewBtn = new Button("View Active Details");
            viewBtn.getStyleClass().add("btn-primary");
            viewBtn.setOnAction(e -> SceneManager.switchTo("MyStay.fxml"));
            actions.getChildren().add(viewBtn);
        } else if (BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(detail.getStatus())) {
            Button confirmBtn = new Button("Confirm Booking");
            confirmBtn.getStyleClass().add("btn-primary");
            confirmBtn.setOnAction(e -> confirmBooking(detail));
            Button cancelBtn = new Button("Decline");
            cancelBtn.getStyleClass().add("btn-secondary");
            cancelBtn.setOnAction(e -> cancelPending(detail));
            actions.getChildren().addAll(confirmBtn, cancelBtn);
        } else if (BookingStatus.PENDING_OWNER.equalsIgnoreCase(detail.getStatus())) {
            Button cancelBtn = new Button("Cancel Request");
            cancelBtn.getStyleClass().add("btn-secondary");
            cancelBtn.setOnAction(e -> cancelPending(detail));
            actions.getChildren().add(cancelBtn);
        }

        card.getChildren().addAll(titleRow, location, metaRow, new Separator(), actions);
        row.getChildren().addAll(nodeColumn, card);
        return row;
    }

    private String formatStatusLabel(String status) {
        if (BookingStatus.CONFIRMED.equalsIgnoreCase(status)) {
            return "CONFIRMED";
        }
        if (BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(status)) {
            return "AWAITING CONFIRMATION";
        }
        if (BookingStatus.PENDING_OWNER.equalsIgnoreCase(status)) {
            return "PENDING OWNER";
        }
        if (BookingStatus.REJECTED.equalsIgnoreCase(status)) {
            return "REJECTED";
        }
        if (BookingStatus.CANCELLED.equalsIgnoreCase(status)) {
            return "CANCELLED";
        }
        return status.toUpperCase();
    }

    private void confirmBooking(BookingDetail detail) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        if (!AlertUtil.showConfirmation("Confirm Booking", "Finalize Stay",
                "Confirm your booking at " + detail.getPgName() + "? The bed will be reserved for you.")) {
            return;
        }
        try {
            bookingService.confirmBooking(SessionManager.getCurrentUser().getId(), detail.getBookingId());
            AlertUtil.showInfo("Confirmed", "Booking Confirmed",
                    "Your stay is confirmed. View details in My Stay.");
            refreshHistory();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Confirmation Failed", "Unable to Confirm", ex.getMessage());
        }
    }

    private void cancelPending(BookingDetail detail) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        if (!AlertUtil.showConfirmation("Cancel Request", "Confirm Cancellation",
                "Cancel your pending booking request for " + detail.getPgName() + "?")) {
            return;
        }
        try {
            bookingService.cancelBooking(SessionManager.getCurrentUser().getId(), detail.getBookingId());
            AlertUtil.showInfo("Cancelled", "Request Cancelled", "Your booking request has been cancelled.");
            refreshHistory();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Cancellation Failed", "Unable to Cancel", ex.getMessage());
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
    private void openAnnouncements() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
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
