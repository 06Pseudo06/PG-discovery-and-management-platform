package com.pgfinder.controller;

import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.Message;
import com.pgfinder.service.BookingException;
import com.pgfinder.service.BookingService;
import com.pgfinder.service.ChatService;
import com.pgfinder.util.AlertUtil;
import com.pgfinder.util.SceneManager;
import com.pgfinder.util.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentChatController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    private final ChatService chatService = new ChatService();
    private final BookingService bookingService = new BookingService();
    private int ownerPartnerId = -1;

    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField messageInput;
    @FXML
    private Label chatPartnerLabel;
    @FXML
    private Label profileNameLabel;

    @FXML
    public void initialize() {
        if (profileNameLabel != null && SessionManager.isLoggedIn()) {
            profileNameLabel.setText("Hi, " + SessionManager.getCurrentUser().getName());
        }
        resolveChatPartner();
        loadMessages();
    }

    private void resolveChatPartner() {
        if (!SessionManager.isLoggedIn()) {
            return;
        }
        int studentId = SessionManager.getCurrentUser().getId();
        BookingDetail stay = bookingService.getActiveStayForStudent(studentId);
        if (stay == null) {
            List<BookingDetail> awaiting = bookingService.getAwaitingConfirmationForStudent(studentId);
            if (!awaiting.isEmpty()) {
                stay = awaiting.get(0);
            }
        }
        if (stay == null) {
            List<BookingDetail> history = bookingService.getStudentBookingHistory(studentId);
            stay = history.stream()
                    .filter(b -> com.pgfinder.model.BookingStatus.isActive(b.getStatus()))
                    .findFirst()
                    .orElse(null);
        }
        if (stay != null) {
            ownerPartnerId = stay.getOwnerId();
            if (chatPartnerLabel != null) {
                chatPartnerLabel.setText(stay.getOwnerName() + " • Owner of " + stay.getPgName());
            }
        } else if (chatPartnerLabel != null) {
            chatPartnerLabel.setText("No active booking — chat unavailable");
        }
    }

    private void loadMessages() {
        if (messagesContainer == null || !SessionManager.isLoggedIn()) {
            return;
        }
        messagesContainer.getChildren().clear();

        if (ownerPartnerId <= 0) {
            Label placeholder = new Label("No conversations available yet. Chat becomes available after a confirmed booking.");
            placeholder.getStyleClass().add("info-label");
            messagesContainer.getChildren().add(placeholder);
            return;
        }

        int studentId = SessionManager.getCurrentUser().getId();
        try {
            List<Message> messages = chatService.getMessages(studentId, ownerPartnerId);

            if (messages.isEmpty()) {
                messagesContainer.getChildren().add(new Label("No messages yet. Say hello to your owner."));
                return;
            }

            for (Message message : messages) {
                boolean sentByMe = message.getSenderId() == studentId;
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
        if (!SessionManager.isLoggedIn() || ownerPartnerId <= 0) {
            AlertUtil.showWarning("Chat Unavailable", "No Booking",
                    "You need an active or approved booking to chat with the owner.");
            return;
        }
        String text = messageInput.getText();
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try {
            chatService.sendMessage(SessionManager.getCurrentUser().getId(), ownerPartnerId, text.trim());
            messageInput.clear();
            loadMessages();
        } catch (BookingException ex) {
            AlertUtil.showWarning("Send Failed", "Unable to Send", ex.getMessage());
        }
    }

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
    private void openPGHistory() {
        SceneManager.switchTo("PGHistory.fxml");
    }

    @FXML
    private void openAnnouncements() {
        SceneManager.switchTo("StudentAnnouncements.fxml");
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
