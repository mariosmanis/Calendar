package com.unipi.p18196.callendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.p18196.callendar.Models.EventModel;
import com.unipi.p18196.callendar.Models.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

public class AddEvent extends AppCompatActivity implements LocationListener
{
    EditText title,start_time, end_time, comments;
    TextView creator, date, members, creator_comments, title_label, start_hour_label, end_hour_label, creator_label, members_label, location_label, comments_label, comments_history;
    Button add, location;
    ArrayList<EventModel> eventModelArrayList;
    FirebaseFirestore db;
    String documentID = "";
    FirebaseAuth auth;
    FirebaseDatabase database;
    String format = "";
    EventModel eventModel;
    UserModel userModel;
    private LocationManager locationManager;
    String event_latitude, event_longitude;
    ArrayList<String> memberList = new ArrayList<>();
    ArrayList<String> statusList = new ArrayList<>();
    private static final int REQ_LOC_CODE = 23;
    private Context c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Get fire base instance
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        date = findViewById(R.id.add_new_selected_date);
        title = findViewById(R.id.add_event_title);
        start_time = findViewById(R.id.add_event_start_time);
        end_time = findViewById(R.id.add_event_end_time);
        creator = findViewById(R.id.add_event_creator);
        members = findViewById(R.id.add_event_members);
        comments = findViewById(R.id.add_event_comments);
        creator_comments = findViewById(R.id.creator_comments);
        comments_history = findViewById(R.id.comments_history);
        location = findViewById(R.id.add_event_location);
        add = findViewById(R.id.add_event_button);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        comments_history.setMovementMethod(new ScrollingMovementMethod());

        populateMembers();


        if (Signup.selected_lang.equals("ΕΛ")  || Login.selected_lang.equals("ΕΛ"))
        {
            c = LocaleHelper.setLocale(this, "el");
            statusList.add("αναμένεται");
            statusList.add("όχι");
            statusList.add("ναι");
        }
        else
        {
            c = LocaleHelper.setLocale(this, "en");
            statusList.add("pending");
            statusList.add("no");
            statusList.add("yes");
        }

            setTitle(c.getString(R.string.login_app_name));

            title_label = findViewById(R.id.add_event_title_label);
            title_label.setText(c.getResources().getString(R.string.add_event_title));

            start_hour_label = findViewById(R.id.add_event_start_time_label);
            start_hour_label.setText(c.getResources().getString(R.string.add_event_start_hour));

            end_hour_label = findViewById(R.id.add_event_end_time_label);
            end_hour_label.setText(c.getResources().getString(R.string.add_event_end_hour));

            creator_label = findViewById(R.id.add_event_creator_label);
            creator_label.setText(c.getResources().getString(R.string.add_event_creator));

            location.setText(c.getResources().getString(R.string.location_event_button));

            add.setText(c.getResources().getString(R.string.add_event_button));

            comments_label = findViewById(R.id.add_event_comments_label);
            comments_label.setText(c.getResources().getString(R.string.add_event_comments));

            members_label = findViewById(R.id.add_event_members_label);
            members_label.setText(c.getResources().getString(R.string.add_event_members));

            members.setHint(c.getResources().getString(R.string.add_event_add_members));

            location_label = findViewById(R.id.add_event_location_label);
            location_label.setText(c.getResources().getString(R.string.add_event_location));

