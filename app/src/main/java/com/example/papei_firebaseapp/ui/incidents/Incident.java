package com.example.papei_firebaseapp.ui.incidents;

public class Incident {
    String description;

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
}
