package com.pgfinder.model;

import java.time.LocalDate;

public class Tenant {
    private String name;
    private String email;
    private String phone;
    private String pgName;
    private String roomNumber;
    private String roomType;
    private double rentRate;
    private LocalDate moveInDate;
    private LocalDate endDate;

    public Tenant(String name, String email, String phone, String pgName, 
                  String roomNumber, String roomType, double rentRate, 
                  LocalDate moveInDate, LocalDate endDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.pgName = pgName;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.rentRate = rentRate;
        this.moveInDate = moveInDate;
        this.endDate = endDate;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPgName() { return pgName; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getRentRate() { return rentRate; }
    public LocalDate getMoveInDate() { return moveInDate; }
    public LocalDate getEndDate() { return endDate; }
}