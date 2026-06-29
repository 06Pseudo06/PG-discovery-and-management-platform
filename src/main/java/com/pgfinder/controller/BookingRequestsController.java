package com.pgfinder.controller;

import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.BookingStatus;
import com.pgfinder.model.PG;
import com.pgfinder.model.User;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.BookingService;
import com.pgfinder.service.PGService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.BookingRefreshHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class BookingRequestsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final BookingService bookingService = new BookingService();
    private final PGService pgService = new PGService();
    private List<BookingDetail> allRequests;

    @FXML
    private VBox requestsContainer;
    @FXML
    private ComboBox<String> pgFilterCombo;
    @FXML
    private TextField searchField;
    @FXML
    private ToggleButton allFilterBtn;
    @FXML
    private ToggleButton pendingFilterBtn;
    @FXML
    private ToggleButton approvedFilterBtn;
    @FXML
    private Label pendingBadgeLabel;

    @FXML
    public void initialize() {
        if (!SessionManager.isLoggedIn()) {
            Platform.runLater(() ->
                    AlertUtil.showWarning("Not Logged In", "Session Required", "Please log in as an owner."));
            return;
        }
        try {
            loadPgFilter();
            refreshRequests();
            wireFilters();
        } catch (Exception ex) {
            System.err.println("Failed to load booking requests: " + ex.getMessage());
            if (requestsContainer != null) {
                requestsContainer.getChildren().clear();
                requestsContainer.getChildren().add(new Label("Unable to load booking requests. Check database connection."));
            }
        }
    }

    private void wireFilters() {
        if (pgFilterCombo != null) {
            pgFilterCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> renderRequests());
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> renderRequests());
        }
        if (allFilterBtn != null) {
            allFilterBtn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) renderRequests();
            });
        }
        if (pendingFilterBtn != null) {
            pendingFilterBtn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) renderRequests();
            });
        }
        if (approvedFilterBtn != null) {
            approvedFilterBtn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) renderRequests();
            });
        }
    }

    private void loadPgFilter() {
        if (pgFilterCombo == null || !SessionManager.isLoggedIn()) {
            return;
        }
        User owner = SessionManager.getCurrentUser();
        List<String> pgNames = pgService.getOwnerPGs(owner.getId()).stream()
                .map(PG::getName)
                .collect(Collectors.toList());
        pgNames.add(0, "All Properties");
        pgFilterCombo.setItems(javafx.collections.FXCollections.observableArrayList(pgNames));
        pgFilterCombo.getSelectionModel().selectFirst();
    }

    private void refreshRequests() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        User owner = SessionManager.getCurrentUser();
        allRequests = bookingService.getBookingsForOwner(owner.getId());
        if (pendingBadgeLabel != null) {
            pendingBadgeLabel.setText(String.valueOf(BookingRefreshHelper.getPendingRequestCount(owner.getId())));
        }
        renderRequests();
    }

    private void renderRequests() {
        if (requestsContainer == null || allRequests == null) {
            return;
        }
        requestsContainer.getChildren().clear();

        List<BookingDetail> filtered = allRequests.stream()
                .filter(this::matchesPgFilter)
                .filter(this::matchesStatusFilter)
                .filter(this::matchesSearch)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            requestsContainer.getChildren().add(new Label("No booking requests found."));
            return;
        }

        for (BookingDetail detail : filtered) {
            requestsContainer.getChildren().add(buildRequestCard(detail));
        }
    }

    private boolean matchesPgFilter(BookingDetail detail) {
        if (pgFilterCombo == null) {
            return true;
        }
        String selected = pgFilterCombo.getSelectionModel().getSelectedItem();
        return selected == null || "All Properties".equals(selected) || selected.equals(detail.getPgName());
    }

    private boolean matchesStatusFilter(BookingDetail detail) {
        if (pendingFilterBtn != null && pendingFilterBtn.isSelected()) {
            return BookingStatus.PENDING_OWNER.equalsIgnoreCase(detail.getStatus());
        }
        if (approvedFilterBtn != null && approvedFilterBtn.isSelected()) {
            return BookingStatus.AWAITING_STUDENT_CONFIRMATION.equalsIgnoreCase(detail.getStatus())
                    || BookingStatus.CONFIRMED.equalsIgnoreCase(detail.getStatus());
        }
        return true;
    }

    private boolean matchesSearch(BookingDetail detail) {
        if (searchField == null || searchField.getText().trim().isEmpty()) {
            return true;
        }
        String query = searchField.getText().trim().toLowerCase();
        return detail.getStudentName().toLowerCase().contains(query)
                || detail.getStudentEmail().toLowerCase().contains(query);
    }

    private VBox buildRequestCard(BookingDetail detail) {
        VBox card = new VBox(15);
        card.getStyleClass().add("owner-large-card");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");
        Label initials = new Label(getInitials(detail.getStudentName()));
        initials.getStyleClass().add("avatar-text");
        initials.setStyle("-fx-font-size: 14px;");
        avatar.getChildren().add(initials);

        VBox info = new VBox();
        HBox titleRow = new HBox();
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label(detail.getStudentName());
        nameLabel.getStyleClass().add("item-main-title");
        nameLabel.setStyle("-fx-font-size: 16px;");
        Label contactLabel = new Label("•  " + detail.getStudentEmail() + "  •  " + detail.getStudentPhone());
        contactLabel.getStyleClass().add("item-subtitle");
        contactLabel.setStyle("-fx-padding: 0 0 0 10;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Label statusLabel = new Label(detail.getStatus().toUpperCase());
        statusLabel.getStyleClass().add("priority-medium");
        titleRow.getChildren().addAll(nameLabel, contactLabel, spacer, statusLabel);

        Label pgLabel = new Label("Requested " + detail.getPgName() + "  •  Room "
                + detail.getRoomNumber() + " (Bed " + detail.getBedLabel() + ")");
        pgLabel.getStyleClass().add("item-subtitle");
        pgLabel.setStyle("-fx-padding: 5 0;");
        info.getChildren().addAll(titleRow, pgLabel);
        header.getChildren().addAll(avatar, info);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(buildStatBox("Preferred Move-in", detail.getStartDate().format(DATE_FORMAT)), 0, 0);
        grid.add(buildStatBox("Monthly Rent Rate", "₹" + String.format("%.0f", detail.getRent())), 1, 0);
        grid.add(buildStatBox("Security Deposit", "₹" + String.format("%.0f", detail.getDeposit())), 2, 0);
        grid.add(buildStatBox("Submitted On", detail.getCreatedAt().format(DATE_FORMAT)), 3, 0);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("secondary-button");
        detailsBtn.setOnAction(e -> showDetails(detail));

        if (BookingStatus.PENDING_OWNER.equalsIgnoreCase(detail.getStatus())) {
            Button rejectBtn = new Button("Reject Request");
            rejectBtn.getStyleClass().add("secondary-button");
            rejectBtn.setStyle("-fx-text-fill: #DC2626; -fx-border-color: #FCA5A5;");
            rejectBtn.setOnAction(e -> handleReject(detail));

            Button approveBtn = new Button("Approve Request");
            approveBtn.getStyleClass().add("primary-button");
            approveBtn.setOnAction(e -> handleApprove(detail));

            actions.getChildren().addAll(detailsBtn, rejectBtn, approveBtn);
        } else {
            actions.getChildren().add(detailsBtn);
        }

        card.getChildren().addAll(header, new Separator(), grid, new Separator(), actions);
        return card;
    }

    private VBox buildStatBox(String label, String value) {
        VBox box = new VBox();
        Label statLabel = new Label(label);
        statLabel.getStyleClass().add("stat-label");
        Label statValue = new Label(value);
        statValue.setStyle("-fx-font-weight: bold;");
        box.getChildren().addAll(statLabel, statValue);
        return box;
    }

    private String getInitials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private void showDetails(BookingDetail detail) {
        StringBuilder content = new StringBuilder();
        content.append("Student: ").append(detail.getStudentName()).append("\n");
        content.append("PG: ").append(detail.getPgName()).append("\n");
        content.append("Room: ").append(detail.getRoomNumber()).append(" (").append(detail.getRoomType()).append(")\n");
        content.append("Bed: ").append(detail.getBedLabel()).append("\n");
        content.append("Move-in: ").append(detail.getStartDate().format(DATE_FORMAT)).append("\n");
        content.append("Status: ").append(detail.getStatus()).append("\n");
        if (detail.getStudentNotes() != null && !detail.getStudentNotes().isBlank()) {
            content.append("\nStudent Notes:\n").append(detail.getStudentNotes());
        }
        if (detail.getOwnerRemarks() != null && !detail.getOwnerRemarks().isBlank()) {
            content.append("\n\nOwner Remarks:\n").append(detail.getOwnerRemarks());
        }
        AlertUtil.showInfo("Booking Details", detail.getPgName(), content.toString());
    }

    private void handleApprove(BookingDetail detail) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        if (!AlertUtil.showConfirmation("Approve Booking", "Confirm Approval",
                "Approve booking for " + detail.getStudentName() + "? The bed will be marked as occupied.")) {
            return;
        }
        try {
            bookingService.approveBooking(SessionManager.getCurrentUser().getId(), detail.getBookingId(), null);
            AlertUtil.showInfo("Approved", "Booking Approved",
                    "The booking has been approved and the bed is now occupied.");
            refreshRequests();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Approval Failed", "Unable to Approve", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Approval Failed", "Unexpected Error",
                    "Something went wrong while approving the request.");
        }
    }

    private void handleReject(BookingDetail detail) {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        if (!AlertUtil.showConfirmation("Reject Booking", "Confirm Rejection",
                "Reject booking request from " + detail.getStudentName() + "?")) {
            return;
        }
        try {
            bookingService.rejectBooking(SessionManager.getCurrentUser().getId(), detail.getBookingId(),
                    "Rejected by owner.");
            AlertUtil.showInfo("Rejected", "Booking Rejected", "The booking request has been rejected.");
            refreshRequests();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Rejection Failed", "Unable to Reject", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Rejection Failed", "Unexpected Error",
                    "Something went wrong while rejecting the request.");
        }
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
    public void openMyPGs() {
        SceneManager.switchTo("MyPGs.fxml");
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
    public void openRoomsBeds(PG pg) {
        SelectedPGManager.setSelectedPG(pg);
        SceneManager.switchTo("RoomsBeds.fxml");
    }

    @FXML
    public void openBookingRequests() {
        refreshRequests();
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
        AlertUtil.showInfo("Reports Module", "Feature Coming Soon",
                "The reports dashboard and analytics module will be available in the next system update.");
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
