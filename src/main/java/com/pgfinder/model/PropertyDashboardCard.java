package com.pgfinder.model;

public class PropertyDashboardCard {

    private int id;

    private String name;

    private String address;

    private int totalBeds;

    private int occupiedBeds;

    private int availableBeds;

    private String status;

    public PropertyDashboardCard() {
    }

    public PropertyDashboardCard(
            int id,
            String name,
            String address,
            int totalBeds,
            int occupiedBeds,
            int availableBeds,
            String status) {

        this.id = id;
        this.name = name;
        this.address = address;
        this.totalBeds = totalBeds;
        this.occupiedBeds = occupiedBeds;
        this.availableBeds = availableBeds;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getTotalBeds() {
        return totalBeds;
    }

    public void setTotalBeds(int totalBeds) {
        this.totalBeds = totalBeds;
    }

    public int getOccupiedBeds() {
        return occupiedBeds;
    }

    public void setOccupiedBeds(int occupiedBeds) {
        this.occupiedBeds = occupiedBeds;
    }

    public int getAvailableBeds() {
        return availableBeds;
    }

    public void setAvailableBeds(int availableBeds) {
        this.availableBeds = availableBeds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 