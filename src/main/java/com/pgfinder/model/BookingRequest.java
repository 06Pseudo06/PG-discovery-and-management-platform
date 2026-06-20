package com.pgfinder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingRequest {
    private int id;
    private int studentId;
    private int bedId;
    private String status; // "pending", "approved", "rejected"
    private LocalDate requestedMoveInDate;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt; // Nullable

    public BookingRequest() {}

    public BookingRequest(int id, int studentId, int bedId, String status, LocalDate requestedMoveInDate, LocalDateTime createdAt, LocalDateTime decidedAt) {
        this.id = id;
        this.studentId = studentId;
        this.bedId = bedId;
        this.status = status;
        this.requestedMoveInDate = requestedMoveInDate;
        this.createdAt = createdAt;
        this.decidedAt = decidedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getBedId() { return bedId; }
    public void setBedId(int bedId) { this.bedId = bedId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getRequestedMoveInDate() { return requestedMoveInDate; }
    public void setRequestedMoveInDate(LocalDate requestedMoveInDate) { this.requestedMoveInDate = requestedMoveInDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDecidedAt() { return decidedAt; }
    public void setDecidedAt(LocalDateTime decidedAt) { this.decidedAt = decidedAt; }
}