            Intent incomingIntent = getIntent();
            String selected_date = incomingIntent.getStringExtra("date");
            date.setText(selected_date);
            database.getReference().child("Users").child(String.valueOf(auth.getUid())).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userModel = snapshot.getValue(UserModel.class);
                    creator.setText(userModel.getUsername());
                    creator_comments.setText(userModel.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });


        start_time.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){

                Calendar calendar = Calendar.getInstance();

                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEvent.this, (view, selectedHour, selectedMinute) ->               {
                    try{

                        if (selectedHour == 0) {
                            selectedHour += 12;
                            format = "AM";
                        } else if (selectedHour == 12) {
                            format = "PM";
                        } else if (selectedHour > 12) {
                            selectedHour -= 12;
                            format = "PM";
                        } else {
                            format = "AM";
                        }

                        String selected_hour = String.valueOf(selectedHour);
                        String selected_minutes = String.valueOf(selectedMinute);

                        if(selected_hour.length() == 1){
                            selected_hour = "0" + selected_hour;
                        }

                        if(selected_minutes.length() == 1){
                            selected_minutes = "0" + selected_minutes;
                        }

                        start_time.setText(new StringBuilder().append(selected_hour).append(" : ").append(selected_minutes).append(" ").append(format));
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }, hours, min, false);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        end_time.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){

                Calendar calendar = Calendar.getInstance();

                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int min = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddEvent.this, (view, selectedHour, selectedMinute) -> {
                    try{

                        if (selectedHour == 0) {
                            selectedHour += 12;
                            format = "AM";
                        } else if (selectedHour == 12) {
                            format = "PM";
                        } else if (selectedHour > 12) {
                            selectedHour -= 12;
                            format = "PM";
                        } else {
                            format = "AM";
                        }

                        String selected_hour = String.valueOf(selectedHour);
                        String selected_minutes = String.valueOf(selectedMinute);

                        if(selected_hour.length() == 1){
                            selected_hour = "0" + selected_hour;
                        }

                        if(selected_minutes.length() == 1){
                            selected_minutes = "0" + selected_minutes;
                        }

                        end_time.setText(new StringBuilder().append(selected_hour).append(" : ").append(selected_minutes).append(" ").append(format));
                    }

                    catch (Exception e) {
                        e.printStackTrace();
                    }

                }, hours, min, false);

                timePickerDialog.setTitle("Select Time");
                timePickerDialog.show();
            }
        });

        add.setOnClickListener(v -> {

            try {
                String dateTitle = title.getText().toString();
                String dateHour = start_time.getText().toString();

                // Validating the text fields if they are empty or not.
                if (dateTitle.isEmpty()) {
                    // Print message for users
                    if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                        title.setError(c.getResources().getString(R.string.add_event_title_error));
                    }
                    else{
                        title.setError("Please enter a title to your event");
                    }
                }
                else if (dateHour.isEmpty()) {
                    // Print message for user
                    if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                        start_time.setError(c.getResources().getString(R.string.add_event_start_hour_error));
                    }
                    else{
                        start_time.setError("Please select hour");
                    }
                }
                else{
                    addEvent(dateTitle, dateHour);
                }
            }
            catch (Exception ex) {
            }
        });


        members.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        memberList.remove(userModel.getUsername());

                        ArrayList<Integer> langList = new ArrayList<>();

                        String[] langArray = new String[memberList.size()];
                        memberList.toArray(langArray);

                        boolean[] selectedLanguage = new boolean[langArray.length];

                        // Initialize alert dialog
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddEvent.this);

                        // set title

                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            builder.setTitle(c.getResources().getString(R.string.add_event_select_members_title));
                        } else {
                            builder.setTitle("Select members");
                        }

                        // set dialog non cancelable
                        builder.setCancelable(false);

                        builder.setMultiChoiceItems(langArray, selectedLanguage, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                // check condition
                                if (b) {
                                    // when checkbox selected
                                    // Add position  in lang list
                                    langList.add(i);
                                    // Sort array list
                                    Collections.sort(langList);
                                } else {
                                    // when checkbox unselected
                                    // Remove position from langList
                                    langList.remove(Integer.valueOf(i));
                                }
                            }
                        });

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Initialize string builder
                                StringBuilder stringBuilder = new StringBuilder();
                                // use for loop
                                for (int j = 0; j < langList.size(); j++) {
                                    // concat array value
                                    stringBuilder.append(langArray[langList.get(j)]);
                                    // check condition
                                    if (j != langList.size() - 1) {
                                        // When j value  not equal
                                        // to lang list size - 1
                                        // add comma
                                        stringBuilder.append(", ");
                                    }
                                }
                                // set text on textView
                                members.setText(stringBuilder.toString());
                            }
                        });

                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            builder.setNegativeButton(c.getResources().getString(R.string.add_event_select_members_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // dismiss dialog
                                    dialogInterface.dismiss();
                                }
                            });
                        } else {
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // dismiss dialog
                                    dialogInterface.dismiss();
                                }
                            });
                        }

                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            builder.setNeutralButton(c.getResources().getString(R.string.add_event_select_members_clear), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // use for loop
                                    for (int j = 0; j < selectedLanguage.length; j++) {
                                        // remove all selection
                                        selectedLanguage[j] = false;
                                        // clear language list
                                        langList.clear();
                                        // clear text view value
                                        members.setText("");
                                    }
                                }
                            });
                        } else {
                            builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // use for loop
                                    for (int j = 0; j < selectedLanguage.length; j++) {
                                        // remove all selection
                                        selectedLanguage[j] = false;
                                        // clear language list
                                        langList.clear();
                                        // clear text view value
                                        members.setText("");
                                    }
                                }
                            });
                        }
                        // show dialog
                        builder.show();
                    }
                });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If permission is not granted
                if (ActivityCompat.checkSelfPermission(AddEvent.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // Ask for permission
                    ActivityCompat.requestPermissions(AddEvent.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOC_CODE);
                }
                // If permission is granted
                else
                {
                    // Request location updates
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 50, AddEvent.this);
                }
            }
        });
    }

    public void populateMembers()
    {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot areaSnapshot: snapshot.getChildren()) {
                    String username = areaSnapshot.child("username").getValue(String.class);
                    memberList.add(username);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    public void addEvent(String eventTitle, String eventTime){

        // Create hash map to pass the data
        final HashMap<String, Object> eventMap = new HashMap<>();

        StringBuilder builder = new StringBuilder();

        // If no previous comment history
        if(comments_history.getText().toString().isEmpty())
        {
            if(!comments.getText().toString().isEmpty())
            {
                // Creator comment
                builder.append(creator.getText().toString()).append(": ").append(comments.getText().toString());
            }
        }
        // Append new comment
        else
        {
            // Get history
            builder.append(comments_history.getText().toString()).append(System.getProperty("line.separator"));

            // Get new comment
            builder.append(creator_comments.getText().toString()).append(": ").append(comments.getText().toString());
        }

        ArrayList<String> memberlist = new ArrayList<>();
        String[] usernames = members.getText().toString().split(",");
        for (int i = 0; i < usernames.length; i++)
        {
            String username = usernames[i].trim();
            memberlist.add(username + ": pending");
        }



        // Fill the hash map
        eventMap.put("Date", date.getText().toString());
        eventMap.put("Title", eventTitle);
        eventMap.put("Start_Time", eventTime);
        eventMap.put("End_Time", end_time.getText().toString());
        eventMap.put("Members", memberlist.toString().replace("[","").replace("]",""));
        eventMap.put("Creator", creator.getText().toString());
        eventMap.put("Comments", builder.toString());
        eventMap.put("Latitude", event_latitude);
        eventMap.put("Longitude", event_longitude);

        db.collection("Events").add(eventMap).addOnCompleteListener(task -> {

            if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                Context context = LocaleHelper.setLocale(AddEvent.this, "el");
                Toast.makeText(AddEvent.this, context.getResources().getString(R.string.add_event_success_language), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(AddEvent.this, "Event has been added", Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }


    @Override
    public void onLocationChanged(@NonNull Location location)
    {
        event_latitude = String.valueOf(location.getLatitude());
        event_longitude = String.valueOf(location.getLongitude());
        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
            Context context = LocaleHelper.setLocale(AddEvent.this, "el");
            Toast.makeText(AddEvent.this, context.getResources().getString(R.string.add_event_success_loc), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(AddEvent.this, "Location has been added", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}