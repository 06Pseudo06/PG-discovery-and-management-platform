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

public class RoomsBedsController {

    private final RoomDAO roomDAO = new RoomDAO();
    private final BedDAO bedDAO = new BedDAO();
    private PG currentPG;

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

    @FXML
    public void initialize() {
        currentPG = SelectedPGManager.getSelectedPG();
        if (currentPG == null) {
            AlertUtil.showWarning("No PG Selected", "Missing Context", "Redirecting back to your properties list.");
            goBack();
            return;
        }

        pgTitleLabel.setText(currentPG.getName() + " - Inventory");
        pgLocationLabel.setText("📍 " + currentPG.getAddress() + ", " + currentPG.getArea() + ", " + currentPG.getCity());
        
        refreshData();
    }

    // =========================================================================
    // REFRESH ENGINE
    // =========================================================================
    private void refreshData() {
        try {
            if (currentPG == null || roomsContainer == null) return;
            
            roomsContainer.getChildren().clear();
            List<Room> rooms = roomDAO.findByPgId(currentPG.getId());
            
            int totalBeds = 0;
            int occupiedBeds = 0;

            if (rooms.isEmpty()) {
                Label emptyLabel = new Label("No rooms added to this property yet. Click '+ Create Room' to get started.");
                emptyLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 14px; -fx-font-style: italic;");
                roomsContainer.getChildren().add(emptyLabel);
            } else {
                for (Room room : rooms) {
                    List<Bed> beds = bedDAO.findByRoomId(room.getId());
                    int roomBedsCount = beds.size();
                    int roomOccupiedCount = 0;

                    for (Bed bed : beds) {
                        if ("occupied".equalsIgnoreCase(bed.getStatus())) {
                            roomOccupiedCount++;
                        }
                    }

                    totalBeds += roomBedsCount;
                    occupiedBeds += roomOccupiedCount;

                    VBox roomCard = createRoomCard(room, beds, roomBedsCount, roomOccupiedCount);
                    roomsContainer.getChildren().add(roomCard);
                }
            }

            int vacantBeds = totalBeds - occupiedBeds;
            totalBedsLabel.setText(totalBeds + " Bed" + (totalBeds == 1 ? "" : "s"));
            occupiedBedsLabel.setText(occupiedBeds + " Bed" + (occupiedBeds == 1 ? "" : "s"));
            vacantBedsLabel.setText(vacantBeds + " Space" + (vacantBeds == 1 ? "" : "s"));

        } catch (Exception ex) {
            AlertUtil.showError("Database Error", "Failed to reload inventory data", ex.getMessage());
        }
    }

    // =========================================================================
    // UI RENDERING BUILDERS
    // =========================================================================
    private VBox createRoomCard(Room room, List<Bed> beds, int totalBeds, int occupiedBeds) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 12px; -fx-padding: 20px; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        // Header Row
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(3);
        Label roomTitle = new Label("Room " + room.getRoomNumber());
        roomTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #1A202C;");
        Label typeLabel = new Label(room.getRoomType() + "  •  ₹" + String.format("%.0f", room.getRent()) + "/mo");
        typeLabel.setStyle("-fx-text-fill: #718096; -fx-font-size: 13px;");
        titleBox.getChildren().addAll(roomTitle, typeLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Control Actions
        Button addBedBtn = new Button("+ Add Bed");
        addBedBtn.setStyle("-fx-background-color: #EEF7F4; -fx-text-fill: #0F4F46; -fx-font-weight: bold; -fx-background-radius: 6px;");
        addBedBtn.setOnAction(e -> handleAddBed(room));

        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: #E2E8F0; -fx-text-fill: #4A5568; -fx-background-radius: 6px;");
        editBtn.setOnAction(e -> handleEditRoom(room));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 6px;");
        deleteBtn.setOnAction(e -> handleDeleteRoom(room));

        HBox actionBox = new HBox(10, addBedBtn, editBtn, deleteBtn);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(titleBox, spacer, actionBox);

        // Bed Inventory Flow layout
        FlowPane bedsContainer = new FlowPane();
        bedsContainer.setHgap(12);
        bedsContainer.setVgap(12);
        bedsContainer.setPadding(new Insets(10, 0, 0, 0));

        if (beds.isEmpty()) {
            Label noBeds = new Label("No physical beds provisioned inside this room layout.");
            noBeds.setStyle("-fx-text-fill: #A0AEC0; -fx-font-size: 12px; -fx-font-style: italic;");
            bedsContainer.getChildren().add(noBeds);
        } else {
            for (Bed bed : beds) {
                HBox bedCard = createBedCard(bed);
                bedsContainer.getChildren().add(bedCard);
            }
        }

        card.getChildren().addAll(header, new Separator(), bedsContainer);
        return card;
    }

