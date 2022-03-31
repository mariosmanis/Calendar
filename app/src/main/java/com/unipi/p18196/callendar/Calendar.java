package com.unipi.p18196.callendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.unipi.p18196.callendar.Adapters.EventAdapter;
import com.unipi.p18196.callendar.Models.EventModel;
import com.unipi.p18196.callendar.Models.UserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Calendar extends AppCompatActivity {

    CalendarView calendar;
    TextView event;
    ImageView add, edit, delete;
    String selected_date;
    ListView event_list;
    ArrayList<EventModel> eventModelArrayList;
    FirebaseFirestore db;
    FirebaseDatabase database;
    FirebaseAuth auth;
    UserModel userModel;
    String username, language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Initialize variables
        event_list = findViewById(R.id.event_list);
        event = findViewById(R.id.no_event);
        add = findViewById(R.id.add);
        calendar = findViewById(R.id.calendar);

        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ"))
        {
            Context context = LocaleHelper.setLocale(Calendar.this, "el");
            Resources resources = context.getResources();
            setTitle(resources.getString(R.string.login_app_name));
        }

        else
        {
            Context context = LocaleHelper.setLocale(Calendar.this, "en");
            Resources resources = context.getResources();
            setTitle(resources.getString(R.string.login_app_name));
        }

        eventModelArrayList = new ArrayList<>();
        Intent roof = getIntent();
        username = roof.getStringExtra("username");

        Intent lang = getIntent();
        language = lang.getStringExtra("lang");

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        long calendar_date = calendar.getDate();
        selected_date = DateFormat.format("dd/MM/yyyy", new Date(calendar_date)).toString();


        calendar.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            eventModelArrayList.clear();
            selected_date = formatDate(dayOfMonth, month + 1, year);
            loadListview(selected_date, username);
        });

        add.setOnClickListener(v -> {
            Intent intent = new Intent(Calendar.this, AddEvent.class);
            intent.putExtra("date", selected_date);
            startActivity(intent);

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventModelArrayList.clear();
        loadListview(selected_date, username);
    }

    private String formatDate(int day, int month, int year) {
        String sDay = String.valueOf(day);
        String sMonth = String.valueOf(month);
        String sYear = String.valueOf(year);

        if (sDay.length() == 1)
            sDay = "0" + sDay;

        if (sMonth.length() == 1)
            sMonth = "0" + sMonth;

        return sDay + "/" + sMonth + "/" + sYear;
    }

    private void loadListview(String date, String username) {
        db.collection("Events")
                .whereEqualTo("Date", date)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            event.setText("");
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                            for (DocumentSnapshot d : list) {
                                if (Objects.equals(d.get("Creator"), username) ||Objects.requireNonNull(d.get("Members")).toString().contains(username)) {

                                    String documentId = d.getId();

                                    EventModel eventModel = d.toObject(EventModel.class);
                                    eventModel.setDocumentId(documentId);

                                    eventModelArrayList.add(eventModel);
                                    EventAdapter adapter = new EventAdapter(Calendar.this, eventModelArrayList);
                                    event_list.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                }

                            }
                        }
                        else{
                            eventModelArrayList.clear();
                            event_list.setAdapter(null);

                            if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                                Context context = LocaleHelper.setLocale(Calendar.this, "el");
                                setTitle(context.getString(R.string.login_app_name));
                                event.setText(context.getResources().getString(R.string.event_calendar));
                            }
                            else{
                                Context context = LocaleHelper.setLocale(Calendar.this, "en");
                                setTitle(context.getString(R.string.login_app_name));
                                event.setText("No events");
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Calendar.this, "Fail to load events...", Toast.LENGTH_SHORT).show();
            }
        });
    }



}

