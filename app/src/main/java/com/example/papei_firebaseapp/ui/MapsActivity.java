package com.example.papei_firebaseapp.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.example.papei_firebaseapp.ui.main.MainActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button filter;
    String incidentType = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        filter = (Button) findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] problemTypes = {"Road Problem", "Illegal Parking", "Garbage Problem", "Building Problem"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Please choose a problem category");
                builder.setItems(problemTypes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // the user clicked on colors[which]
                        if( problemTypes[which].equals("Road Problem"))
                        {
                            incidentType = "Road Problem";
                        }
                        else if ( problemTypes[which].equals("Illegal Parking"))
                        {
                            incidentType = "Illegal Parking";
                        }
                        else if ( problemTypes[which].equals("Garbage Problem"))
                        {
                            incidentType = "Garbage Problem";
                        }
                        else if ( problemTypes[which].equals("Building Problem"))
                        {
                            incidentType = "Building Problem";
                        }

                        filterProblems();
                    }
                });
                builder.show();
            }
        });
    }

    private void filterProblems() {
        if(mMap!=null)
        {
            mMap.clear();
        }
        LatLng last = null;
        ArrayList<Incident> markersArray = AllProblems.markersArray;


        if(incidentType.length()>0){
            for(int i = 0 ; i < markersArray.size() ; i++) {
                if(incidentType.equals(markersArray.get(i).getType()))
                {
                    createMarker(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()), markersArray.get(i).getType(),
                            markersArray.get(i).getDescription()+" "+
                                    markersArray.get(i).getDate());
                    last= new LatLng(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(last));
                }

            }
        }
        else
        {
            for(int i = 0 ; i < markersArray.size() ; i++) {

                createMarker(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()), markersArray.get(i).getType(),
                        markersArray.get(i).getDescription()+" "+
                                markersArray.get(i).getDate());
                last= new LatLng(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(last));
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng last = null;
        ArrayList<Incident> markersArray = AllProblems.markersArray;
        for(int i = 0 ; i < markersArray.size() ; i++) {

            createMarker(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()), markersArray.get(i).getType(),
                    markersArray.get(i).getDescription()+" "+
                    markersArray.get(i).getDate());
            last= new LatLng(Double.parseDouble(markersArray.get(i).getLocationLat()), Double.parseDouble(markersArray.get(i).getLocationLong()));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(last));

    }

    protected Marker createMarker(double latitude, double longitude, String title, String snippet) {

        return mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet));
    }
}