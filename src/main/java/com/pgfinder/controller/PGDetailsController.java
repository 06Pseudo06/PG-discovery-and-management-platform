package com.pgfinder.controller;

import com.pgfinder.dao.RoomDAO;
import com.pgfinder.model.PG;
import com.pgfinder.model.Room;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.BookingRefreshHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

public class PGDetailsController {

    private final RoomDAO roomDAO = new RoomDAO();
    private PG currentPG;

    @FXML
    private Label pgTitleLabel;
    @FXML
    private Label pgLocationLabel;
    @FXML
    private Label pgDescriptionLabel;
    @FXML
    private Label rentFromLabel;
    @FXML
    private Label vacantBedsLabel;
    @FXML
    private Label profileNameLabel;

    @FXML
    public void initialize() {
        currentPG = SelectedPGManager.getSelectedPG();
        if (currentPG == null) {
            Platform.runLater(() -> {
                AlertUtil.showWarning("No PG Selected", "Missing Context", "Please browse and select a PG first.");
                SceneManager.switchTo("BrowsePG.fxml");
            });
            return;
        }
        updateProfileLabel();
        refreshDetails();
    }

    private void updateProfileLabel() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
    }

    private void refreshDetails() {
        if (pgTitleLabel != null) {
            pgTitleLabel.setText(currentPG.getName());
        }
        if (pgLocationLabel != null) {
            pgLocationLabel.setText("📍 " + currentPG.getAddress() + ", " + currentPG.getArea()
                    + ", " + currentPG.getCity() + "  •  " + formatGender(currentPG.getGenderPreference()));
        }
        if (pgDescriptionLabel != null) {
            pgDescriptionLabel.setText(currentPG.getDescription() != null ? currentPG.getDescription() : "");
        }

        List<Room> rooms = roomDAO.findByPgId(currentPG.getId());
        double minRent = rooms.stream().mapToDouble(Room::getRent).min().orElse(0);
        if (rentFromLabel != null) {
            rentFromLabel.setText("From ₹" + String.format("%.0f", minRent) + "/month");
        }
        int vacantBeds = BookingRefreshHelper.getVacantBedCountForPg(currentPG.getId());
        if (vacantBedsLabel != null) {
            vacantBedsLabel.setText(vacantBeds + " vacant bed" + (vacantBeds == 1 ? "" : "s") + " available");
        }
    }

    private String formatGender(String preference) {
        if (preference == null) {
            return "Co-ed PG";
        }
        return switch (preference.toLowerCase()) {
            case "male" -> "Boys PG";
            case "female" -> "Girls PG";
            default -> "Co-ed PG";
        };
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("BrowsePG.fxml");
    }

    @FXML
    public void openBooking() {
        if (currentPG == null) {
            AlertUtil.showWarning("No PG Selected", "Missing Context", "Please select a PG first.");
            return;
        }
        if (BookingRefreshHelper.getVacantBedCountForPg(currentPG.getId()) == 0) {
            AlertUtil.showWarning("No Availability", "Fully Occupied",
                    "This PG has no vacant beds at the moment.");
            return;
        }
        SceneManager.switchTo("Booking.fxml");
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
