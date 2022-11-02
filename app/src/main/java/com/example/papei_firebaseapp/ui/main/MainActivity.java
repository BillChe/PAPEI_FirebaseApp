package com.example.papei_firebaseapp.ui.main;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.papei_firebaseapp.R;
import com.example.papei_firebaseapp.data.User;
import com.example.papei_firebaseapp.data.viewmodels.MainViewModel;
import com.example.papei_firebaseapp.databinding.ActivityMainBinding;
import com.example.papei_firebaseapp.ui.incidents.Incident;
import com.example.papei_firebaseapp.ui.login.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.output.ByteArrayOutputStream;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LocationListener {
    MainViewModel vm;
    ActivityMainBinding binding;
    //drawer layout
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private static final int PERMISSIONS_REQUEST = 100;
    LocationManager locationManager;
    FusedLocationProviderClient fusedLocationClient;
    public static Location currentLocation;
    String provider;
    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;
    String lat;
    public Button report;
    private String username;
    private Location location;
    Context context;
    Incident incident;
    String incidentType="";
    String incidentDescription = "";
    Uri filePath;
    // instance for firebase storage and StorageReference
    FirebaseStorage storage;
    StorageReference storageReference;
    String imageUrl ="";
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        vm = new MainViewModel(MainActivity.this);

        binding.setVm(vm);
        context = MainActivity.this;
        //check email on firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null&&user.isEmailVerified()) {
            if (user.getEmail().equals(MainViewModel.emailAmin)) {
                MainViewModel.setIsAdmin(true);
            }
        }
        report = findViewById(R.id.report);
        usernameTextView = findViewById(R.id.usernameText);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser == null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

            }

        };

        // get the Firebase  storage reference
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildSelectTypeMessage();
            }
        });

    }

    //checking for updated Incidents for User Mode
    private void checkForIncidents() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Incidents");
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Incident incidentTemp = snapshot.getValue(Incident.class);
                //first we check if is verified by an Admin
                //check only verified and close to User location problems to notify!
                if(currentLocation!=null)
                {

                //todo watch out set here the key value as temp incident's id
                String id = snapshot.getKey();
                if(incidentTemp.isCheckedByAdmin() && checkLocation(incidentTemp) &&
                        !incidentTemp.getNotifiedUsersId().contains(FirebaseAuth.getInstance().getUid())
                        && !MainViewModel.getIsAdmin()
                )
                {
                    //get city name here with Geocoder
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = null;
                    String cityName = "";//addresses.get(0).getAddressLine(0);
                    String stateName = "";//addresses.get(0).getAddressLine(1);
                    String countryName = "";//addresses.get(0).getAddressLine(2);
                    try {
                        addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                        cityName = addresses.get(0).getAddressLine(0);
                        countryName = addresses.get(0).getAddressLine(2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String descriptionNotification = incidentTemp.getDescription()!=null?incidentTemp.getDescription():
                            incidentTemp.getType()+ "Incident at";
                    //show a notification to alert user for near Incident
                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(MainActivity.this)
                            .setColor(getResources().getColor(R.color.transp))
                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.papeiimg))
                            .setSmallIcon(R.drawable.papeiimg) // notification icon
                            .setStyle(new NotificationCompat.BigTextStyle().bigText( descriptionNotification
                                    +" "+ getString(R.string.at)+" " + cityName  ))
                            .setContentTitle(incidentTemp.getType() ) // title for notification
                            .setVibrate(new long[] { 500, 500, 500, 500, 500 })// add vibration
                            .setAutoCancel(true); // clear notification after click
                    //add sound
                    try {
                        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        Ringtone r = RingtoneManager.getRingtone(context, uri);
                        r.play();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(0, mBuilder.build());
                    Map<String, Object> updates = new HashMap<String,Object>();
                    updates.put("notifiedUsersId", FirebaseAuth.getInstance().getUid());
                    //todo watch out correct id update here logic is that the incident uid is null upon creation!!
                    database.child(id).updateChildren(updates);

                }

                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        ref.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User currentUser = dataSnapshot.child(FirebaseAuth.getInstance().getUid()).getValue(User.class);
                        //Get map of users in datasnapshot
                        username = collectUsername((Map<String,Object>) dataSnapshot.getValue());
                        usernameTextView.setText("Welcome "+ username);
                        vm.setUsername(username);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });


    }

    private boolean checkLocation(Incident incidentTemp) {
        Location locationA = new Location("point A");

        locationA.setLatitude(currentLocation.getLatitude());
        locationA.setLongitude(currentLocation.getLongitude());

        Location locationB = new Location("point B");

        locationB.setLatitude(Double.parseDouble(incidentTemp.getLocationLat()));
        locationB.setLongitude(Double.parseDouble(incidentTemp.getLocationLong()));

        float distance = locationA.distanceTo(locationB);
        if(distance<4000)
        {
            return true;
        }
        return false;

    }

    private boolean checkAndRequestPermissions() {
        int permissionSendMessage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionSendMessage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 100);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.logoutBtn:
                //sign out firebase user
                FirebaseAuth.getInstance().signOut();
                //intent to Login Activity
                Intent registerIntent = new Intent(MainActivity.this, LoginActivity.class);
                registerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(registerIntent);
                //vm.logout();
                finish();
            default:

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
            //todo start service to take location and show notification and store lat and long and location to send it to firebase with get last location

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fetchLastLocation();

        }
        if(!MainViewModel.getIsAdmin())
        checkForIncidents();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 100: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "camera & location services permission granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            buildAlertMessageNoGps();
                        }
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                            showDialogOK("Camera and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();


            uploadImage();

        }
        if(requestCode == 2 && resultCode == RESULT_OK){

            filePath = data.getData();
            // Setting image on image view using Bitmap
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
           /* Uri tempUri = getImageUri(getApplicationContext(), bitmap);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));*/
            uploadImageFromCamera(bitmap);


        }
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        Bitmap OutImage = Bitmap.createScaledBitmap(inImage, 1000, 1000,true);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), OutImage, "Title", null);
        return Uri.parse(path);
    }

    private void uploadImageFromCamera(Bitmap bitmap) {
        // Code for showing progressDialog while uploading
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference ref
                = storageReference
                .child(
                        "images/"
                                + UUID.randomUUID().toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        imageUrl = uri.toString();
                        Log.i("image uri vasilis", imageUrl);
                        report();
                        progressDialog.dismiss();
                    }
                });


            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {

        lat = "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));


                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void fetchLastLocation() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            currentLocation = location;
                            vm.setLocation(location);
                            Log.e("LAST LOCATION: ", location.toString());
                        }
                    }
                });

    }
    public void buildSelectTypeMessage() {
        String[] problemTypes = {getString(R.string.fire), getString(R.string.earthquake), getString(R.string.flood),
                getString(R.string.heavy_rain),getString(R.string.snow_storm)};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Please choose a problem category");
        builder.setItems(problemTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // the user clicked on colors[which]
                if( problemTypes[which].equals(getString(R.string.earthquake)))
                {
                    incidentType = getString(R.string.earthquake);
                }
                else if ( problemTypes[which].equals(getString(R.string.flood)))
                {
                    incidentType = getString(R.string.flood);
                }
                else if ( problemTypes[which].equals(getString(R.string.fire)))
                {
                    incidentType = getString(R.string.fire);
                }
                else if ( problemTypes[which].equals(getString(R.string.heavy_rain)))
                {
                    incidentType = getString(R.string.heavy_rain);
                }
                else if ( problemTypes[which].equals(getString(R.string.snow_storm)))
                {
                    incidentType = getString(R.string.snow_storm);
                }
                buildAddDesciptionMessage(incidentType);
            }
        });
        builder.show();
    }

    public void buildAddDesciptionMessage(String problemTitle) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(problemTitle);
        alertDialog.setMessage("Please enter a description");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        // alertDialog.setIcon(R.drawable.key);

        alertDialog.setPositiveButton("CONFIRM",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(input.getText()!=null && input.getText().length()>0) {
                            incidentDescription = input.getText().toString();
                        }
                        else {
                            incidentDescription = "no description";
                        }
                        dialog.dismiss();
                        //show camera dialog
                        buildCameraMessage();
                    }
                });

        alertDialog.setNegativeButton("SKIP",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        incidentDescription = "no description";
                        buildCameraMessage();
                        dialog.dismiss();
                    }
                });

        alertDialog.show();
    }

    public void buildCameraMessage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Do you want to take a picture also?")
                .setCancelable(false)
                .setPositiveButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent camera = new Intent(
                                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(camera,2);
                    }
                })
                .setNeutralButton("Choose from device", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto , 1);//one can be replaced with any action code

                    }
                })
                .setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        report();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void report()
    {
        Incident incident = new Incident();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String dateNow = dateFormat.format(date).toString();
        //Adding values
        incident.setDescription(incidentDescription);
        incident.setDate(dateNow);
        //incident.setLocation(vm.getLocation());
        incident.setLocationLat(String.valueOf(vm.getLocation().getLatitude()));
        incident.setLocationLong(String.valueOf(vm.getLocation().getLongitude()));
        incident.setType(incidentType);
        incident.setImageUrl(imageUrl);
        incident.setNotifiedUsersId("");
        String incidentUid =  FirebaseDatabase.getInstance().getReference("Incidents").push().getKey();
        incident.setIncidentUid("");
        incident.setUserUId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        FirebaseDatabase.getInstance().getReference("Incidents")
                .push()
                .setValue(incident)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            //todo remove them
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


    // UploadImage method
    private void uploadImage()
    {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(MainActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            imageUrl = uri.toString();
                                            Log.i("image uri vasilis", imageUrl);
                                            report();
                                        }
                                    });
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });
        }
    }

    private String collectUsername(Map<String, Object> value) {
        String name = "";

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : value.entrySet()){
            //Get user map
            Map singleUser = (Map) entry.getValue();
            if(singleUser.get("email").equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            {
                //Get username field
                name = singleUser.get("username").toString();
            }
        }
        return name;
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }
}
