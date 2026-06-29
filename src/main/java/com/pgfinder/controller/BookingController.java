package com.pgfinder.controller;

import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.RoomDAO;
import com.pgfinder.model.Bed;
import com.pgfinder.model.PG;
import com.pgfinder.model.Room;
import com.pgfinder.model.User;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.BookingService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BookingController {

    private final BookingService bookingService = new BookingService();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BedDAO bedDAO = new BedDAO();

    private PG currentPG;
    private final Map<String, Integer> roomKeyToId = new HashMap<>();
    private final Map<String, Integer> bedKeyToId = new HashMap<>();

    @FXML
    private ComboBox<String> roomComboBox;
    @FXML
    private ComboBox<String> bedComboBox;
    @FXML
    private DatePicker moveInDatePicker;
    @FXML
    private TextArea additionalNotes;
    @FXML
    private Label monthlyRentLabel;
    @FXML
    private Label depositLabel;
    @FXML
    private Label totalPaymentLabel;
    @FXML
    private Label profileNameLabel;

    @FXML
    public void initialize() {
        currentPG = SelectedPGManager.getSelectedPG();
        if (currentPG == null) {
            Platform.runLater(() -> {
                AlertUtil.showWarning("No PG Selected", "Missing Context", "Please select a PG before booking.");
                SceneManager.switchTo("BrowsePG.fxml");
            });
            return;
        }
        updateProfileLabel();
        loadRooms();
        if (moveInDatePicker != null) {
            moveInDatePicker.setValue(LocalDate.now().plusDays(1));
        }
        if (roomComboBox != null) {
            roomComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> loadBedsForSelectedRoom());
        }
        if (bedComboBox != null) {
            bedComboBox.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> updateSummary());
        }
    }

    private void updateProfileLabel() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
    }

    private void loadRooms() {
        roomKeyToId.clear();
        List<Room> rooms = roomDAO.findByPgId(currentPG.getId());
        List<String> roomOptions = rooms.stream()
                .map(room -> {
                    String key = "Room " + room.getRoomNumber() + " (" + room.getRoomType() + ")";
                    roomKeyToId.put(key, room.getId());
                    return key;
                })
                .collect(Collectors.toList());

        if (roomComboBox != null) {
            roomComboBox.setItems(FXCollections.observableArrayList(roomOptions));
            if (!roomOptions.isEmpty()) {
                roomComboBox.getSelectionModel().selectFirst();
                loadBedsForSelectedRoom();
            }
        }
    }

    private void loadBedsForSelectedRoom() {
        bedKeyToId.clear();
        if (roomComboBox == null || bedComboBox == null) {
            return;
        }
        String selectedRoom = roomComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            bedComboBox.setItems(FXCollections.observableArrayList());
            return;
        }

        Integer roomId = roomKeyToId.get(selectedRoom);
        if (roomId == null) {
            return;
        }

        List<String> bedOptions = bedDAO.findByRoomId(roomId).stream()
                .filter(Bed::isVacant)
                .map(bed -> {
                    String key = "Bed " + bed.getBedLabel() + " (Vacant)";
                    bedKeyToId.put(key, bed.getId());
                    return key;
                })
                .collect(Collectors.toList());

        bedComboBox.setItems(FXCollections.observableArrayList(bedOptions));
        if (!bedOptions.isEmpty()) {
            bedComboBox.getSelectionModel().selectFirst();
        }
        updateSummary();
    }

    private void updateSummary() {
        if (roomComboBox == null || bedComboBox == null) {
            return;
        }
        String selectedRoom = roomComboBox.getSelectionModel().getSelectedItem();
        String selectedBed = bedComboBox.getSelectionModel().getSelectedItem();
        if (selectedRoom == null || selectedBed == null) {
            return;
        }

        Integer roomId = roomKeyToId.get(selectedRoom);
        Integer bedId = bedKeyToId.get(selectedBed);
        if (roomId == null || bedId == null) {
            return;
        }

        Room room = roomDAO.findById(roomId);
        Bed bed = bedDAO.findById(bedId);
        if (room == null || bed == null) {
            return;
        }

        double rent = room.getRent();
        double deposit = bed.getDeposit();
        if (monthlyRentLabel != null) {
            monthlyRentLabel.setText("₹" + String.format("%.0f", rent));
        }
        if (depositLabel != null) {
            depositLabel.setText("₹" + String.format("%.0f", deposit));
        }
        if (totalPaymentLabel != null) {
            totalPaymentLabel.setText("₹" + String.format("%.0f", rent + deposit));
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("PGDetails.fxml");
    }

    @FXML
    private void handleConfirmBooking() {
        if (!SessionManager.isLoggedIn()) {
            AlertUtil.showWarning("Not Logged In", "Session Required", "Please log in to submit a booking request.");
            return;
        }

        User student = SessionManager.getCurrentUser();
        if (!"STUDENT".equalsIgnoreCase(student.getRole())) {
            AlertUtil.showWarning("Invalid Role", "Students Only", "Only students can submit booking requests.");
            return;
        }

        String selectedBed = bedComboBox != null ? bedComboBox.getSelectionModel().getSelectedItem() : null;
        if (selectedBed == null) {
            AlertUtil.showWarning("Bed Required", "Select a Bed", "Please select a vacant bed to continue.");
            return;
        }

        Integer bedId = bedKeyToId.get(selectedBed);
        LocalDate moveInDate = moveInDatePicker != null ? moveInDatePicker.getValue() : null;
        String notes = additionalNotes != null ? additionalNotes.getText().trim() : null;

        try {
            bookingService.submitBooking(student.getId(), bedId, moveInDate, null, notes);
            AlertUtil.showInfo("Request Submitted", "Booking Pending",
                    "Your booking request has been sent to the owner for approval.");
            SceneManager.switchTo("StudentDashboard.fxml");
        } catch (BookingException ex) {
            AlertUtil.showWarning("Booking Failed", "Unable to Submit", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Booking Failed", "Unexpected Error",
                    "Something went wrong while submitting your request. Please try again.");
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
