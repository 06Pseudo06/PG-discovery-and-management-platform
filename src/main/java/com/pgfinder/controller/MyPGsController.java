package com.pgfinder.controller;

import com.pgfinder.dao.BedDAO;
import com.pgfinder.dao.PGDAO;
import com.pgfinder.dao.RoomDAO;
import com.pgfinder.model.Bed;
import com.pgfinder.model.PG;
import com.pgfinder.model.Room;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.List;
import java.util.Optional;

public class MyPGsController {

    private final PGDAO pgDAO = new PGDAO();
    private final RoomDAO roomDAO = new RoomDAO();
    private final BedDAO bedDAO = new BedDAO();

    @FXML
    private Label totalPgsLabel;
    @FXML
    private Label totalRoomsLabel;
    @FXML
    private Label avgOccupancyLabel;

    @FXML
    private VBox pgsContainer;

    @FXML
    public void initialize() {
        refreshDashboard();
    }

    private void refreshDashboard() {
        refreshData();
    }

    private void refreshData() {
        if (pgsContainer == null) return;
        pgsContainer.getChildren().clear();

        if (!SessionManager.isLoggedIn()) {
            return;
        }

        int ownerId = SessionManager.getCurrentUser().getId();
        List<PG> pgs = pgDAO.findByOwnerId(ownerId);

        int totalPgs = pgs.size();
        int totalRooms = 0;
        int totalBeds = 0;
        int occupiedBeds = 0;

        totalPgsLabel.setText(totalPgs + " PG" + (totalPgs == 1 ? "" : "s"));

        for (PG pg : pgs) {
            List<Room> rooms = roomDAO.findByPgId(pg.getId());
            totalRooms += rooms.size();

            int pgBeds = 0;
            int pgOccupied = 0;

            for (Room r : rooms) {
                List<Bed> beds = bedDAO.findByRoomId(r.getId());
                pgBeds += beds.size();
                for (Bed b : beds) {
                    if ("occupied".equalsIgnoreCase(b.getStatus())) {
                        pgOccupied++;
                    }
                }
            }

            totalBeds += pgBeds;
            occupiedBeds += pgOccupied;

            // Render PG card
            VBox card = createPGCard(pg, rooms.size(), pgBeds, pgOccupied);
            pgsContainer.getChildren().add(card);
        }

        totalRoomsLabel.setText(totalRooms + " Room" + (totalRooms == 1 ? "" : "s"));

        if (totalBeds > 0) {
            double rate = (double) occupiedBeds / totalBeds * 100;
            avgOccupancyLabel.setText(String.format("%.1f%%", rate));
        } else {
            avgOccupancyLabel.setText("0%");
        }
    }

