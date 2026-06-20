package com.pgfinder.model;

import java.time.LocalDateTime;

public class Verification {
    private int id;
    private int bookingId;
    private String code;
    private LocalDateTime generatedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt; // Nullable
    private String status; // "pending", "confirmed", "expired"

    public Verification() {}

    public Verification(int id, int bookingId, String code, LocalDateTime generatedAt, LocalDateTime expiresAt, LocalDateTime confirmedAt, String status) {
        this.id = id;
        this.bookingId = bookingId;
        this.code = code;
        this.generatedAt = generatedAt;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
