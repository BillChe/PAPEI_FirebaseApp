package com.example.papei_firebaseapp.ui.incidents;

import android.location.Location;

public class Incident {
    private String userUId;
    String description;
    String date;
   // Location location;
    String type;
    String imageUrl;
    String locationLat;
    String locationLong;

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

    /*public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;

    }*/

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Incident{" +
                "userUId='" + userUId + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                ", location latitude=" + locationLat +
                ", location longitude=" + locationLong +
                ", type='" + type + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(String locationLat) {
        this.locationLat = locationLat;
    }

    public String getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(String locationLong) {
        this.locationLong = locationLong;
    }
}
