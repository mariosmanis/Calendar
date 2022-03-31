package com.unipi.p18196.callendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
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
import java.util.HashMap;

public class EditEvent extends AppCompatActivity {

    EditText title,start_time, end_time, comments;
    TextView creator, date, members, creator_comments, title_label, start_hour_label, end_hour_label, creator_label, members_label, location_label, comments_label, comments_history, status_selection, status_label;
    Button edit, location;
    ArrayList<EventModel> eventModelArrayList;
    FirebaseFirestore db;
    String documentID = "";
    FirebaseAuth auth;
    FirebaseDatabase database;
    String format = "";
    EventModel eventModel;
    UserModel userModel;
    private LocationManager locationManager;
    String event_latitude = "", event_longitude = "";
    ArrayList<String> memberList = new ArrayList<>();
    ArrayList<String> statusList = new ArrayList<>();
    private Context c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Get fire base instance
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        date = findViewById(R.id.edit_new_selected_date);
        title = findViewById(R.id.edit_event_title);
        start_time = findViewById(R.id.edit_event_start_time);
        end_time = findViewById(R.id.edit_event_end_time);
        creator = findViewById(R.id.edit_event_creator);
        members = findViewById(R.id.edit_event_members);
        comments = findViewById(R.id.edit_event_comments);
        creator_comments = findViewById(R.id.edit_creator_comments);
        comments_history = findViewById(R.id.edit_comments_history);
        location = findViewById(R.id.edit_event_location);
        edit = findViewById(R.id.edit_event_button);
        status_label = findViewById(R.id.edit_event_status);
        status_selection = findViewById(R.id.edit_event_status_selection);
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

        title_label = findViewById(R.id.edit_event_title_label);
        title_label.setText(c.getResources().getString(R.string.add_event_title));

        start_hour_label = findViewById(R.id.edit_event_start_time_label);
        start_hour_label.setText(c.getResources().getString(R.string.add_event_start_hour));

        end_hour_label = findViewById(R.id.edit_event_end_time_label);
        end_hour_label.setText(c.getResources().getString(R.string.add_event_end_hour));

        creator_label = findViewById(R.id.edit_event_creator_label);
        creator_label.setText(c.getResources().getString(R.string.add_event_creator));

        location.setText(c.getResources().getString(R.string.edit_location_event_button));

        edit.setText(c.getResources().getString(R.string.edit_event_button));

        comments_label = findViewById(R.id.edit_event_comments_label);
        comments_label.setText(c.getResources().getString(R.string.add_event_comments));

        members_label = findViewById(R.id.edit_event_members_label);
        members_label.setText(c.getResources().getString(R.string.add_event_members));

        members.setHint(c.getResources().getString(R.string.add_event_add_members));

        status_label.setText(c.getResources().getString(R.string.edit_event_status));
        status_selection.setHint(c.getResources().getString(R.string.edit_event_status_selection));

        location_label = findViewById(R.id.edit_event_location_label);
        location_label.setText(c.getResources().getString(R.string.add_event_location));

        final Object object = getIntent().getSerializableExtra("event_details");

        if( object instanceof EventModel) {
            eventModel = (EventModel) object;
        }