    private HBox createBedCard(Bed bed) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        
        boolean isOccupied = "occupied".equalsIgnoreCase(bed.getStatus());
        String statusColor = isOccupied ? "#EF4444" : "#22C55E";
        String statusBg = isOccupied ? "#FEE2E2" : "#DCFCE7";

        card.setStyle("-fx-background-color: " + statusBg + "; -fx-background-radius: 8px; " +
                      "-fx-padding: 8px 12px; -fx-border-color: " + statusColor + "; -fx-border-radius: 8px; -fx-border-width: 1px;");

        VBox metaBox = new VBox(2);
        Label lbl = new Label("🛏 " + bed.getBedLabel());
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #1A202C; -fx-font-size: 13px;");
        Label dep = new Label("Dep: ₹" + String.format("%.0f", bed.getDeposit()));
        dep.setStyle("-fx-text-fill: #4A5568; -fx-font-size: 11px;");
        metaBox.getChildren().addAll(lbl, dep);

        ContextMenu menu = new ContextMenu();
        MenuItem editBed = new MenuItem("Modify Layout");
        editBed.setOnAction(e -> handleEditBed(bed));
        MenuItem deleteBed = new MenuItem("Remove Bed");
        deleteBed.setStyle("-fx-text-fill: #DC2626;");
        deleteBed.setOnAction(e -> handleDeleteBed(bed));
        menu.getItems().addAll(editBed, deleteBed);

        Button menuBtn = new Button("⋮");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #4A5568; -fx-font-weight: bold; -fx-padding: 0 4 0 4;");
        menuBtn.setOnMouseClicked(e -> menu.show(menuBtn, e.getScreenX(), e.getScreenY()));

