package com.pgfinder.service;

import com.pgfinder.config.DBConnection;
import com.pgfinder.dao.BookingDAO;
import com.pgfinder.dao.MessageDAO;
import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.dao.UserDAO;
import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.ChatConversation;
import com.pgfinder.model.Message;
import com.pgfinder.model.BookingStatus;
import com.pgfinder.model.User;
import com.pgfinder.util.NotificationMessages;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChatService {

    private final MessageDAO messageDAO = new MessageDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final UserDAO userDAO = new UserDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public List<ChatConversation> getConversations(int userId, String role) {
        Map<Integer, ChatConversation> conversations = new LinkedHashMap<>();
        List<BookingDetail> bookings = "OWNER".equalsIgnoreCase(role)
                ? bookingDAO.findByOwner(userId)
                : bookingDAO.findDetailsByStudent(userId);

        for (BookingDetail booking : bookings) {
            if (!com.pgfinder.model.BookingStatus.isActive(booking.getStatus())) {
                continue;
            }
            int otherUserId = "OWNER".equalsIgnoreCase(role) ? booking.getStudentId() : booking.getOwnerId();
            String otherName = "OWNER".equalsIgnoreCase(role) ? booking.getStudentName() : booking.getOwnerName();

            ChatConversation conversation = conversations.computeIfAbsent(otherUserId, id -> {
                ChatConversation c = new ChatConversation();
                c.setOtherUserId(id);
                c.setOtherUserName(otherName);
                c.setPgName(booking.getPgName());
                c.setUnreadCount(0);
                return c;
            });

            Message latest = messageDAO.findLatestBetween(userId, otherUserId);
            if (latest != null) {
                conversation.setLastMessage(latest.getBody());
                conversation.setLastMessageAt(latest.getCreatedAt());
            }
            conversation.setUnreadCount(messageDAO.countUnreadFromSender(userId, otherUserId));
        }

        for (Integer partnerId : messageDAO.findConversationPartnerIds(userId)) {
            if (conversations.containsKey(partnerId)) {
                continue;
            }
            if (!canChat(userId, partnerId)) {
                continue;
            }
            User partner = userDAO.findById(partnerId);
            ChatConversation conversation = new ChatConversation();
            conversation.setOtherUserId(partnerId);
            conversation.setOtherUserName(partner != null ? partner.getName() : "User");
            Message latest = messageDAO.findLatestBetween(userId, partnerId);
            if (latest != null) {
                conversation.setLastMessage(latest.getBody());
                conversation.setLastMessageAt(latest.getCreatedAt());
            }
            conversation.setUnreadCount(messageDAO.countUnreadFromSender(userId, partnerId));
            conversations.put(partnerId, conversation);
        }

        return new ArrayList<>(conversations.values());
    }

    public List<Message> getMessages(int userId, int otherUserId) {
        if (!canChat(userId, otherUserId)) {
            throw new BookingException("You are not allowed to chat with this user.");
        }
        messageDAO.markConversationRead(userId, otherUserId);
        return messageDAO.findConversation(userId, otherUserId);
    }

    public void sendMessage(int senderId, int receiverId, String body) {
        if (body == null || body.trim().isEmpty()) {
            throw new BookingException("Message cannot be empty.");
        }
        if (!canChat(senderId, receiverId)) {
            throw new BookingException("You are not allowed to chat with this user.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                messageDAO.insert(conn, senderId, receiverId, null, body.trim());
                notificationDAO.insert(conn, receiverId,
                        NotificationMessages.CHAT_MESSAGE + " Open chat to read.");
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new BookingException("Unable to send message. Please try again.");
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throw new BookingException("Unable to send message. Please try again.");
        }
    }

    public int countUnreadMessages(int userId) {
        return messageDAO.countUnreadForUser(userId);
    }

    public boolean canChat(int userId, int otherUserId) {
        User user = userDAO.findById(userId);
        User other = userDAO.findById(otherUserId);
        if (user == null || other == null) {
            return false;
        }

        if ("OWNER".equalsIgnoreCase(user.getRole()) && "STUDENT".equalsIgnoreCase(other.getRole())) {
            return hasSharedBooking(other.getId(), user.getId());
        }
        if ("STUDENT".equalsIgnoreCase(user.getRole()) && "OWNER".equalsIgnoreCase(other.getRole())) {
            return hasSharedBooking(user.getId(), other.getId());
        }
        return false;
    }

    private boolean hasSharedBooking(int studentId, int ownerId) {
        return bookingDAO.findDetailsByStudent(studentId).stream()
                .anyMatch(b -> b.getOwnerId() == ownerId
                        && BookingStatus.isOccupying(b.getStatus()));
    }
}
