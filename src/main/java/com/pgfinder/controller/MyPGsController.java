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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.io.File;

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
    private ImageView ownerProfileImage;
    @FXML
    private Label ownerNameLabel;
    @FXML
    private Label ownerEmailLabel;


    @FXML
    public void initialize() {

        initializeHeader();
        refreshDashboard();
    }

    private void refreshDashboard() {
        refreshData();

    }

    private void initializeHeader() {

        if (!SessionManager.isLoggedIn()) {
            ownerNameLabel.setText("Guest");
            ownerEmailLabel.setText("");
            return;
        }

        var owner = SessionManager.getCurrentUser();
        ownerNameLabel.setText(owner.getName());
        ownerEmailLabel.setText("Property Owner");

        String imagePath = owner.getProfileImagePath();

        if (imagePath != null && !imagePath.isBlank()) {
            File file = new File(imagePath);

            if (file.exists()) {
                Circle clip = new Circle(36,36,36);
                ownerProfileImage.setClip(clip);
                ownerProfileImage.setFitWidth(72);
                ownerProfileImage.setFitHeight(72);
                ownerProfileImage.setPreserveRatio(false);
                ownerProfileImage.setImage(
                        new Image(file.toURI().toString())
                );
            }
        }
    }

    private Label createAmenityChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("amenity-chip");
        return chip;
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
            // Render PG card
            HBox card = createPGCard(pg, rooms.size(), pgBeds, pgOccupied);
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

    // Note: Change the return type from VBox to HBox!
private HBox createPGCard(PG pg, int roomsCount, int bedsCount, int occupiedBeds) {

    HBox card = new HBox(25);
    card.getStyleClass().add("pg-card");
    card.setPadding(new Insets(24));
    card.setAlignment(Pos.CENTER_LEFT);

    // =========================
    // Left Image
    // =========================

    StackPane photoPlaceholder = new StackPane();
    photoPlaceholder.setPrefSize(240, 250);
    photoPlaceholder.getStyleClass().add("pg-image-placeholder");

    Label icon = new Label("🏠");
    icon.getStyleClass().add("pg-image-icon");

    photoPlaceholder.getChildren().add(icon);

    // =========================
    // Right Section
    // =========================

    VBox rightSide = new VBox(18);
    rightSide.getStyleClass().add("pg-details-container");

    HBox.setHgrow(rightSide, Priority.ALWAYS);
    rightSide.setMaxWidth(Double.MAX_VALUE);

    // ---------- Header ----------

    HBox header = new HBox(12);
    header.setAlignment(Pos.CENTER_LEFT);

    Label titleLabel = new Label(pg.getName());
    titleLabel.getStyleClass().add("pg-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button roomsButton = new Button("🛏 View Rooms & Beds");
    roomsButton.getStyleClass().add("secondary-button");
    roomsButton.setOnAction(e -> openRoomsBeds(pg));
    roomsButton.getStyleClass().add("secondary-btn");

    MenuButton actionsButton = new MenuButton("Manage");
    actionsButton.getStyleClass().add("primary-btn");

    MenuItem editItem = new MenuItem("Edit Details");
    editItem.setOnAction(e -> handleEditPG(pg));

    MenuItem deleteItem = new MenuItem("Unregister PG");
    deleteItem.setOnAction(e -> handleDeletePG(pg));

    actionsButton.getItems().addAll(editItem, deleteItem);

    header.getChildren().addAll(
            titleLabel,
            spacer,
            roomsButton,
            actionsButton
    );

    HBox.setMargin(roomsButton, new Insets(0, 12, 0, 0));

    // ---------- Subtitle ----------

    String genderPref =
            (pg.getGenderPreference() == null || pg.getGenderPreference().isBlank())
                    ? "Any"
                    : pg.getGenderPreference();

    Label subLabel = new Label(
            "📍 " + pg.getArea() + ", " +
            pg.getCity() +
            " • " +
            genderPref +
            " Accommodation"
    );

    subLabel.getStyleClass().add("pg-subtitle");

    // ---------- Stats ----------

    HBox statsRow = new HBox(40);
    statsRow.getStyleClass().add("stats-row");

    statsRow.getChildren().addAll(

            createStatCol(
                    "Rooms",
                    "🚪",
                    String.valueOf(roomsCount),
                    "Rooms",
                    false
            ),

            createStatCol(
                    "Beds Occupied",
                    "🛏",
                    occupiedBeds + " / " + bedsCount,
                    "Beds",
                    true
            ),

            createStatCol(
                    "Vacant Space",
                    "🪑",
                    String.valueOf(bedsCount - occupiedBeds),
                    "Beds Vacant",
                    true
            )
    );

    // ---------- Occupancy ----------

    VBox progressBox = new VBox(8);

    HBox progressHeader = new HBox();

    Label rateLabel = new Label("Occupancy Rate");
    rateLabel.getStyleClass().add("occupancy-label");

    Region rateSpacer = new Region();
    HBox.setHgrow(rateSpacer, Priority.ALWAYS);

    double ratePercent =
            bedsCount == 0
                    ? 0
                    : (double) occupiedBeds / bedsCount;

    Label rateValue = new Label(
            String.format("%.1f%%", ratePercent * 100)
    );

    rateValue.getStyleClass().add("occupancy-value");

    progressHeader.getChildren().addAll(
            rateLabel,
            rateSpacer,
            rateValue
    );

    ProgressBar progressBar = new ProgressBar(ratePercent);
    progressBar.setMaxWidth(Double.MAX_VALUE);
    progressBar.getStyleClass().add("occupancy-progress");

    progressBox.getChildren().addAll(
            progressHeader,
            progressBar
    );

    // ---------- Amenities ----------

    HBox amenities = new HBox(10);

    if (pg.isWifiAvailable())
        amenities.getChildren().add(createAmenityChip("📶 WiFi"));

    if (pg.isFoodAvailable())
        amenities.getChildren().add(createAmenityChip("🍽 Food"));

    if (pg.isParkingAvailable())
        amenities.getChildren().add(createAmenityChip("🚗 Parking"));

    rightSide.getChildren().addAll(
            header,
            subLabel,
            statsRow,
            progressBox,
            amenities
    );

    card.getChildren().addAll(
            photoPlaceholder,
            rightSide
    );

    return card;
}

private HBox createStatCol(
        String title,
        String icon,
        String value,
        String unit,
        boolean highlight
) {

    HBox container = new HBox(12);
    container.setAlignment(Pos.CENTER_LEFT);

    StackPane iconPane = new StackPane();

    iconPane.getStyleClass().add("stat-icon-circle");

    Label iconLabel = new Label(icon);
    iconLabel.getStyleClass().add("stat-icon");

    iconPane.getChildren().add(iconLabel);

    VBox text = new VBox(2);

    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("stat-title");

    HBox valueRow = new HBox(4);
    valueRow.setAlignment(Pos.BOTTOM_LEFT);

    Label valueLabel = new Label(value);

    valueLabel.getStyleClass().add(
            highlight
                    ? "stat-value-green"
                    : "stat-value"
    );

    Label unitLabel = new Label(unit);
    unitLabel.getStyleClass().add("stat-unit");

    valueRow.getChildren().addAll(
            valueLabel,
            unitLabel
    );

    text.getChildren().addAll(
            titleLabel,
            valueRow
    );

    container.getChildren().addAll(
            iconPane,
            text
    );

    return container;
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

