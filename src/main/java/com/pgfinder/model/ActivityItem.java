package com.pgfinder.model;

import java.time.LocalDateTime;

public class ActivityItem {

    private String title;
    private String description;
    private LocalDateTime createdAt;
    private ActivityType type;

    public ActivityItem() {
    }

    public ActivityItem(String title,
                        String description,
                        LocalDateTime createdAt,
                        ActivityType type) {

        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }
}