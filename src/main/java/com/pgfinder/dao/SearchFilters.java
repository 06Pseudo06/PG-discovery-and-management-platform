package com.pgfinder.dao;

public class SearchFilters {
    private String city;
    private Double minBudget;
    private Double maxBudget;
    private String gender; // "male", "female", "any"
    private Boolean foodRequired;
    private Boolean wifiRequired;

    public SearchFilters() {}

    public SearchFilters(String city, Double minBudget, Double maxBudget, String gender, Boolean foodRequired, Boolean wifiRequired) {
        this.city = city;
        this.minBudget = minBudget;
        this.maxBudget = maxBudget;
        this.gender = gender;
        this.foodRequired = foodRequired;
        this.wifiRequired = wifiRequired;
    }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getMinBudget() { return minBudget; }
    public void setMinBudget(Double minBudget) { this.minBudget = minBudget; }

    public Double getMaxBudget() { return maxBudget; }
    public void setMaxBudget(Double maxBudget) { this.maxBudget = maxBudget; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Boolean getFoodRequired() { return foodRequired; }
    public void setFoodRequired(Boolean foodRequired) { this.foodRequired = foodRequired; }

    public Boolean getWifiRequired() { return wifiRequired; }
    public void setWifiRequired(Boolean wifiRequired) { this.wifiRequired = wifiRequired; }
}
