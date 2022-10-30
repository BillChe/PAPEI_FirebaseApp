package com.example.papei_firebaseapp.helpers;

import com.example.papei_firebaseapp.R;

public enum IncidentType {

    EARTHQUAKE(String.valueOf(R.string.earthquake)),
    FLOOD(String.valueOf(R.string.flood)),
    HEAVY_RAIN(String.valueOf(R.string.heavy_rain)),
    FIRE(String.valueOf(R.string.fire));


    private String value;

    IncidentType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }
}
