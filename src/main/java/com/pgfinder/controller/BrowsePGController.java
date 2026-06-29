package com.pgfinder.controller;

import com.pgfinder.dao.RoomDAO;
import com.pgfinder.model.PG;
import com.pgfinder.model.Room;
import com.pgfinder.service.PGService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.BookingRefreshHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

public class BrowsePGController {

    private final PGService pgService = new PGService();
    private final RoomDAO roomDAO = new RoomDAO();

    @FXML
    private TilePane pgGrid;
    @FXML
    private Label profileNameLabel;

    @FXML
    public void initialize() {
        updateProfileLabel();
        refreshPgGrid();
    }

    private void updateProfileLabel() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
    }

    private void refreshPgGrid() {
        if (pgGrid == null) {
            return;
        }
        pgGrid.getChildren().clear();
        List<PG> pgs = pgService.getAllPGs();
        if (pgs.isEmpty()) {
            pgGrid.getChildren().add(new Label("No PG listings available at the moment."));
            return;
        }
        for (PG pg : pgs) {
            pgGrid.getChildren().add(buildPgCard(pg));
        }
    }

    private VBox buildPgCard(PG pg) {
        int vacantBeds = BookingRefreshHelper.getVacantBedCountForPg(pg.getId());
        double minRent = roomDAO.findByPgId(pg.getId()).stream()
                .mapToDouble(Room::getRent)
                .min()
                .orElse(0);

        VBox card = new VBox(6);
        card.getStyleClass().add("pg-card");
        card.setPrefWidth(300);

        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("pg-image");
        imagePane.setStyle("-fx-background-color: #DFF1EA;");
        Label imageTitle = new Label(pg.getName());
        imageTitle.getStyleClass().add("pg-title");
        imageTitle.setStyle("-fx-text-fill: #214F4B;");
        imagePane.getChildren().add(imageTitle);

        Label nameLabel = new Label(pg.getName());
        nameLabel.getStyleClass().add("pg-title");
        Label locationLabel = new Label("📍 " + pg.getArea() + ", " + pg.getCity());
        locationLabel.getStyleClass().add("sub-heading");
        locationLabel.setStyle("-fx-font-size: 12px;");

        HBox tags = new HBox(8);
        tags.setPadding(new Insets(5, 0, 5, 0));
        Label genderTag = new Label(formatGender(pg.getGenderPreference()));
        genderTag.getStyleClass().add("gender-tag");
        Label statusTag = new Label(vacantBeds > 0 ? "AVAILABLE" : "FULL");
        statusTag.getStyleClass().add(vacantBeds > 0 ? "status-tag" : "status-tag-warning");
        tags.getChildren().addAll(genderTag, statusTag);

        Label amenitiesLabel = new Label(buildAmenitiesText(pg));
        amenitiesLabel.getStyleClass().add("sub-heading");
        amenitiesLabel.setStyle("-fx-font-size: 11px;");

        Label bedsLabel = new Label(vacantBeds + " Bed" + (vacantBeds == 1 ? "" : "s") + " Available");
        bedsLabel.getStyleClass().add("sub-heading");
        bedsLabel.setStyle("-fx-text-fill: #16A34A; -fx-font-weight: bold;");

        HBox priceRow = new HBox();
        priceRow.setAlignment(Pos.CENTER_LEFT);
        Label priceLabel = new Label("₹" + String.format("%.0f", minRent));
        priceLabel.getStyleClass().add("price-text");
        Label perMonth = new Label("/month");
        perMonth.getStyleClass().add("sub-heading");
        perMonth.setStyle("-fx-font-size: 11px;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        Button detailsBtn = new Button("View Details");
        detailsBtn.getStyleClass().add("btn-primary");
        detailsBtn.setOnAction(e -> openPGDetails(pg));
        priceRow.getChildren().addAll(priceLabel, perMonth, spacer, detailsBtn);

        card.getChildren().addAll(imagePane, nameLabel, locationLabel, tags, amenitiesLabel, bedsLabel,
                new Separator(), priceRow);
        return card;
    }

    private String formatGender(String preference) {
        if (preference == null) {
            return "ANY";
        }
        return switch (preference.toLowerCase()) {
            case "male" -> "BOYS";
            case "female" -> "GIRLS";
            default -> "CO-ED";
        };
    }

    private String buildAmenitiesText(PG pg) {
        StringBuilder sb = new StringBuilder();
        if (pg.isWifiAvailable()) sb.append("WiFi • ");
        if (pg.isFoodAvailable()) sb.append("Food • ");
        if (pg.isAcAvailable()) sb.append("AC • ");
        if (pg.isLaundryAvailable()) sb.append("Laundry • ");
        if (pg.isGymAvailable()) sb.append("Gym • ");
        if (pg.isParkingAvailable()) sb.append("Parking • ");
        if (sb.length() >= 3) {
            sb.setLength(sb.length() - 3);
        }
        return sb.length() > 0 ? sb.toString() : "Basic amenities";
    }

    private void openPGDetails(PG pg) {
        SelectedPGManager.setSelectedPG(pg);
        SceneManager.switchTo("PGDetails.fxml");
    }

    @FXML
    public void goBack() {
        SceneManager.switchTo("StudentDashboard.fxml");
    }

    @FXML
    public void openPGDetails() {
        if (!SelectedPGManager.hasSelectedPG()) {
            AlertUtil.showWarning("No PG Selected", "Select a PG", "Please choose a PG from the list first.");
            return;
        }
        SceneManager.switchTo("PGDetails.fxml");
    }

    @FXML
    private void openDashboard() {
        SceneManager.switchTo("StudentDashboard.fxml");
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
