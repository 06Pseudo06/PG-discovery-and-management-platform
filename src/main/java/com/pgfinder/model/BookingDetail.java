package com.pgfinder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Enriched booking view joining student, PG, room, and bed data for UI display.
 */
public class BookingDetail {

    private int bookingId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private int bedId;
    private String bedLabel;
    private double deposit;
    private int roomId;
    private String roomNumber;
    private String roomType;
    private double rent;
    private int pgId;
    private String pgName;
    private String pgAddress;
    private String pgArea;
    private String pgCity;
    private int ownerId;
    private String ownerName;
    private String ownerPhone;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String ownerRemarks;
    private String studentNotes;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public int getBedId() {
        return bedId;
    }

    public void setBedId(int bedId) {
        this.bedId = bedId;
    }

    public String getBedLabel() {
        return bedLabel;
    }

    public void setBedLabel(String bedLabel) {
        this.bedLabel = bedLabel;
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public double getRent() {
        return rent;
    }

    public void setRent(double rent) {
        this.rent = rent;
    }

    public int getPgId() {
        return pgId;
    }

    public void setPgId(int pgId) {
        this.pgId = pgId;
    }

    public String getPgName() {
        return pgName;
    }

    public void setPgName(String pgName) {
        this.pgName = pgName;
    }

    public String getPgAddress() {
        return pgAddress;
    }

    public void setPgAddress(String pgAddress) {
        this.pgAddress = pgAddress;
    }

    public String getPgArea() {
        return pgArea;
    }

    public void setPgArea(String pgArea) {
        this.pgArea = pgArea;
    }

    public String getPgCity() {
        return pgCity;
    }

    public void setPgCity(String pgCity) {
        this.pgCity = pgCity;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getOwnerRemarks() {
        return ownerRemarks;
    }

    public void setOwnerRemarks(String ownerRemarks) {
        this.ownerRemarks = ownerRemarks;
    }

    public String getStudentNotes() {
        return studentNotes;
    }

    public void setStudentNotes(String studentNotes) {
        this.studentNotes = studentNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
