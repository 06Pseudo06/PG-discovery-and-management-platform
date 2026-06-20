package com.pgfinder.model;

public class PG {
    private int id;
    private int ownerId;
    private String name;
    private String address;
    private String city;
    private String area;
    private String description;
    private String genderPreference; // "male", "female", "any"
    private boolean foodAvailable;
    private boolean wifiAvailable;

    public PG() {}

    public PG(int id, int ownerId, String name, String address, String city, String area, String description, String genderPreference, boolean foodAvailable, boolean wifiAvailable) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.area = area;
        this.description = description;
        this.genderPreference = genderPreference;
        this.foodAvailable = foodAvailable;
        this.wifiAvailable = wifiAvailable;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGenderPreference() { return genderPreference; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }

    public boolean isFoodAvailable() { return foodAvailable; }
    public void setFoodAvailable(boolean foodAvailable) { this.foodAvailable = foodAvailable; }

    public boolean isWifiAvailable() { return wifiAvailable; }
    public void setWifiAvailable(boolean wifiAvailable) { this.wifiAvailable = wifiAvailable; }
}