        card.getChildren().addAll(metaBox, menuBtn);
        return card;
    }

    // =========================================================================
    // ROOM CRUD BUSINESS LOGIC
    // =========================================================================
    @FXML
    private void handleCreateRoom() {
        showRoomDialog(null).ifPresent(room -> {
            try {
                // Check local duplicate rule boundaries
                List<Room> existing = roomDAO.findByPgId(currentPG.getId());
                for (Room r : existing) {
                    if (r.getRoomNumber().trim().equalsIgnoreCase(room.getRoomNumber().trim())) {
                        AlertUtil.showError("Validation Error", "Duplicate Room Identification", 
                            "Room '" + room.getRoomNumber() + "' already operates inside this target property asset.");
                        return;
                    }
                }
                roomDAO.insert(room);
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Transaction Fault", "Failed to preserve room entity", ex.getMessage());
            }
        });
    }

    private void handleEditRoom(Room room) {
        showRoomDialog(room).ifPresent(updated -> {
            try {
                List<Room> existing = roomDAO.findByPgId(currentPG.getId());
                for (Room r : existing) {
                    if (r.getId() != updated.getId() && r.getRoomNumber().trim().equalsIgnoreCase(updated.getRoomNumber().trim())) {
                        AlertUtil.showError("Validation Error", "Duplicate Identity Conflict", 
                            "Another structural deployment already occupies room tag: " + updated.getRoomNumber());
                        return;
                    }
                }
                roomDAO.update(updated);
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Transaction Fault", "Failed to modify configuration", ex.getMessage());
            }
        });
    }

    private void handleDeleteRoom(Room room) {
        boolean confirm = AlertUtil.showConfirmation("Destructive Inventory Action", "Purge Room " + room.getRoomNumber() + "?",
            "This action cascades and purges all nested physical spaces and active rental slots within this layout structural envelope. Proceed?");
        if (confirm) {
            try {
                roomDAO.delete(room.getId());
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Execution Aborted", "Failed to drop database node pointer references", ex.getMessage());
            }
        }
    }

    private Optional<Room> showRoomDialog(Room existing) {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Provision New Core Unit" : "Update Structural Layout Context");
        
        ButtonType saveBtnType = new ButtonType("Commit Config", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField numField = new TextField();
        numField.setPromptText("e.g., 101, 204-B");
        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Single Sharing", "Double Sharing", "Triple Sharing", "Four Sharing");
        typeBox.setValue("Double Sharing");
        TextField rentField = new TextField("0");

        if (existing != null) {
            numField.setText(existing.getRoomNumber());
            typeBox.setValue(existing.getRoomType());
            rentField.setText(String.valueOf(existing.getRent()));
        }

        grid.add(new Label("Unit Identifier/No:"), 0, 0); grid.add(numField, 1, 0);
        grid.add(new Label("Sharing Topology:"), 0, 1); grid.add(typeBox, 1, 1);
        grid.add(new Label("Monthly Base Rent (₹):"), 0, 2); grid.add(rentField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String num = numField.getText();
                if (num == null || num.trim().isEmpty()) return null;
                
                double rent;
                try {
                    rent = Double.parseDouble(rentField.getText());
                    if (rent < 0) return null;
                } catch (NumberFormatException nfe) {
                    return null;
                }

                int id = existing == null ? 0 : existing.getId();
                return new Room(id, currentPG.getId(), num.trim(), typeBox.getValue(), rent);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // =========================================================================
    // BED CRUD BUSINESS LOGIC
    // =========================================================================
    private void handleAddBed(Room room) {
        showBedDialog(null, room).ifPresent(bed -> {
            try {
                List<Bed> existing = bedDAO.findByRoomId(room.getId());
                for (Bed b : existing) {
                    if (b.getBedLabel().trim().equalsIgnoreCase(bed.getBedLabel().trim())) {
                        AlertUtil.showError("Validation Error", "Slot Reference Imbalance", 
                            "A bed layout node labeled '" + bed.getBedLabel() + "' already stands configured within this spatial layout.");
                        return;
                    }
                }
                bedDAO.insert(bed);
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Transaction Fault", "Failed to serialize physical item mapping node", ex.getMessage());
            }
        });
    }

    private void handleEditBed(Bed bed) {
        // Fetch matching container room mock for scope constraints safety
        Room mockRoom = new Room(bed.getRoomId(), currentPG.getId(), "", "", 0);
        showBedDialog(bed, mockRoom).ifPresent(updated -> {
            try {
                List<Bed> existing = bedDAO.findByRoomId(bed.getRoomId());
                for (Bed b : existing) {
                    if (b.getId() != updated.getId() && b.getBedLabel().trim().equalsIgnoreCase(updated.getBedLabel().trim())) {
                        AlertUtil.showError("Validation Error", "Label Inversion Failure", 
                            "Label configuration vector conflicts with an operational unit inside this micro-layout footprint.");
                        return;
                    }
                }
                bedDAO.update(updated);
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Transaction Fault", "Failed to synchronize state update properties", ex.getMessage());
            }
        });
    }

    private void handleDeleteBed(Bed bed) {
        boolean confirm = AlertUtil.showConfirmation("Destructive Component Drop", "Purge Unit Placement?", 
            "Are you certain you wish to remove bed component assignment sequence reference trace layout allocations irreversibly?");
        if (confirm) {
            try {
                bedDAO.delete(bed.getId());
                refreshData();
            } catch (Exception ex) {
                AlertUtil.showError("Aborted Engine Command", "Failed to detach entity sequence pointer link targets", ex.getMessage());
            }
        }
    }

    private Optional<Bed> showBedDialog(Bed existing, Room parentRoom) {
        Dialog<Bed> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Map Physical Unit" : "Update Component Properties");

        ButtonType saveBtnType = new ButtonType("Sync Core", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField lblField = new TextField();
        lblField.setPromptText("e.g., Bed-A, B2");
        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("vacant", "occupied");
        statusBox.setValue("vacant");
        TextField depField = new TextField("0");

        if (existing != null) {
            lblField.setText(existing.getBedLabel());
            statusBox.setValue(existing.getStatus().toLowerCase());
            depField.setText(String.valueOf(existing.getDeposit()));
        }

        grid.add(new Label("Bed Node Label:"), 0, 0); grid.add(lblField, 1, 0);
        grid.add(new Label("Allocation State Status:"), 0, 1); grid.add(statusBox, 1, 1);
        grid.add(new Label("Security Deposit Escrow (₹):"), 0, 2); grid.add(depField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtnType) {
                String label = lblField.getText();
                if (label == null || label.trim().isEmpty()) return null;

                double deposit;
                try {
                    deposit = Double.parseDouble(depField.getText());
                    if (deposit < 0) return null;
                } catch (NumberFormatException nfe) {
                    return null;
                }

                int id = existing == null ? 0 : existing.getId();
                return new Bed(id, parentRoom.getId(), label.trim(), statusBox.getValue(), deposit);
            }
            return null;
        });

        return dialog.showAndWait();
    }

    // =========================================================================
    // NAVIGATION ROUTING CHANNELS
    // =========================================================================
    @FXML
    public void goBack() {
        SelectedPGManager.clear();
        SceneManager.switchTo("MyPGs.fxml");
    }

    @FXML
    public void openMyPGs() {
        goBack();
    }

    @FXML
    public void openDashboard() {
        SceneManager.switchTo("OwnerDashboard.fxml");
    }

    @FXML
    public void openRoomsBeds() {
        // Do nothing since we are already on this screen
        return;
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
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon", "The reports dashboard and analytics module will be available in the next system update.");
    }

    @FXML
    public void openSettings() {
        SceneManager.switchTo("OwnerSettings.fxml");
    }

    @FXML
    public void handleLogout() {
        SelectedPGManager.clear();
        SessionManager.logout();
        SceneManager.switchTo("Login.fxml");
    }
}