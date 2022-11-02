package com.example.papei_firebaseapp.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.data.viewmodels.MainViewModel;
import com.example.papei_firebaseapp.helpers.ListAdapter;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.example.papei_firebaseapp.ui.login.LoginActivity;
import com.example.papei_firebaseapp.ui.main.MainActivity;
import com.example.papei_firebaseapp.ui.register.RegisterForm;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AllProblems extends AppCompatActivity {
    private boolean showUser = false;
    ListView listView;
    ArrayList<Incident> arrayList = new ArrayList<>();
    ListAdapter arrayAdapter ;
    Button viewOnMap;
    boolean problemsExist= false;
    public static ArrayList<Incident> markersArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_problems);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Incidents");

        //set views and listener for button
        viewOnMap = (Button) findViewById(R.id.viewOnMap);
        if(getIntent().getStringExtra("user").equals("yes"))
        {
            showUser = true;
        }
        getAllProblems();
        viewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(problemsExist)
                intentToMapsView();
            }
        });
    }



    private void intentToMapsView() {
        Intent registerIntent = new Intent(AllProblems.this, MapsActivity.class);
        startActivity(registerIntent);
        AllProblems.this.finish();
    }

    private void getAllProblems() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Incidents");
        listView = (ListView) findViewById(R.id.problemsListView);
        arrayAdapter = new ListAdapter(this, R.layout.itemlist,arrayList);
        listView.setAdapter(arrayAdapter);
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Incident incidentTemp = snapshot.getValue(Incident.class);
                //UPDATE INCIDENT ID TO HAVE THEM DISTINCT BY KEY
                String incidentUid =  snapshot.getKey();
                incidentTemp.setIncidentUid(incidentUid);
                //show all results for Admin
                if(!showUser && MainViewModel.getIsAdmin())
                {
                    arrayList.add(incidentTemp);
                    arrayAdapter.notifyDataSetChanged();
                    //fill markers array with incidents
                    markersArray.add(incidentTemp);
                    problemsExist = true;
                }
                //show only verified incidents for simple User
                else if(!showUser && !MainViewModel.getIsAdmin())
                {
                    if(incidentTemp.isCheckedByAdmin())
                    {
                        arrayList.add(incidentTemp);
                        arrayAdapter.notifyDataSetChanged();
                        markersArray.add(incidentTemp);
                        problemsExist = true;
                    }
                }
                else
                {
                    //show only user related incidents for User
                    if(incidentTemp.getUserUId().equals(FirebaseAuth.getInstance().getUid()))
                    {
                        arrayList.add(incidentTemp);
                        arrayAdapter.notifyDataSetChanged();
                        markersArray.add(incidentTemp);
                        problemsExist = true;
                    }
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                arrayAdapter.clear();
                markersArray.clear();
                getAllProblems();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent loginIntent = new Intent(AllProblems.this, MainActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent loginIntent = new Intent(AllProblems.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
        finish();
    }
}