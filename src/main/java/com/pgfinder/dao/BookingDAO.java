package com.pgfinder.dao;

import com.pgfinder.config.DBConnection;
import com.pgfinder.model.Booking;
import com.pgfinder.model.BookingDetail;
import com.pgfinder.model.BookingStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    private static final String DETAIL_SELECT = """
            SELECT br.id AS booking_id,
                   br.student_id,
                   u.name AS student_name,
                   u.email AS student_email,
                   u.phone AS student_phone,
                   br.bed_id,
                   b.bed_label,
                   b.deposit,
                   r.id AS room_id,
                   r.room_number,
                   r.room_type,
                   r.rent,
                   p.id AS pg_id,
                   p.name AS pg_name,
                   p.address AS pg_address,
                   p.area AS pg_area,
                   p.city AS pg_city,
                   p.owner_id,
                   owner.name AS owner_name,
                   owner.phone AS owner_phone,
                   br.status,
                   br.requested_move_in_date,
                   br.end_date,
                   br.owner_remarks,
                   br.student_notes,
                   br.created_at,
                   br.decided_at
            FROM booking_requests br
            JOIN users u ON br.student_id = u.id
            JOIN beds b ON br.bed_id = b.id
            JOIN rooms r ON b.room_id = r.id
            JOIN pgs p ON r.pg_id = p.id
            JOIN users owner ON p.owner_id = owner.id
            """;

    public int createBooking(Connection conn, Booking booking) throws SQLException {
        String sql = """
                INSERT INTO booking_requests
                (student_id, bed_id, status, requested_move_in_date, end_date, student_notes)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getStudentId());
            ps.setInt(2, booking.getBedId());
            ps.setString(3, booking.getStatus());
            ps.setDate(4, Date.valueOf(booking.getStartDate()));
            if (booking.getEndDate() != null) {
                ps.setDate(5, Date.valueOf(booking.getEndDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, booking.getStudentNotes());

            if (ps.executeUpdate() == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    booking.setId(id);
                    return id;
                }
            }
        }
        return -1;
    }

    public Booking findById(int id) {
        String sql = "SELECT * FROM booking_requests WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding booking: " + id, e);
        }
        return null;
    }

    public Booking findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM booking_requests WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBooking(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> findByStudent(int studentId) {
        String sql = """
                SELECT *
                FROM booking_requests
                WHERE student_id = ?
                ORDER BY created_at DESC
                """;
        return queryBookings(sql, studentId);
    }

    public List<BookingDetail> findDetailsByStudent(int studentId) {
        String sql = DETAIL_SELECT + " WHERE br.student_id = ? ORDER BY br.created_at DESC";
        return queryBookingDetails(sql, studentId);
    }

    public List<BookingDetail> findByOwner(int ownerId) {
        String sql = DETAIL_SELECT + " WHERE p.owner_id = ? ORDER BY br.created_at DESC";
        return queryBookingDetails(sql, ownerId);
    }

    public List<BookingDetail> findPendingRequests(int ownerId) {
        String sql = DETAIL_SELECT
                + " WHERE p.owner_id = ? AND br.status = ? ORDER BY br.created_at ASC";
        List<BookingDetail> details = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ps.setString(2, BookingStatus.PENDING_OWNER);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapRowToDetail(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding pending booking requests.", e);
        }
        return details;
    }

    public BookingDetail findDetailById(int bookingId) {
        String sql = DETAIL_SELECT + " WHERE br.id = ?";
        List<BookingDetail> details = queryBookingDetails(sql, bookingId);
        return details.isEmpty() ? null : details.get(0);
    }

    public boolean approveBooking(Connection conn, int bookingId, String ownerRemarks) throws SQLException {
        return updateStatusWithRemarks(conn, bookingId, BookingStatus.AWAITING_STUDENT_CONFIRMATION, ownerRemarks);
    }

    public boolean confirmBooking(Connection conn, int bookingId) throws SQLException {
        return updateStatusWithRemarks(conn, bookingId, BookingStatus.CONFIRMED, null);
    }

    public boolean rejectBooking(Connection conn, int bookingId, String ownerRemarks) throws SQLException {
        return updateStatusWithRemarks(conn, bookingId, BookingStatus.REJECTED, ownerRemarks);
    }

    public boolean cancelBooking(Connection conn, int bookingId) throws SQLException {
        return updateStatusWithRemarks(conn, bookingId, BookingStatus.CANCELLED, null);
    }

    public int rejectPendingForBedExcept(Connection conn, int bedId, int approvedBookingId) throws SQLException {
        String sql = """
                UPDATE booking_requests
                SET status = ?,
                    decided_at = CURRENT_TIMESTAMP,
                    owner_remarks = ?
                WHERE bed_id = ?
                AND status = ?
                AND id <> ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, BookingStatus.REJECTED);
            ps.setString(2, "Automatically rejected — another request was approved for this bed.");
            ps.setInt(3, bedId);
            ps.setString(4, BookingStatus.PENDING_OWNER);
            ps.setInt(5, approvedBookingId);
            return ps.executeUpdate();
        }
    }

    public List<BookingDetail> findPendingByBedExcept(Connection conn, int bedId, int exceptBookingId)
            throws SQLException {
        String sql = DETAIL_SELECT
                + " WHERE br.bed_id = ? AND br.status = ? AND br.id <> ?";
        List<BookingDetail> details = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bedId);
            ps.setString(2, BookingStatus.PENDING_OWNER);
            ps.setInt(3, exceptBookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapRowToDetail(rs));
                }
            }
        }
        return details;
    }

    public boolean existsActiveBookingForBed(Connection conn, int bedId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM booking_requests
                WHERE bed_id = ?
                AND status = ?
                """;
        return countByQuery(conn, sql, bedId, BookingStatus.CONFIRMED) > 0;
    }

    public boolean existsActiveBookingForBed(int bedId) {
        try (Connection conn = DBConnection.getConnection()) {
            return existsActiveBookingForBed(conn, bedId);
        } catch (SQLException e) {
            throw new RuntimeException("Error checking active booking for bed: " + bedId, e);
        }
    }

    public boolean existsStudentBooking(Connection conn, int studentId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM booking_requests
                WHERE student_id = ?
                AND status IN (?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, BookingStatus.PENDING_OWNER);
            ps.setString(3, BookingStatus.AWAITING_STUDENT_CONFIRMATION);
            ps.setString(4, BookingStatus.CONFIRMED);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existsStudentBooking(int studentId) {
        try (Connection conn = DBConnection.getConnection()) {
            return existsStudentBooking(conn, studentId);
        } catch (SQLException e) {
            throw new RuntimeException("Error checking student booking: " + studentId, e);
        }
    }

    public boolean existsPendingBookingForBedAndStudent(Connection conn, int bedId, int studentId)
            throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM booking_requests
                WHERE bed_id = ?
                AND student_id = ?
                AND status = ?
                """;
        return countByQuery(conn, sql, bedId, studentId, BookingStatus.PENDING_OWNER) > 0;
    }

    public int countBookings(int ownerId, String status) {
        String sql = """
                SELECT COUNT(*)
                FROM booking_requests br
                JOIN beds b ON br.bed_id = b.id
                JOIN rooms r ON b.room_id = r.id
                JOIN pgs p ON r.pg_id = p.id
                WHERE p.owner_id = ?
                """;
        if (status != null) {
            sql += " AND br.status = ?";
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            if (status != null) {
                ps.setString(2, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting bookings.", e);
        }
        return 0;
    }

    public int countPendingForOwner(int ownerId) {
        return countBookings(ownerId, BookingStatus.PENDING_OWNER);
    }

    public List<BookingDetail> findAwaitingConfirmationForStudent(int studentId) {
        String sql = DETAIL_SELECT
                + " WHERE br.student_id = ? AND br.status = ? ORDER BY br.created_at DESC";
        List<BookingDetail> details = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, BookingStatus.AWAITING_STUDENT_CONFIRMATION);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapRowToDetail(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding awaiting confirmation bookings.", e);
        }
        return details;
    }

    public boolean hasConfirmedBookingForBed(Connection conn, int bedId) throws SQLException {
        return existsActiveBookingForBed(conn, bedId);
    }

    public boolean hasAwaitingConfirmationForBed(Connection conn, int bedId) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM booking_requests
                WHERE bed_id = ?
                AND status = ?
                """;
        return countByQuery(conn, sql, bedId, BookingStatus.AWAITING_STUDENT_CONFIRMATION) > 0;
    }

    private boolean updateStatusWithRemarks(Connection conn, int bookingId, String status, String ownerRemarks)
            throws SQLException {
        String sql = """
                UPDATE booking_requests
                SET status = ?,
                    owner_remarks = ?,
                    decided_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, ownerRemarks);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        }
    }

    private List<Booking> queryBookings(String sql, int param) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRowToBooking(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying bookings.", e);
        }
        return bookings;
    }

    private List<BookingDetail> queryBookingDetails(String sql, int param) {
        List<BookingDetail> details = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapRowToDetail(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error querying booking details.", e);
        }
        return details;
    }

    private int countByQuery(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                if (param instanceof Integer) {
                    ps.setInt(i + 1, (Integer) param);
                } else if (param instanceof String) {
                    ps.setString(i + 1, (String) param);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private Booking mapRowToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setStudentId(rs.getInt("student_id"));
        booking.setBedId(rs.getInt("bed_id"));
        booking.setStatus(rs.getString("status"));
        booking.setStartDate(rs.getDate("requested_move_in_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            booking.setEndDate(endDate.toLocalDate());
        }
        booking.setOwnerRemarks(rs.getString("owner_remarks"));
        booking.setStudentNotes(rs.getString("student_notes"));
        booking.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp decidedAt = rs.getTimestamp("decided_at");
        if (decidedAt != null) {
            booking.setDecidedAt(decidedAt.toLocalDateTime());
        }
        return booking;
    }

    private BookingDetail mapRowToDetail(ResultSet rs) throws SQLException {
        BookingDetail detail = new BookingDetail();
        detail.setBookingId(rs.getInt("booking_id"));
        detail.setStudentId(rs.getInt("student_id"));
        detail.setStudentName(rs.getString("student_name"));
        detail.setStudentEmail(rs.getString("student_email"));
        detail.setStudentPhone(rs.getString("student_phone"));
        detail.setBedId(rs.getInt("bed_id"));
        detail.setBedLabel(rs.getString("bed_label"));
        detail.setDeposit(rs.getDouble("deposit"));
        detail.setRoomId(rs.getInt("room_id"));
        detail.setRoomNumber(rs.getString("room_number"));
        detail.setRoomType(rs.getString("room_type"));
        detail.setRent(rs.getDouble("rent"));
        detail.setPgId(rs.getInt("pg_id"));
        detail.setPgName(rs.getString("pg_name"));
        detail.setPgAddress(rs.getString("pg_address"));
        detail.setPgArea(rs.getString("pg_area"));
        detail.setPgCity(rs.getString("pg_city"));
        detail.setOwnerId(rs.getInt("owner_id"));
        detail.setOwnerName(rs.getString("owner_name"));
        detail.setOwnerPhone(rs.getString("owner_phone"));
        detail.setStatus(rs.getString("status"));
        detail.setStartDate(rs.getDate("requested_move_in_date").toLocalDate());
        Date endDate = rs.getDate("end_date");
        if (endDate != null) {
            detail.setEndDate(endDate.toLocalDate());
        }
        detail.setOwnerRemarks(rs.getString("owner_remarks"));
        detail.setStudentNotes(rs.getString("student_notes"));
        detail.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp decidedAt = rs.getTimestamp("decided_at");
        if (decidedAt != null) {
            detail.setDecidedAt(decidedAt.toLocalDateTime());
        }
        return detail;
    }
}