    private VBox createPGCard(PG pg, int roomsCount, int bedsCount, int occupiedBeds) {
        VBox card = new VBox(15);
        card.getStyleClass().add("owner-large-card");

        // Header Row (Title and Actions)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label titleLabel = new Label(pg.getName());
        titleLabel.getStyleClass().add("section-title");
        titleLabel.setStyle("-fx-font-size: 18px;");

        String genderPref = pg.getGenderPreference();
        String genderText;
        if (genderPref == null || genderPref.isBlank()) {
            genderText = "Any";
        } else {
            genderText = genderPref.substring(0, 1).toUpperCase() + genderPref.substring(1);
        }
        
        Label subLabel = new Label("📍 " + pg.getArea() + ", " + pg.getCity() + "  •  " + genderText + " Accommodation");
        subLabel.getStyleClass().add("welcome-subtitle");
        subLabel.setStyle("-fx-text-fill: #666666;");

        titleBox.getChildren().addAll(titleLabel, subLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton actionsButton = new MenuButton("Actions");
        actionsButton.getStyleClass().add("card-action-btn");

        MenuItem editItem = new MenuItem("Edit Details");
        editItem.setOnAction(e -> handleEditPG(pg));

        MenuItem manageInvItem = new MenuItem("Manage Inventory");
        manageInvItem.setOnAction(e -> openRoomsBeds(pg));

        MenuItem deleteItem = new MenuItem("Unregister PG");
        deleteItem.setStyle("-fx-text-fill: #DC2626;");
        deleteItem.setOnAction(e -> handleDeletePG(pg));

        actionsButton.getItems().addAll(editItem, manageInvItem, deleteItem);

        Button roomsButton = new Button("View Rooms & Beds");
        roomsButton.getStyleClass().add("primary-button");
        roomsButton.setOnAction(e -> openRoomsBeds(pg));
        HBox.setMargin(roomsButton, new Insets(0, 10, 0, 0));

        header.getChildren().addAll(titleBox, spacer, roomsButton, actionsButton);

        // Body Row (Photo and Info)
        HBox body = new HBox(25);
        body.setAlignment(Pos.CENTER_LEFT);

        StackPane photoPlaceholder = new StackPane();
        photoPlaceholder.setPrefSize(180, 110);
        photoPlaceholder.setStyle("-fx-background-color: #E2ECE8; -fx-background-radius: 10px;");
        Label photoLabel = new Label(pg.getName() + " Photo");
        photoLabel.getStyleClass().add("pg-list-name");
        photoLabel.setStyle("-fx-text-fill: #0F4F46;");
        photoPlaceholder.getChildren().add(photoLabel);

        VBox infoBox = new VBox(10);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        HBox statsRow = new HBox(40);
        statsRow.getChildren().addAll(
            createStatCol("Rooms", roomsCount + " Rooms"),
            createStatCol("Beds Occupied", occupiedBeds + " / " + bedsCount + " Beds"),
            createStatCol("Vacant Space", (bedsCount - occupiedBeds) + " Beds Vacant", true)
        );

        Separator sep = new Separator();

        VBox progressBox = new VBox(5);
        HBox progressHeader = new HBox();
        progressHeader.setAlignment(Pos.CENTER_LEFT);
        Label rateLabel = new Label("Occupancy rate");
        rateLabel.getStyleClass().add("stat-label");
        Region rateSpacer = new Region();
        HBox.setHgrow(rateSpacer, Priority.ALWAYS);

        double ratePercent = bedsCount > 0 ? (double) occupiedBeds / bedsCount : 0.0;
        Label rateValLabel = new Label(String.format("%.1f%%", ratePercent * 100));
        rateValLabel.getStyleClass().addAll("stat-label");
        rateValLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F4F46;");
        progressHeader.getChildren().addAll(rateLabel, rateSpacer, rateValLabel);

        ProgressBar bar = new ProgressBar(ratePercent);
        bar.setPrefWidth(600);
        bar.setStyle("-fx-min-height: 8px;");

        progressBox.getChildren().addAll(progressHeader, bar);
        infoBox.getChildren().addAll(statsRow, sep, progressBox);
        body.getChildren().addAll(photoPlaceholder, infoBox);

        // Append to Card VBox
        card.getChildren().addAll(header, body);
        return card;
    }

    private VBox createStatCol(String title, String val) {
        return createStatCol(title, val, false);
    }

    private VBox createStatCol(String title, String val, boolean highlightGreen) {
        VBox col = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-label");
        Label valLbl = new Label(val);
        valLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        if (highlightGreen) {
            valLbl.setStyle(valLbl.getStyle() + " -fx-text-fill: #16A34A;");
        }
        col.getChildren().addAll(titleLbl, valLbl);
        return col;
    }

    @FXML
    private void handleRegisterNewPG() {
        showPGDialog(null).ifPresent(newPg -> {
            try {
                pgDAO.insert(newPg);
                refreshDashboard();
                AlertUtil.showInfo("Success", "PG Registered", "PG property '" + newPg.getName() + "' registered successfully.");
            } catch (Exception ex) {
                AlertUtil.showError("Error", "Registration Failed", ex.getMessage());
            }
        });
    }

    private void handleEditPG(PG pg) {
        showPGDialog(pg).ifPresent(updatedPg -> {
            try {
                pgDAO.update(updatedPg);
                refreshDashboard();
                AlertUtil.showInfo("Success", "PG Updated", "PG details saved successfully.");
            } catch (Exception ex) {
                AlertUtil.showError("Error", "Update Failed", ex.getMessage());
            }
        });
    }

    private void handleDeletePG(PG pg) {
        boolean confirm = AlertUtil.showConfirmation(
            "Delete PG Building",
            "Are you sure?",
            "Deleting '" + pg.getName() + "' will cascade and delete all associated rooms and beds. This action is irreversible!"
        );
        if (confirm) {
            try {
                pgDAO.delete(pg.getId());
                refreshDashboard();
                AlertUtil.showInfo("Deleted", "PG Building Removed", "'" + pg.getName() + "' was successfully unregistered.");
            } catch (Exception ex) {
                AlertUtil.showError("Delete Error", "Action failed", ex.getMessage());
            }
        }
    }

    private Optional<PG> showPGDialog(PG existing) {
        Dialog<PG> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Register New PG" : "Edit PG Details");
        dialog.setHeaderText("Please provide the PG details below.");

        ButtonType saveButtonType = new ButtonType("Save Details", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField nameField = new TextField();
        nameField.setPromptText("Oxford PG");
        nameField.setPrefWidth(250);

        TextField addressField = new TextField();
        addressField.setPromptText("Symbiosis Road, Viman Nagar");

        TextField cityField = new TextField();
        cityField.setPromptText("Pune");

        TextField areaField = new TextField();
        areaField.setPromptText("Viman Nagar");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Detailed information about amenities, timings, location.");
        descriptionField.setPrefRowCount(3);

        ComboBox<String> genderField = new ComboBox<>();
        genderField.getItems().addAll("male", "female", "any");
        genderField.setValue("any");

        CheckBox foodField = new CheckBox("Food Included");
        CheckBox wifiField = new CheckBox("WiFi Included");
        CheckBox acField = new CheckBox("AC Available");
        CheckBox laundryField = new CheckBox("Laundry Included");
        CheckBox gymField = new CheckBox("Gym Available");
        CheckBox parkingField = new CheckBox("Parking Available");

        grid.add(new Label("PG Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("City:"), 0, 2);
        grid.add(cityField, 1, 2);
        grid.add(new Label("Area:"), 0, 3);
        grid.add(areaField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);
        grid.add(new Label("Gender Restriction:"), 0, 5);
        grid.add(genderField, 1, 5);

        VBox amenitiesCol1 = new VBox(8, foodField, wifiField, acField);
        VBox amenitiesCol2 = new VBox(8, laundryField, gymField, parkingField);
        HBox amenitiesBox = new HBox(25, amenitiesCol1, amenitiesCol2);

        grid.add(new Label("Amenities:"), 0, 6);
        grid.add(amenitiesBox, 1, 6);

        // Prepopulate if editing
        if (existing != null) {
            nameField.setText(existing.getName());
            addressField.setText(existing.getAddress());
            cityField.setText(existing.getCity());
            areaField.setText(existing.getArea());
            descriptionField.setText(existing.getDescription());
            genderField.setValue(existing.getGenderPreference());
            foodField.setSelected(existing.isFoodAvailable());
            wifiField.setSelected(existing.isWifiAvailable());
            acField.setSelected(existing.isAcAvailable());
            laundryField.setSelected(existing.isLaundryAvailable());
            gymField.setSelected(existing.isGymAvailable());
            parkingField.setSelected(existing.isParkingAvailable());
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText();
                String address = addressField.getText();
                String city = cityField.getText();
                String area = areaField.getText();

                if (name == null || name.trim().isEmpty() ||
                    address == null || address.trim().isEmpty() ||
                    city == null || city.trim().isEmpty() ||
                    area == null || area.trim().isEmpty()) {
                    return null;
                }

                int id = existing == null ? 0 : existing.getId();
                int ownerId = existing == null ? SessionManager.getCurrentUser().getId() : existing.getOwnerId();

                return new PG(
                    id, ownerId, name.trim(), address.trim(), city.trim(), area.trim(),
                    descriptionField.getText() == null ? "" : descriptionField.getText().trim(),
                    genderField.getValue(), foodField.isSelected(), wifiField.isSelected(),
                    acField.isSelected(), laundryField.isSelected(), gymField.isSelected(), parkingField.isSelected()
                );
            }
            return null;
        });

        return dialog.showAndWait();
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    public void openDashboard() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    public void openRoomsBeds(PG pg) {
        SelectedPGManager.setSelectedPG(pg);
        SceneManager.switchTo("RoomsBeds.fxml");
    }

    @FXML
    public void openRoomsBeds() {
        if (SelectedPGManager.getSelectedPG() != null) {
            SceneManager.switchTo("RoomsBeds.fxml");
        } else {
            AlertUtil.showWarning("Context Missing", "No PG Selected", 
                "Please select a specific PG property from your list first to view its detailed inventory.");
            SceneManager.switchTo("MyPGs.fxml");
        }
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
    public void openReviews() {
        SceneManager.switchTo("OwnerReviews.fxml");
    }

    @FXML
    public void openChat() {
        SceneManager.switchTo("Chat.fxml");
    }

    @FXML
    public void openReports() {
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon", "The reports dashboard and analytics module will be available in the next system update.");
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

