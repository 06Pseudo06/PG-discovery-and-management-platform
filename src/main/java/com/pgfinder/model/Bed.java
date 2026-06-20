package com.pgfinder.model;

public class Bed {
    private int id;
    private int roomId;
    private String bedLabel;
    private String status; // "vacant" or "occupied"
    private double deposit;

    public Bed() {}

    public Bed(int id, int roomId, String bedLabel, String status, double deposit) {
        this.id = id;
        this.roomId = roomId;
        this.bedLabel = bedLabel;
        this.status = status;
        this.deposit = deposit;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getBedLabel() { return bedLabel; }
    public void setBedLabel(String bedLabel) { this.bedLabel = bedLabel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getDeposit() { return deposit; }
    public void setDeposit(double deposit) { this.deposit = deposit; }
}
