package com.pgfinder.util;

import com.pgfinder.dao.NotificationDAO;
import com.pgfinder.model.Notification;
import com.pgfinder.service.BookingService;
import com.pgfinder.service.ChatService;
import javafx.scene.control.Label;

import java.util.List;

public final class DashboardBadgeHelper {

    private static final NotificationDAO notificationDAO = new NotificationDAO();
    private static final BookingService bookingService = new BookingService();
    private static final ChatService chatService = new ChatService();

    private DashboardBadgeHelper() {
    }

    public static void updateNotificationBadge(Label badgeLabel, int userId) {
        if (badgeLabel == null) {
            return;
        }
        int count = notificationDAO.countUnread(userId);
        badgeLabel.setText(String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        badgeLabel.setManaged(count > 0);
    }

    public static void updatePendingRequestsBadge(Label badgeLabel, int ownerId) {
        if (badgeLabel == null) {
            return;
        }
        int count = bookingService.countPendingRequests(ownerId);
        badgeLabel.setText(String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        badgeLabel.setManaged(count > 0);
    }

    public static void updateChatBadge(Label badgeLabel, int userId) {
        if (badgeLabel == null) {
            return;
        }
        int count = chatService.countUnreadMessages(userId);
        badgeLabel.setText(String.valueOf(count));
        badgeLabel.setVisible(count > 0);
        badgeLabel.setManaged(count > 0);
    }

    public static List<Notification> getRecentNotifications(int userId, int limit) {
        List<Notification> all = notificationDAO.findByUserId(userId);
        return all.size() <= limit ? all : all.subList(0, limit);
    }
}
