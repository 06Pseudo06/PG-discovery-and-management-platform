package com.pgfinder.controller;

import com.pgfinder.model.BookingDetail;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.BookingService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

public class MyStayController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final BookingService bookingService = new BookingService();

    @FXML
    private Label profileNameLabel;
    @FXML
    private Label pgNameLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label rentLabel;
    @FXML
    private Label depositLabel;
    @FXML
    private Label moveInLabel;
    @FXML
    private Label ownerNameLabel;
    @FXML
    private Label ownerPhoneLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox stayContentBox;
    @FXML
    private VBox noStayBox;
    @FXML
    private HBox detailsSection;
    @FXML
    private Button cancelBookingBtn;

    private BookingDetail activeStay;

    @FXML
    public void initialize() {
        updateProfileLabel();
        refreshStayDetails();
    }

    private void updateProfileLabel() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
    }

    private void refreshStayDetails() {
        if (!SessionManager.isLoggedIn()) {
            showNoStay("Please log in to view your stay details.");
            return;
        }

        try {
            activeStay = bookingService.getActiveStayForStudent(SessionManager.getCurrentUser().getId());
            if (activeStay == null) {
                showNoStay("You do not have an active stay yet. Browse PGs to book a bed.");
                return;
            }
            showActiveStay(activeStay);
        } catch (BookingException ex) {
            showNoStay(ex.getMessage());
        }
    }

    private void showNoStay(String message) {
        if (stayContentBox != null) {
            stayContentBox.setVisible(false);
            stayContentBox.setManaged(false);
        }
        if (detailsSection != null) {
            detailsSection.setVisible(false);
            detailsSection.setManaged(false);
        }
        if (noStayBox != null) {
            noStayBox.setVisible(true);
            noStayBox.setManaged(true);
            noStayBox.getChildren().clear();
            noStayBox.getChildren().add(new Label(message));
        }
    }

    private void showActiveStay(BookingDetail stay) {
        if (noStayBox != null) {
            noStayBox.setVisible(false);
            noStayBox.setManaged(false);
        }
        if (stayContentBox != null) {
            stayContentBox.setVisible(true);
            stayContentBox.setManaged(true);
        }
        if (detailsSection != null) {
            detailsSection.setVisible(true);
            detailsSection.setManaged(true);
        }
        if (pgNameLabel != null) {
            pgNameLabel.setText(stay.getPgName());
        }
        if (locationLabel != null) {
            locationLabel.setText("📍 " + stay.getPgArea() + ", " + stay.getPgCity()
                    + "  •  Room " + stay.getRoomNumber() + "  •  Bed " + stay.getBedLabel()
                    + " (" + stay.getRoomType() + ")");
        }
        if (rentLabel != null) {
            rentLabel.setText("₹" + String.format("%.0f", stay.getRent()) + "/month");
        }
        if (depositLabel != null) {
            depositLabel.setText("₹" + String.format("%.0f", stay.getDeposit()) + " (Paid)");
        }
        if (moveInLabel != null) {
            moveInLabel.setText(stay.getStartDate().format(DATE_FORMAT));
        }
        if (ownerNameLabel != null) {
            ownerNameLabel.setText(stay.getOwnerName());
        }
        if (ownerPhoneLabel != null) {
            ownerPhoneLabel.setText(stay.getOwnerPhone());
        }
        if (statusLabel != null) {
            statusLabel.setText("ACTIVE CONTRACT");
        }
    }

    @FXML
    private void handleCancelBooking() {
        if (activeStay == null || !SessionManager.isLoggedIn()) {
            return;
        }
        if (!AlertUtil.showConfirmation("Cancel Stay", "Confirm Cancellation",
                "Are you sure you want to cancel your active stay? The bed will become vacant.")) {
            return;
        }
        try {
            bookingService.cancelBooking(SessionManager.getCurrentUser().getId(), activeStay.getBookingId());
            AlertUtil.showInfo("Cancelled", "Stay Cancelled", "Your stay has been cancelled successfully.");
            refreshStayDetails();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Cancellation Failed", "Unable to Cancel", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Cancellation Failed", "Unexpected Error",
                    "Something went wrong while cancelling your stay.");
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
    private void openChat() {
        SceneManager.switchTo("StudentChat.fxml");
    }

    @FXML
    private void openAnnouncements() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
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
