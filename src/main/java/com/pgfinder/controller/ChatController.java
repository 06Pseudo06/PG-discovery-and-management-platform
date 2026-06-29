package com.pgfinder.controller;

import com.pgfinder.model.ChatConversation;
import com.pgfinder.model.Message;
import com.pgfinder.model.PG;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.ChatService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.DashboardBadgeHelper;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SelectedPGManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    private final ChatService chatService = new ChatService();
    private int selectedPartnerId = -1;

    @FXML
    private VBox conversationsContainer;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private Label chatPartnerLabel;
    @FXML
    private Label pendingBadgeLabel;
    @FXML
    private Label chatBadgeLabel;

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            int userId = SessionManager.getCurrentUser().getId();
            DashboardBadgeHelper.updatePendingRequestsBadge(pendingBadgeLabel, userId);
            DashboardBadgeHelper.updateChatBadge(chatBadgeLabel, userId);
        }
        refreshConversations();
    }

private void refreshConversations() {
        if (conversationsContainer == null || !SessionManager.isLoggedIn()) {
            return;
        }
        conversationsContainer.getChildren().clear();
        
        List<ChatConversation> conversations;
        try {
            conversations = chatService.getConversations(
                    SessionManager.getCurrentUser().getId(), "OWNER");
        } catch (BookingException ex) {
            // Handle the exception gracefully by showing an empty list state
            conversationsContainer.getChildren().add(new Label("No tenant conversations yet."));
            loadMessages(); // Directs the message container to show its empty placeholder
            return;
        }

        if (conversations.isEmpty()) {
            conversationsContainer.getChildren().add(new Label("No tenant conversations yet."));
            loadMessages(); // Directs the message container to show its empty placeholder
            return;
        }

        for (ChatConversation conversation : conversations) {
            Button btn = new Button(conversation.getOtherUserName()
                    + (conversation.getPgName() != null ? " (" + conversation.getPgName() + ")" : ""));
            btn.getStyleClass().add("chat-user");
            if (conversation.getOtherUserId() == selectedPartnerId) {
                btn.getStyleClass().add("chat-user-active");
            }
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER_LEFT);
            btn.setOnAction(e -> selectConversation(conversation.getOtherUserId(), conversation.getOtherUserName()));
            conversationsContainer.getChildren().add(btn);
        }

        if (selectedPartnerId <= 0 && !conversations.isEmpty()) {
            ChatConversation first = conversations.get(0);
            selectConversation(first.getOtherUserId(), first.getOtherUserName());
        } else if (selectedPartnerId <= 0) {
            loadMessages();
        }
    }

    private void selectConversation(int partnerId, String partnerName) {
        selectedPartnerId = partnerId;
        if (chatPartnerLabel != null) {
            chatPartnerLabel.setText(partnerName);
        }
        loadMessages();
        refreshConversations();
    }

    private void loadMessages() {
        if (messagesContainer == null || !SessionManager.isLoggedIn()) {
            return;
        }
        messagesContainer.getChildren().clear();

        if (selectedPartnerId <= 0) {
            Label placeholder = new Label("No conversations available yet. Chat becomes available after a confirmed booking.");
            placeholder.getStyleClass().add("info-label");
            messagesContainer.getChildren().add(placeholder);
            return;
        }

        int userId = SessionManager.getCurrentUser().getId();
        try {
            List<Message> messages = chatService.getMessages(userId, selectedPartnerId);

            if (messages.isEmpty()) {
                messagesContainer.getChildren().add(new Label("No messages yet. Start the conversation."));
                return;
            }

            for (Message message : messages) {
                boolean sentByMe = message.getSenderId() == userId;
                VBox messageBox = new VBox(4);
                messageBox.setAlignment(sentByMe ? Pos.TOP_RIGHT : Pos.TOP_LEFT);

                String header = (sentByMe ? "You" : message.getSenderName())
                        + " • " + message.getCreatedAt().format(TIME_FORMAT);
                Label headerLabel = new Label(header);
                headerLabel.getStyleClass().add("chat-time");

                Label bubble = new Label(message.getBody());
                bubble.getStyleClass().add(sentByMe ? "message-sent" : "message-received");
                bubble.setWrapText(true);

                messageBox.getChildren().addAll(headerLabel, bubble);
                HBox wrapper = new HBox(messageBox);
                wrapper.setAlignment(sentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                messagesContainer.getChildren().add(wrapper);
            }
        } catch (BookingException ex) {
            Label errorLabel = new Label("No conversations available yet. Chat becomes available after a confirmed booking.");
            errorLabel.getStyleClass().add("info-label");
            messagesContainer.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void handleSendMessage() {
        if (!SessionManager.isLoggedIn() || selectedPartnerId <= 0) {
            AlertUtil.showWarning("No Conversation", "Select a Tenant", "Choose a tenant conversation first.");
            return;
        }
        String text = messageInput.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try {
            chatService.sendMessage(SessionManager.getCurrentUser().getId(), selectedPartnerId, text.trim());
            messageInput.clear();
            loadMessages();
            if (SessionManager.isLoggedIn()) {
                DashboardBadgeHelper.updateChatBadge(chatBadgeLabel, SessionManager.getCurrentUser().getId());
            }
        } catch (BookingException ex) {
            AlertUtil.showWarning("Send Failed", "Unable to Send", ex.getMessage());
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
                    "Please select a specific PG property from your list first.");
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
