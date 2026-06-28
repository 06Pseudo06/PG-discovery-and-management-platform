package com.pgfinder.controller;

import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.RoomDAO;
import com.pgfinder.model.Bed;
import com.pgfinder.model.PG;
import com.pgfinder.model.Room;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

public class RoomsBedsController {

    /* ==========================================
                DAO OBJECTS
       ========================================== */

    private final RoomDAO roomDAO = new RoomDAO();
    private final BedDAO bedDAO = new BedDAO();

    /* ==========================================
                CURRENT PG
       ========================================== */

    private PG selectedPG;

    /* ==========================================
                FXML COMPONENTS
       ========================================== */

    @FXML
    private Label pgTitleLabel;

    @FXML
    private Label pgLocationLabel;

    @FXML
    private Label totalBedsLabel;

    @FXML
    private Label occupiedBedsLabel;

    @FXML
    private Label vacantBedsLabel;

    @FXML
    private VBox roomsContainer;

    @FXML
    private Button createRoomButton;

    /* ==========================================
                INITIALIZATION
       ========================================== */

    @FXML
    public void initialize() {

        selectedPG = SelectedPGManager.getSelectedPG();

        if (selectedPG == null) {

            SceneManager.switchTo("MyPGs.fxml");
            return;

        }

        loadPGInformation();

        loadStatistics();

    }

    /* ==========================================
                LOAD PG INFO
       ========================================== */

    private void loadPGInformation() {

        pgTitleLabel.setText(selectedPG.getName() + " - Rooms & Beds");

        pgLocationLabel.setText(
                selectedPG.getArea() + ", " + selectedPG.getCity()
        );

    }

    /* ==========================================
                LOAD STATS
       ========================================== */

    private void loadStatistics() {

        List<Room> rooms =
                roomDAO.findByPgId(selectedPG.getId());

        int totalBeds = 0;

        int occupiedBeds = 0;

        for (Room room : rooms) {

            List<Bed> beds =
                    bedDAO.findByRoomId(room.getId());

            totalBeds += beds.size();

            for (Bed bed : beds) {

                if (bed.isOccupied()) {

                    occupiedBeds++;

                }

            }

        }

        int vacantBeds = totalBeds - occupiedBeds;

        totalBedsLabel.setText(totalBeds + " Beds");

        occupiedBedsLabel.setText(
                occupiedBeds + " Beds"
        );

        vacantBedsLabel.setText(
                vacantBeds + " Beds"
        );

    }

    /* ==========================================
                ROOM ACTIONS
       ========================================== */

    @FXML
    private void handleCreateRoom() {

        // Batch 4

    }

    /* ==========================================
                NAVIGATION
       ========================================== */

    @FXML
    public void goBack() {

        SelectedPGManager.clear();

        SceneManager.switchTo("MyPGs.fxml");

    }

    @FXML
    private void openDashboard() {

        SelectedPGManager.clear();

        SceneManager.switchTo("OwnerDashboard.fxml");

    }

    @FXML
    private void openMyPGs() {

        SelectedPGManager.clear();

        SceneManager.switchTo("MyPGs.fxml");

    }

    @FXML
    private void openBookingRequests() {

        SceneManager.switchTo("BookingRequests.fxml");

    }

    @FXML
    private void openTenants() {

        SceneManager.switchTo("Tenants.fxml");

    }

    @FXML
    private void openAnnouncements() {

        SceneManager.switchTo("Announcements.fxml");

    }

    @FXML
    private void openReviews() {

        SceneManager.switchTo("OwnerReviews.fxml");

    }

    @FXML
    private void openChat() {

        SceneManager.switchTo("Chat.fxml");

    }

    @FXML
    private void handleLogout() {

        SessionManager.logout();

        SelectedPGManager.clear();

        SceneManager.switchTo("Login.fxml");

    }

}