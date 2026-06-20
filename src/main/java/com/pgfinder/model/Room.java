package com.pgfinder.model;

public class Room {
    private int id;
    private int pgId;
    private String roomNumber;
    private String roomType;
    private double rent;

    public Room() {}

    public Room(int id, int pgId, String roomNumber, String roomType, double rent) {
        this.id = id;
        this.pgId = pgId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.rent = rent;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPgId() { return pgId; }
    public void setPgId(int pgId) { this.pgId = pgId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public double getRent() { return rent; }
    public void setRent(double rent) { this.rent = rent; }
}
