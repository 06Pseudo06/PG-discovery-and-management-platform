package com.pgfinder.controller;

import com.pgfinder.dao.DashboardDAO;
import com.pgfinder.model.ActivityItem;
import com.pgfinder.model.ChatPreview;
import com.pgfinder.model.PropertyDashboardCard;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.pgfinder.dao.PGDAO;
import com.pgfinder.model.PG;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;

import javafx.scene.shape.Circle;

public class OwnerDashboardController implements Initializable {

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final PGDAO pgDAO = new PGDAO();
   

    @FXML private Label headerWelcomeTitle;
    @FXML private Label headerWelcomeDate;
    @FXML private Label totalPgsLabel;
    @FXML private Label confirmedTenantsStatLabel;
    @FXML private Label availableBedsLabel;
    @FXML private Label pendingRequestsStatLabel;
    @FXML private VBox propertiesContainer;
    @FXML private VBox activityFeedContainer;
    @FXML private VBox recentChatsContainer;

    @FXML
private ImageView ownerProfileImage;

@FXML
private Label ownerNameLabel;

@FXML
private Label ownerEmailLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeHeader();
        refreshDashboard();
    }
private void initializeHeader() {

    LocalDate today = LocalDate.now();

    headerWelcomeDate.setText(
            today.format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM dd")
            )
    );

    if (!SessionManager.isLoggedIn()) {

        headerWelcomeTitle.setText("Welcome");

        ownerNameLabel.setText("Guest");

        ownerEmailLabel.setText("");

        return;
    }

    var owner = SessionManager.getCurrentUser();

    String name = owner.getName();

    String firstName =
            (name != null && name.contains(" "))
                    ? name.split(" ")[0]
                    : name;

    headerWelcomeTitle.setText(
            "Welcome back, " + firstName + " 👋"
    );

    ownerNameLabel.setText(owner.getName());

    ownerEmailLabel.setText(totalPgsLabel.getText() + " Properties");

    String imagePath = owner.getProfileImagePath();

    if (imagePath != null && !imagePath.isBlank()) {

        File file = new File(imagePath);

        if (file.exists()) {

            Circle clip = new Circle(36, 36, 36);
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

@FXML
public void refreshDashboard() {
    try {
        int ownerId = SessionManager.getCurrentUser().getId();
        Map<String, Integer> metrics = dashboardDAO.getOwnerMetrics(ownerId);

        int totalPgs = metrics.getOrDefault("total_pgs", 0);

        totalPgsLabel.setText(String.valueOf(totalPgs));

        ownerEmailLabel.setText(
                totalPgs == 1
                        ? "1 Property"
                        : totalPgs + " Properties"
        );

        confirmedTenantsStatLabel.setText(
                String.valueOf(metrics.getOrDefault("occupied_beds", 0))
        );

        availableBedsLabel.setText(
                String.valueOf(metrics.getOrDefault("available_beds", 0))
        );

        pendingRequestsStatLabel.setText(
                String.valueOf(metrics.getOrDefault("pending_requests", 0))
        );

        loadProperties(ownerId);
        loadActivities(ownerId);
        loadChats(ownerId);

    } catch (Exception ex) {
        showDashboardError(ex);
    }
}

    private void loadProperties(int ownerId) {
        propertiesContainer.getChildren().clear();
        List<PropertyDashboardCard> properties = dashboardDAO.getPropertiesByOwner(ownerId);

        if (properties.isEmpty()) {
            propertiesContainer.getChildren().add(createEmptyState("🏠", "No Properties Yet", "Your listed properties will appear here."));
            return;
        }

        for (PropertyDashboardCard property : properties) {
            propertiesContainer.getChildren().add(createPropertyCard(property));
        }
    }

    private Node createPropertyCard(PropertyDashboardCard property) {
        HBox root = new HBox(16);
        root.getStyleClass().add("property-card");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(root, Priority.ALWAYS);

        VBox left = new VBox(4);
        VBox.setVgrow(left, Priority.ALWAYS);
        Label icon = new Label("🏠");
        icon.getStyleClass().add("property-card-icon");
        
        Label name = new Label(property.getName());
        name.setMaxWidth(Double.MAX_VALUE);
        name.getStyleClass().add("property-card-name");
        
        Label address = new Label("📍 " + property.getAddress());
        address.setMaxWidth(Double.MAX_VALUE);
        address.getStyleClass().add("property-card-address");
        
        left.getChildren().addAll(icon, name, address);

        VBox right = new VBox(6);
        right.setAlignment(Pos.CENTER_RIGHT);
        
        Label occupancy = new Label(property.getOccupiedBeds() + " of " + property.getTotalBeds() + " Beds Occupied");
        occupancy.setWrapText(true);
        occupancy.getStyleClass().add("property-card-occupancy");
        
        Label available = new Label(property.getAvailableBeds() + " Beds Available");
        available.getStyleClass().add("activity-meta");
        
        Button manage = new Button("Manage");
        manage.getStyleClass().add("property-manage-btn");
        manage.setOnAction(e -> openProperty(property));
        right.getChildren().addAll(occupancy, available, manage);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(left, spacer, right);
        return root;
    }

    private void loadActivities(int ownerId) {
        activityFeedContainer.getChildren().clear();
        List<ActivityItem> activities = dashboardDAO.getRecentActivities(ownerId);

        if (activities.isEmpty()) {
            activityFeedContainer.getChildren().add(createEmptyState("⚡", "No Recent Activity", "Bookings, approvals and updates will appear here."));
            return;
        }

        for (ActivityItem activity : activities) {
            activityFeedContainer.getChildren().add(createActivityCard(activity));
        }
    }

    private Node createActivityCard(ActivityItem activity) {
        HBox root = new HBox(12);
        root.getStyleClass().add("activity-row");
        root.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("●");
        switch (activity.getType()) {
            case SUCCESS -> dot.getStyleClass().add("activity-dot-green");
            case INFO -> dot.getStyleClass().add("activity-dot-blue");
            case WARNING -> dot.getStyleClass().add("activity-dot-amber");
            case ERROR -> dot.getStyleClass().add("activity-dot-slate");
        }

        VBox textContainer = new VBox(3);
        Label title = new Label(activity.getTitle());
        title.setWrapText(true);
        title.getStyleClass().add("activity-title");
        textContainer.getChildren().add(title);
        
        if (activity.getDescription() != null && !activity.getDescription().isBlank()) {
            Label description = new Label(activity.getDescription());
            description.getStyleClass().add("activity-meta");
            textContainer.getChildren().add(description);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label time = new Label(formatRelativeTime(activity.getCreatedAt()));
        time.getStyleClass().add("activity-time");

        root.getChildren().addAll(dot, textContainer, spacer, time);
        return root;
    }

    private void loadChats(int ownerId) {
        recentChatsContainer.getChildren().clear();
        List<ChatPreview> chats = dashboardDAO.getRecentMessages(ownerId);

        if (chats.isEmpty()) {
            recentChatsContainer.getChildren().add(createEmptyState("💬", "No Messages", "Recent conversations will appear here."));
            return;
        }

        for (ChatPreview chat : chats) {
            recentChatsContainer.getChildren().add(createChatCard(chat));
        }
    }

    private Node createChatCard(ChatPreview chat) {
        HBox root = new HBox(12);
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("chat-preview-card");

        String name = chat.getSenderName();
        String avatarLetter = (name != null && !name.isBlank()) ? name.substring(0, 1).toUpperCase() : "?";
        Label avatar = new Label(avatarLetter);
        avatar.getStyleClass().add("chat-avatar");

        VBox center = new VBox(3);
        Label sender = new Label(name == null ? "Unknown" : name);
        sender.setWrapText(true);
        sender.getStyleClass().add("chat-sender-name");
        
        String lastMsg = chat.getLastMessage() == null ? "" : chat.getLastMessage();
        Label message = new Label(lastMsg);
        message.getStyleClass().add("chat-message");
        message.setWrapText(true);
        message.setMaxWidth(220);
        center.getChildren().addAll(sender, message);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox right = new VBox(4);
        right.setAlignment(Pos.CENTER_RIGHT);
        Label time = new Label(formatRelativeTime(chat.getMessageTime()));
        time.getStyleClass().add("chat-time");
        right.getChildren().add(time);

        if (chat.isUnread()) {
            Label unread = new Label("NEW");
            unread.getStyleClass().add("chat-unread-badge");
            right.getChildren().add(unread);
        }

        root.getChildren().addAll(avatar, center, spacer, right);
        return root;
    }

    private VBox createEmptyState(String icon, String title, String subtitle) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("empty-state");
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("empty-state-icon");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-state-title");
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setWrapText(true);
        subtitleLabel.setMaxWidth(240);
        box.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        return box;
    }

    private String formatRelativeTime(LocalDateTime time) {
        if (time == null) return "";
        if (time.isAfter(LocalDateTime.now())) return "Just now";
        
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();
        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        long hours = duration.toHours();
        if (hours < 24) return hours + " hr ago";
        long days = duration.toDays();
        if (days < 7) return days + " day ago";
        return time.format(DateTimeFormatter.ofPattern("dd MMM"));
    }

 private void openProperty(PropertyDashboardCard property) {

    PG pg = pgDAO.findById(property.getId());

    if (pg == null) {

        AlertUtil.showError(
                "Property Error",
                "Property Not Found",
                "Unable to load the selected property."
        );

        return;
    }

    SelectedPGManager.setSelectedPG(pg);

    navigate("RoomsBeds.fxml");

}

    private void navigate(String scene) {
        SceneManager.switchTo(scene);
    }

    private void showDashboardError(Exception ex) {
        ex.printStackTrace();
        System.err.println("Dashboard Error: " + ex.getMessage());
        AlertUtil.showError( "Dashboard Error", "Dashboard", "Unable to load dashboard data."
);
    }

    @FXML private void handleRefresh() { refreshDashboard(); }

    @FXML public void openDashboard() { refreshDashboard(); }
    @FXML public void openMyPGs() { navigate("MyPGs.fxml"); }
@FXML
public void openRoomsBeds() {

    if (SelectedPGManager.hasSelectedPG()) {
        navigate("RoomsBeds.fxml");
    } else {
        AlertUtil.showWarning(
                "Context Required",
                "Selection Required",
                "Please select a property first."
        );
    }
}



    @FXML public void openBookingRequests() { navigate("BookingRequests.fxml"); }
    @FXML public void openTenants() { navigate("Tenants.fxml"); }
    @FXML public void openAnnouncements() { navigate("Announcements.fxml"); }
    @FXML public void openReviews() { navigate("OwnerReviews.fxml"); }
    @FXML public void openChat() { navigate("Chat.fxml"); }
    @FXML public void openSettings() { navigate("OwnerSettings.fxml"); }
    
    @FXML 
    public void handleLogout() {
        if (AlertUtil.showConfirmation("Logout", "Confirm Logout", "Are you sure you want to logout?")) {
            SessionManager.logout();
            navigate("Login.fxml");
        }
    }
} 