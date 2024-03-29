package com.example.papei_firebaseapp.data.viewmodels;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Environment;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModel;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.ui.AllProblems;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.example.papei_firebaseapp.ui.login.LoginActivity;
import com.example.papei_firebaseapp.ui.register.RegisterForm;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainViewModel extends ViewModel {
    //Personal Email for Admin
    public static String emailAmin = "vasos.red.7@gmail.com";
    //helper boolean
    public static boolean isAdmin = false;

    private String username;
    private Location location;
    Context context;
    Incident incident;
    String incidentType="";
    String incidentDescription = "";

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

    public void viewAllIncidents()
    {
        Intent viewProblemsIntent = new Intent(context, AllProblems.class);
        viewProblemsIntent.putExtra("user","no");
        context.startActivity(viewProblemsIntent);
    }

    public void viewUserIncidents()
    {
        Intent viewProblemsIntent = new Intent(context, AllProblems.class);
        viewProblemsIntent.putExtra("user","yes");
        context.startActivity(viewProblemsIntent);
    }

    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {

        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/sdcard/DirName/");
            wallpaperDirectory.mkdirs();
        }

        File file = new File("/sdcard/DirName/", fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public static boolean getIsAdmin() {
        return isAdmin;
    }

    public static void setIsAdmin(boolean isAdmin) {
        MainViewModel.isAdmin = isAdmin;
    }


}
