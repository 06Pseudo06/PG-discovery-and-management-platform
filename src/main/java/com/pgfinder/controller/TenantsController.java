package com.pgfinder.controller;

import com.pgfinder.dao.TenantDAO;
import com.pgfinder.model.Tenant;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TenantsController {

    @FXML
    private VBox tenantsContainer; // Bound to FXML changes

    private final TenantDAO tenantDAO = new TenantDAO();

    @FXML
    public void initialize() {
        loadDynamicTenants();
    }

    private void loadDynamicTenants() {
        tenantsContainer.getChildren().clear(); // Wipe any remaining elements

        // Retrieve current logged in owner ID from session
        // (Adjust this line to match how your SessionManager extracts User ID)
        int currentOwnerId = SessionManager.getCurrentUser().getId(); 

        List<Tenant> tenants = tenantDAO.getTenantsByOwner(currentOwnerId);

        if (tenants.isEmpty()) {
            Label noTenantsLabel = new Label("No active tenants found.");
            noTenantsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #777; -fx-padding: 20;");
            tenantsContainer.getChildren().add(noTenantsLabel);
            return;
        }

        for (Tenant tenant : tenants) {
            tenantsContainer.getChildren().add(createTenantCard(tenant));
        }
    }

    private VBox createTenantCard(Tenant tenant) {
        VBox card = new VBox(15);
        card.getStyleClass().add("owner-large-card");

        // 1. HEADER ROW (Avatar + Basic Info + Status)
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        // Extract initials for Avatar Bubble
        String initials = "";
        if (tenant.getName() != null && !tenant.getName().isEmpty()) {
            String[] parts = tenant.getName().split(" ");
            initials += parts[0].charAt(0);
            if (parts.length > 1) initials += parts[1].charAt(0);
        }
        
        StackPane avatarCircle = new StackPane();
        avatarCircle.getStyleClass().add("avatar-circle");
        Label avatarText = new Label(initials.toUpperCase());
        avatarText.getStyleClass().add("avatar-text");
        avatarText.setStyle("-fx-font-size: 14px;");
        avatarCircle.getChildren().add(avatarText);

        VBox identityBox = new VBox();
        HBox.setHgrow(identityBox, Priority.ALWAYS);
        
        HBox nameLine = new HBox();
        nameLine.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(tenant.getName());
        nameLbl.getStyleClass().add("item-main-title");
        nameLbl.setStyle("-fx-font-size: 16px;");
        
        Label metaLbl = new Label(" • " + tenant.getEmail() + " • " + tenant.getPhone());
        metaLbl.getStyleClass().add("item-subtitle");
        metaLbl.setStyle("-fx-padding: 0 0 0 10;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusTag = new Label("STAYING");
        statusTag.getStyleClass().add("status-tag");
        
        nameLine.getChildren().addAll(nameLbl, metaLbl, spacer, statusTag);
        
        Label subDetailsLbl = new Label(tenant.getPgName() + " • Room " + tenant.getRoomNumber() + " • Bed " + tenant.getRoomType());
        subDetailsLbl.getStyleClass().add("item-subtitle");
        subDetailsLbl.setStyle("-fx-padding: 5 0;");
        
        identityBox.getChildren().addAll(nameLine, subDetailsLbl);
        header.getChildren().addAll(avatarCircle, identityBox);

        // 2. GRID DETAILS (Dates, Rates, and Stats)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            grid.getColumnConstraints().add(cc);
        }

        VBox col0 = new VBox(new Label("Move-in Date") {{ getStyleClass().add("stat-label"); }}, new Label(tenant.getMoveInDate().toString()) {{ setStyle("-fx-font-weight: bold;"); }});
        VBox col1 = new VBox(new Label("Contract End") {{ getStyleClass().add("stat-label"); }}, new Label(tenant.getEndDate() != null ? tenant.getEndDate().toString() : "N/A") {{ setStyle("-fx-font-weight: bold;"); }});
        VBox col2 = new VBox(new Label("Monthly Rent Rate") {{ getStyleClass().add("stat-label"); }}, new Label("₹" + tenant.getRentRate()) {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #0F4F46;"); }});
        VBox col3 = new VBox(new Label("Rent Status") {{ getStyleClass().add("stat-label"); }}, new Label("Paid") {{ setStyle("-fx-font-weight: bold; -fx-text-fill: #16A34A;"); }});

        grid.add(col0, 0, 0);
        grid.add(col1, 1, 0);
        grid.add(col2, 2, 0);
        grid.add(col3, 3, 0);

        // 3. COUNTDOWN PROGRESS BAR
        VBox progressSection = new VBox(6);
        HBox progressInfo = new HBox();
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        
        Label countdownLabel = new Label("Lease Move-Out Countdown");
        countdownLabel.getStyleClass().add("stat-label");
        Region progressSpacer = new Region();
        HBox.setHgrow(progressSpacer, Priority.ALWAYS);
        
        long daysRemaining = 0;
        double progressPercentage = 1.0;
        if (tenant.getEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), tenant.getEndDate());
            long totalDays = ChronoUnit.DAYS.between(tenant.getMoveInDate(), tenant.getEndDate());
            if (totalDays > 0) {
                progressPercentage = (double) (totalDays - Math.max(0, daysRemaining)) / totalDays;
            }
        }
        
        Label daysRemainingLbl = new Label(Math.max(0, daysRemaining) + " Days Remaining");
        daysRemainingLbl.getStyleClass().add("stat-label");
        daysRemainingLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F4F46;");
        
        progressInfo.getChildren().addAll(countdownLabel, progressSpacer, daysRemainingLbl);
        
        ProgressBar progressBar = new ProgressBar(progressPercentage);
        progressBar.setPrefWidth(950);
        progressBar.setStyle("-fx-min-height: 8px;");
        
        progressSection.getChildren().addAll(progressInfo, progressBar);

        // 4. FOOTER ACTIONS
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnLease = new Button("Lease Details");
        btnLease.getStyleClass().add("secondary-button");
        Button btnChat = new Button("Send Chat Message");
        btnChat.getStyleClass().add("primary-button");
        btnChat.setOnAction(e -> openChat());
        
        actionsRow.getChildren().addAll(btnLease, btnChat);

        // Assemble with separators
        card.getChildren().addAll(header, new Separator(), grid, new Separator(), progressSection, new Separator(), actionsRow);
        return card;
    }

    // --- Keep your existing navigation logic below ---
    @FXML public void goBack() { SceneManager.switchTo("OwnerDashboard.fxml"); }
    @FXML public void openDashboard() { SceneManager.switchTo("OwnerDashboard.fxml"); }
    @FXML public void openMyPGs() { SceneManager.switchTo("MyPGs.fxml"); }
    @FXML public void openBookingRequests() { SceneManager.switchTo("BookingRequests.fxml"); }
    @FXML public void openTenants() { SceneManager.switchTo("Tenants.fxml"); }
    @FXML public void openAnnouncements() { SceneManager.switchTo("Announcements.fxml"); }
    @FXML public void openChat() { SceneManager.switchTo("Chat.fxml"); }
    @FXML public void openReviews() { SceneManager.switchTo("OwnerReviews.fxml"); }
    @FXML public void openSettings() { SceneManager.switchTo("OwnerSettings.fxml"); }
    @FXML public void handleLogout() { SessionManager.logout(); SceneManager.switchTo("Login.fxml"); }
    
    @FXML
    public void openRoomsBeds() {
        if (SelectedPGManager.getSelectedPG() != null) {
            SceneManager.switchTo("RoomsBeds.fxml");
        } else {
            AlertUtil.showWarning("Context Missing", "No PG Selected", "Please select a specific PG property first.");
            SceneManager.switchTo("MyPGs.fxml");
        }
    }
    @FXML public void openReports() { AlertUtil.showInfo("Reports Module", "Coming Soon", "Available next update."); }
}

