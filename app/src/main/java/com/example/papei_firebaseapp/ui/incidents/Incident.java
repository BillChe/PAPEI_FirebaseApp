package com.example.papei_firebaseapp.ui.incidents;

import android.location.Location;

public class Incident {
    private String userUId;
    String description;
    String date;
    Location location;

    public Incident() {
    }

    public Incident(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserUId() {
        return userUId;
    }

    public void setUserUId(String userUId) {
        this.userUId = userUId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
