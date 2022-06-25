package com.example.papei_firebaseapp.data.viewmodels;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.example.papei_firebaseapp.ui.login.LoginActivity;
import com.example.papei_firebaseapp.ui.register.RegisterForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainViewModel extends ViewModel {
    private String username;
    private Location location;
    Context context;

    public MainViewModel() {
    }

    public MainViewModel(Context context) {
        this.context = context;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void logout()
    {
        //sign out firebase user
        FirebaseAuth.getInstance().signOut();
        //intent to Login Activity
        Intent registerIntent = new Intent(context, LoginActivity.class);
        registerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(registerIntent);
    }

    public void report()
    {

        Incident incident = new Incident();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String dateNow = dateFormat.format(date).toString();
        //Adding values
        incident.setDescription("testIncident");
        incident.setDate(dateNow);
        incident.setLocation(getLocation());
        incident.setUserUId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("Incidents")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).push()
                .setValue(incident)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(context, context.getString(R.string.reported_incident_success),
                                    Toast.LENGTH_LONG).show();
                            //todo redirect to Login Screen !!!!
                        }
                        else
                        {
                            Toast.makeText(context, context.getString(R.string.reported_incident__fail),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
