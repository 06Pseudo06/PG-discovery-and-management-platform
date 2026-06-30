package com.pgfinder.model;

import java.time.LocalDateTime;

public class Review {
    private int id;
    private int verificationId;
    private int pgId;
    private int foodRating;
    private int cleanlinessRating;
    private int wifiRating;
    private int ownerBehaviorRating;
    private String comment;
    private LocalDateTime createdAt;
    private String studentName;
    private String pgName;

    public Review() {}

    public Review(int id, int verificationId, int pgId, int foodRating, int cleanlinessRating, int wifiRating, int ownerBehaviorRating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.verificationId = verificationId;
        this.pgId = pgId;
        this.foodRating = foodRating;
        this.cleanlinessRating = cleanlinessRating;
        this.wifiRating = wifiRating;
        this.ownerBehaviorRating = ownerBehaviorRating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.studentName = studentName;
        this.pgName = pgName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVerificationId() { return verificationId; }
    public void setVerificationId(int verificationId) { this.verificationId = verificationId; }

    public int getPgId() { return pgId; }
    public void setPgId(int pgId) { this.pgId = pgId; }

    public int getFoodRating() { return foodRating; }
    public void setFoodRating(int foodRating) { this.foodRating = foodRating; }

    public int getCleanlinessRating() { return cleanlinessRating; }
    public void setCleanlinessRating(int cleanlinessRating) { this.cleanlinessRating = cleanlinessRating; }

    public int getWifiRating() { return wifiRating; }
    public void setWifiRating(int wifiRating) { this.wifiRating = wifiRating; }

    public int getOwnerBehaviorRating() { return ownerBehaviorRating; }
    public void setOwnerBehaviorRating(int ownerBehaviorRating) { this.ownerBehaviorRating = ownerBehaviorRating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getPgName() { return pgName; }
    public void setPgName(String pgName) { this.pgName = pgName; }
}
