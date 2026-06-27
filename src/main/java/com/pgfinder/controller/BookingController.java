package com.pgfinder.controller;

import com.pgfinder.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

import java.time.LocalDate;

public class BookingController {

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private ComboBox<String> bedComboBox;

    @FXML
    private DatePicker moveInDatePicker;

    @FXML
    private TextArea additionalNotes;

    @FXML
    public void initialize() {
        if (roomComboBox != null) {
            roomComboBox.setItems(FXCollections.observableArrayList(
                "Room A1 (Double Sharing)",
                "Room A2 (Triple Sharing)"
            ));
            roomComboBox.setValue("Room A1 (Double Sharing)");
        }
        if (bedComboBox != null) {
            bedComboBox.setItems(FXCollections.observableArrayList(
                "Bed B1 (Occupied by Rahul)",
                "Bed B2 (Vacant - Selected)"
            ));
            bedComboBox.setValue("Bed B2 (Vacant - Selected)");
        }
        if (moveInDatePicker != null) {
            moveInDatePicker.setValue(LocalDate.now().plusDays(5));
        }
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("PGDetails.fxml");
    }

    @FXML
    public void goBack(ActionEvent event) {
        goBack();
    }

    @FXML
    private void handleConfirmBooking() {
        // Confirm reservation logic placeholder
        SceneManager.switchTo("StudentDashboard.fxml");
    }

    // Sidebar navigation actions
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
