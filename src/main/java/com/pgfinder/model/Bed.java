package com.pgfinder.model;

public class Bed {

    private int id;
    private int roomId;
    private String bedLabel;
    private String status;
    private double deposit;

    public Bed() {
    }

    public Bed(int id, int roomId, String bedLabel, String status, double deposit) {
        this.id = id;
        this.roomId = roomId;
        this.bedLabel = bedLabel;
        this.status = status;
        this.deposit = deposit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getBedLabel() {
        return bedLabel;
    }

    public void setBedLabel(String bedLabel) {
        this.bedLabel = bedLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {

        if (status == null) {
            this.status = "vacant";
            return;
        }

        switch (status.toLowerCase()) {

            case "vacant":
            case "occupied":
                this.status = status.toLowerCase();
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid bed status: " + status
                );
        }
    }

    public double getDeposit() {
        return deposit;
    }

    public void setDeposit(double deposit) {

        if (deposit < 0) {
            throw new IllegalArgumentException("Deposit cannot be negative.");
        }

        this.deposit = deposit;
    }

    public boolean isVacant() {
        return "vacant".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "occupied".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return bedLabel;
    }
}