        if( eventModel != null) {

            date.setText(eventModel.getDate());
            title.setText(eventModel.getTitle());
            start_time.setText(eventModel.getStart_Time());
            if(eventModel.getEnd_Time().isEmpty())
            {
                end_time.setText("--:--");
            }
            else
            {
                end_time.setText(eventModel.getEnd_Time());
            }
            creator.setText(eventModel.getCreator());
            event_latitude = eventModel.getLatitude();

            event_longitude = eventModel.getLongitude();


            if (Signup.selected_lang.equals("ΕΛ")  || Login.selected_lang.equals("ΕΛ"))
            {
                members.setText(eventModel.getMembers().replace("pending", "αναμένεται").replace("yes", "ναι").replace(" no","όχι").replace(",",",\n"));
            }
            else
            {
                members.setText(eventModel.getMembers().replace(",",",\n"));
            }

            documentID = eventModel.getDocumentId();
            comments_history.setText(eventModel.getComments());

            database.getReference().child("Users").child(String.valueOf(auth.getUid())).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    userModel = snapshot.getValue(UserModel.class);
                    creator_comments.setText(userModel.getUsername());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!creator_comments.getText().toString().equals(creator.getText().toString()))
                {
                    title.setEnabled(false);
                }
            }
        });

        start_time.setOnFocusChangeListener((v, hasFocus) -> {
            if(creator_comments.getText().toString().equals(creator.getText().toString()))
            {
                if(hasFocus){

                    java.util.Calendar calendar = java.util.Calendar.getInstance();

                    int hours = calendar.get(java.util.Calendar.HOUR_OF_DAY);
                    int min = calendar.get(java.util.Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(EditEvent.this, (view, selectedHour, selectedMinute) ->               {
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
            }
            else
            {
                start_time.setEnabled(false);
            }
        });

        end_time.setOnFocusChangeListener((v, hasFocus) -> {
            if(creator_comments.getText().toString().equals(creator.getText().toString())) {
                if (hasFocus) {

                    java.util.Calendar calendar = java.util.Calendar.getInstance();

                    int hours = calendar.get(java.util.Calendar.HOUR_OF_DAY);
                    int min = calendar.get(Calendar.MINUTE);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(EditEvent.this, (view, selectedHour, selectedMinute) -> {
                        try {

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

                            if (selected_hour.length() == 1) {
                                selected_hour = "0" + selected_hour;
                            }

                            if (selected_minutes.length() == 1) {
                                selected_minutes = "0" + selected_minutes;
                            }

                            end_time.setText(new StringBuilder().append(selected_hour).append(" : ").append(selected_minutes).append(" ").append(format));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }, hours, min, false);

                    timePickerDialog.setTitle("Select Time");
                    timePickerDialog.show();
                }
            }
            else
            {
                end_time.setEnabled(false);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEvent();
            }
        });

        status_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(creator_comments.getText().toString().equals(creator.getText().toString()))
                {
                    status_selection.setEnabled(false);
                }
                else
                {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(EditEvent.this);

                    // title of the alert dialog
                    alertDialog.setTitle(c.getResources().getString(R.string.edit_event_status_selection));

                    String[] statusArray = new String[statusList.size()];

                    statusList.toArray(statusArray);

                    final int[] checkedItem = {-1};

                    alertDialog.setSingleChoiceItems(statusArray, checkedItem[0], new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // update the selected item which is selected by the user
                            // so that it should be selected when user opens the dialog next time
                            // and pass the instance to setSingleChoiceItems method
                            checkedItem[0] = which;

                            // now also update the TextView which previews the selected item
                            status_selection.setText(statusArray[which]);

                            // when selected an item the dialog should be closed with the dismiss method
                            dialog.dismiss();
                        }
                    });

                    // show the alert dialog when the button is clicked
                    alertDialog.show();
                }
            }

        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(event_latitude != null && event_longitude != null){
                    if(!event_latitude.isEmpty() && !event_longitude.isEmpty())
                    {
                        Intent intent = new Intent(EditEvent.this, MapsActivity.class);
                        intent.putExtra("latitude", event_latitude);
                        intent.putExtra("longitude", event_longitude);
                        startActivity(intent);

                    }
                }

                else
                {
                    location.setEnabled(false);
                }
            }
        });



    }

    private void editEvent(){

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
            else if(eventModel != null) {
                // Edw update
                updateEvent();
            }
        }
        catch (Exception ex) {
        }

    }

    private void addEvent(String eventTitle, String eventTime){

        // Create hash map to pass the data
        final HashMap<String, Object> eventMap = new HashMap<>();

        StringBuilder builder = new StringBuilder();

        // If no previous comment history
        if(comments_history.getText().toString().isEmpty())
        {
            // Creator comment
            builder.append(creator.getText().toString()).append(": ").append(comments.getText().toString());
        }
        // Append new comment
        else
        {
            if(!comments.getText().toString().isEmpty())
            {
                // Get history
                builder.append(comments_history.getText().toString()).append(System.getProperty("line.separator"));

                // Get new comment
                builder.append(creator_comments.getText().toString()).append(": ").append(comments.getText().toString());
            }
        }

        ArrayList<String> member_status = new ArrayList<>();
        String[] usernames = members.getText().toString().split(",");
        for (int i = 0; i < usernames.length; i++)
        {
            String username = usernames[i].trim();
            member_status.add(username);
        }


        String current_user_status = status_selection.getText().toString();

        for(String member_with_status : member_status)
        {
            if(member_with_status.trim().split(":")[0].equals(creator_comments.getText().toString()))
            {
                member_status.remove(member_with_status);
                member_status.add(creator_comments.getText().toString()+ ": " +current_user_status);
            }
        }


        // Fill the hash map
        eventMap.put("Date", date.getText().toString());
        eventMap.put("Title", eventTitle);
        eventMap.put("Start_Time", eventTime);
        eventMap.put("End_Time", end_time.getText().toString());
        eventMap.put("Members", member_status.toString().replace("[","")
                                                        .replace("]","")
                                                        .replace("αναμένεται","pending")
                                                        .replace("ναι", "yes")
                                                        .replace("όχι", "no")
                                                        .replace(",\n",","));
        eventMap.put("Creator", creator.getText().toString());
        eventMap.put("Latitude", eventModel.getLatitude());
        eventMap.put("Longitude", eventModel.getLongitude());
        eventMap.put("Comments", builder.toString());

        db.collection("Events").add(eventMap).addOnCompleteListener(task -> {

            if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                Context context = LocaleHelper.setLocale(EditEvent.this, "el");
                Toast.makeText(EditEvent.this, context.getResources().getString(R.string.add_event_success_language), Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    private void updateEvent(){

        db.collection("Events")
                .document(documentID)
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String dateTitle = title.getText().toString();
                        String dateHour = start_time.getText().toString();

                        addEvent(dateTitle, dateHour);
                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            Context context = LocaleHelper.setLocale(EditEvent.this, "el");
                            Toast.makeText(EditEvent.this, context.getResources().getString(R.string.edit_event_success_message), Toast.LENGTH_SHORT).show();
                            notifyUser();
                        }
                        else{
                            Toast.makeText(EditEvent.this, "Event successfully edited", Toast.LENGTH_SHORT).show();
                            notifyUser();
                        }
                    }
                    else {
                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            Context context = LocaleHelper.setLocale(EditEvent.this, "el");
                            Toast.makeText(EditEvent.this, context.getResources().getString(R.string.edit_event_error_message), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(EditEvent.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void populateMembers()
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

    private void notifyUser() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                .setContentText("Test")
                .setSmallIcon(R.drawable.agenda)
                .setAutoCancel(true)
                .setContentText("Event edited");
        NotificationManagerCompat manCompat = NotificationManagerCompat.from(this);
        manCompat.notify(999, builder.build());
    }

}