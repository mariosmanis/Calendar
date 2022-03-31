package com.unipi.p18196.callendar.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.p18196.callendar.EditEvent;
import com.unipi.p18196.callendar.LocaleHelper;
import com.unipi.p18196.callendar.Login;
import com.unipi.p18196.callendar.Models.EventModel;
import com.unipi.p18196.callendar.Models.UserModel;
import com.unipi.p18196.callendar.R;
import com.unipi.p18196.callendar.Signup;

import java.util.ArrayList;

public class EventAdapter extends ArrayAdapter<EventModel> {

    // Initialise variables
    TextView title, start_time, end_time, members, creator, comments, comments_creator, event_title, event_start_hour, event_end_hour, event_members, event_creator;
    ImageView editEvent, deleteEvent;
    FirebaseFirestore db;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ArrayList<EventModel> eventModelArrayList;
    UserModel userModel;
    Context context;
    String username;

    public EventAdapter(@NonNull Context context, ArrayList<EventModel> eventModelList) {
        super(context, 0, eventModelList);

        this.context = context;
        this.eventModelArrayList = eventModelList;
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)  {

        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.event_adapter, parent, false);

        }

        database.getReference().child("Users").child(String.valueOf(auth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userModel = snapshot.getValue(UserModel.class);
                assert userModel != null;
                username = userModel.getUsername();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ"))
        {
            context = LocaleHelper.setLocale(view.getContext(), "el");

            event_title = view.findViewById(R.id.event_title);
            event_title.setText(context.getResources().getString(R.string.event_adapter_title));

            event_start_hour = view.findViewById(R.id.event_start_hour);
            event_start_hour.setText(context.getResources().getString(R.string.event_adapter_start_hour));

            event_end_hour = view.findViewById(R.id.event_end_hour);
            event_end_hour.setText(context.getResources().getString(R.string.event_adapter_end_hour));

            event_members = view.findViewById(R.id.event_members);
            event_members.setText(context.getResources().getString(R.string.event_adapter_members));

            event_creator = view.findViewById(R.id.event_creator);
            event_creator.setText(context.getResources().getString(R.string.event_adapter_creator));
        }


        title = view.findViewById(R.id.show_title);
        start_time = view.findViewById(R.id.show_event_start_hour);
        end_time = view.findViewById(R.id.show_event_end_hour);
        members = view.findViewById(R.id.show_event_members);
        creator = view.findViewById(R.id.show_event_creator);
        editEvent = view.findViewById(R.id.edit_event);
        deleteEvent = view.findViewById(R.id.delete_event);


        title.setText(eventModelArrayList.get(position).getTitle());
        start_time.setText(eventModelArrayList.get(position).getStart_Time());
        end_time.setText(eventModelArrayList.get(position).getEnd_Time());
        members.setText(eventModelArrayList.get(position).getMembers().replace(": pending","").replace(": yes", "").replace(": no",""));
        creator.setText(eventModelArrayList.get(position).getCreator());


        deleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(username.equals(eventModelArrayList.get(position).getCreator()))
                {
                    deleteEvent(position);
                }
                else
                {
                    Toast.makeText(context, context.getResources().getString(R.string.event_cannot_delete), Toast.LENGTH_LONG).show();
                }
            }
        });

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, EditEvent.class);
                intent.putExtra("event_details", eventModelArrayList.get(position));
                context.startActivity(intent);
            }
        });

        return view;
    }

    public  void deleteEvent(int index){

        db.collection("Events")
                .document(eventModelArrayList.get(index).getDocumentId())
                .delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        eventModelArrayList.remove(eventModelArrayList.get(index));

                        notifyDataSetChanged();

                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            Context c = LocaleHelper.setLocale(context, "el");

                            Toast.makeText(context, c.getResources().getString(R.string.delete_event_success_message), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Event successfully removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {

                        if (Signup.selected_lang.equals("ΕΛ") || Login.selected_lang.equals("ΕΛ")) {
                            Context c = LocaleHelper.setLocale(context, "el");

                            Toast.makeText(context, c.getResources().getString(R.string.delete_event_error_message), Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(context, "Something went wrong  ", Toast.LENGTH_SHORT).show();
                        }
                    }

                });
    }
}
