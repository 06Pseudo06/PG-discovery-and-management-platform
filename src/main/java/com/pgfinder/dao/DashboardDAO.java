package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.pgfinder.model.ActivityItem;
import com.pgfinder.model.ActivityType;
import com.pgfinder.model.ChatPreview;
import com.pgfinder.model.PropertyDashboardCard;

import java.sql.Timestamp;



public class DashboardDAO {

       public Map<String, Integer> getOwnerMetrics(int ownerId) {

    Map<String, Integer> metrics = new HashMap<>();

    metrics.put(
            "total_pgs",
            getSingleCount(
                    "SELECT COUNT(*) FROM pgs WHERE owner_id = ?",
                    ownerId));


    int totalBeds = getSingleCount(
            """
            SELECT COUNT(*)
            FROM beds b
            JOIN rooms r ON b.room_id=r.id
            JOIN pgs p ON r.pg_id=p.id
            WHERE p.owner_id=?
            """,
            ownerId);

    int occupiedBeds = getSingleCount(
        """
        SELECT COUNT(*)
        FROM beds b
        JOIN rooms r ON b.room_id = r.id
        JOIN pgs p ON r.pg_id = p.id
        WHERE p.owner_id = ?
        AND LOWER(b.status) = 'occupied'
        """,
        ownerId);

    metrics.put("occupied_beds", occupiedBeds);



    metrics.put(
            "available_beds",
            Math.max(totalBeds - occupiedBeds, 0));

    metrics.put(
            "pending_requests",
            getSingleCount(
                    """
                    SELECT COUNT(*)
                    FROM booking_requests br
                    JOIN beds b ON br.bed_id=b.id
                    JOIN rooms r ON b.room_id=r.id
                    JOIN pgs p ON r.pg_id=p.id
                    WHERE p.owner_id=?
                    AND br.status='pending'
                    """,
                    ownerId));

    return metrics;
}
    

   public List<ActivityItem> getRecentActivities(int ownerId) {

    List<ActivityItem> activities = new ArrayList<>();

    String sql = """
        SELECT
            activity_text,
            created_at

        FROM activity_logs

        WHERE owner_id = ?

        ORDER BY created_at DESC

        LIMIT 5
        """;

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, ownerId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            String text = rs.getString("activity_text");

            ActivityType type = ActivityType.INFO;

            String lower = text.toLowerCase();

            if (lower.contains("approved")
                    || lower.contains("confirmed")
                    || lower.contains("accepted")) {

                type = ActivityType.SUCCESS;

            } else if (lower.contains("pending")
                    || lower.contains("waiting")) {

                type = ActivityType.WARNING;

            } else if (lower.contains("rejected")
                    || lower.contains("cancelled")) {

                type = ActivityType.ERROR;
            }

            ActivityItem item = new ActivityItem(

                    text,

                    "",

                    rs.getTimestamp("created_at")
                            .toLocalDateTime(),

                    type
            );

            activities.add(item);
        }

    } catch (SQLException e) {

        System.err.println(
                "Error loading dashboard activities: "
                        + e.getMessage());

    }

    return activities;
}

    public List<ChatPreview> getRecentMessages(int ownerId) {

    List<ChatPreview> chats = new ArrayList<>();

    String sql = """
        SELECT
            u.id,
            u.name,
            m.content,
            m.created_at,
            m.is_read

        FROM messages m

        JOIN users u
            ON u.id = m.sender_id

        WHERE m.receiver_id = ?

        ORDER BY m.created_at DESC

        LIMIT 5
        """;

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, ownerId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            Timestamp timestamp = rs.getTimestamp("created_at");

            ChatPreview chat = new ChatPreview(

                    rs.getInt("id"),

                    rs.getString("name"),

                    rs.getString("content"),

                    timestamp.toLocalDateTime(),

                    !rs.getBoolean("is_read")
            );

            chats.add(chat);
        }

    } catch (SQLException e) {

        System.err.println(
                "Error loading dashboard chats: "
                        + e.getMessage());

    }

    return chats;
}

    public List<PropertyDashboardCard> getPropertiesByOwner(int ownerId) {

    List<PropertyDashboardCard> properties = new ArrayList<>();

    String sql = """
        SELECT
            p.id,
            p.name,
            p.address,

            COUNT(DISTINCT b.id) AS total_beds,

            SUM(
    CASE
        WHEN LOWER(b.status)='occupied'
        THEN 1
        ELSE 0
    END
)AS occupied_beds

        FROM pgs p

        LEFT JOIN rooms r
            ON r.pg_id = p.id

        LEFT JOIN beds b
            ON b.room_id = r.id

        LEFT JOIN booking_requests br
            ON br.bed_id = b.id

        WHERE p.owner_id = ?

        GROUP BY
            p.id,
            p.name,
            p.address

        ORDER BY p.name
        """;

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, ownerId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            int totalBeds = rs.getInt("total_beds");
            int occupiedBeds = rs.getInt("occupied_beds");
            int availableBeds = Math.max(0, totalBeds - occupiedBeds);

            String status;

            if (availableBeds == 0) {
                status = "Full";
            } else if (occupiedBeds == 0) {
                status = "Vacant";
            } else {
                status = "Active";
            }

            PropertyDashboardCard property =
                    new PropertyDashboardCard(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            totalBeds,
                            occupiedBeds,
                            availableBeds,
                            status
                    );

            properties.add(property);
        }

    } catch (SQLException e) {

        System.err.println(
                "Error loading owner properties: "
                        + e.getMessage());

    }

    return properties;
}

private int getSingleCount(String sql, int ownerId) {

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {

        ps.setInt(1, ownerId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {

        System.err.println(
                "DashboardDAO Count Error : "
                        + e.getMessage());

    }

    return 0;
}

private int getGlobalCount(String sql) {

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()
    ) {

        if (rs.next()) {
            return rs.getInt(1);
        }

    } catch (SQLException e) {

        System.err.println(
                "DashboardDAO Global Count Error : "
                        + e.getMessage());

    }

    return 0;
}
} 