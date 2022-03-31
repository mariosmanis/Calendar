package com.unipi.p18196.callendar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.unipi.p18196.callendar.Models.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Signup extends AppCompatActivity {

    // Initialize variables
    EditText username, email, password;
    TextView title, move, move_label;
    Button signup;
    Spinner lang;
    int lang_selected;
    Context context;
    Resources resources;
    FirebaseAuth auth;
    FirebaseDatabase database;

    public static String selected_lang = "EN";

    private static Signup singleton;

    public static Signup getInstance() {
        return singleton;
    }
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Get fire base instance
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Define the view fields based on id from the xml file
        title = findViewById(R.id.signup_title);
        username = findViewById(R.id.signup_username);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        signup = findViewById(R.id.signup_button);
        lang = (Spinner) findViewById(R.id.select_language);
        move = findViewById(R.id.move_link);
        move_label = findViewById(R.id.move_label);

        signup.setOnClickListener(v -> createUser());
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        move.setOnClickListener(v -> {
            startActivity(new Intent(Signup.this, Login.class));
        });

        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("EN");
        spinnerArray.add("ΕΛ");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lang.setAdapter(adapter);
        if(Login.selected_lang.equals("ΕΛ"))
        {
            lang.setSelection(1);
        }
        lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selected_lang = lang.getSelectedItem().toString();

                if (selected_lang.equals("EN")) {
                    context = LocaleHelper.setLocale(Signup.this, "en");
                    resources = context.getResources();
                    lang_selected = 0;

                    setTitle(resources.getString(R.string.app_name));
                    username.setHint(resources.getString(R.string.signup_username));
                    title.setText(resources.getString(R.string.signup_title));
                    email.setHint(resources.getString(R.string.signup_email));
                    password.setHint(resources.getString(R.string.signup_password));
                    signup.setText(resources.getString(R.string.signup_button));
                    move_label.setText(resources.getString(R.string.signup_button_label));
                    move.setText(resources.getString(R.string.signup_button_link));

                } else {
                    context = LocaleHelper.setLocale(Signup.this, "el");
                    resources = context.getResources();
                    lang_selected = 1;

                    setTitle(resources.getString(R.string.app_name));
                    username.setHint(resources.getString(R.string.signup_username));
                    title.setText(resources.getString(R.string.signup_title));
                    email.setHint(resources.getString(R.string.signup_email));
                    password.setHint(resources.getString(R.string.signup_password));
                    signup.setText(resources.getString(R.string.signup_button));
                    move_label.setText(resources.getString(R.string.signup_button_label));
                    move.setText(resources.getString(R.string.signup_button_link));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        singleton = this;
    }


    // Method for register the user
    private void createUser() {

        if(!isNetworkAvailable())
        {
            if (selected_lang.equals("ΕΛ")) {
                context = LocaleHelper.setLocale(Signup.this, "el");
                Toast.makeText(Signup.this, context.getResources().getString(R.string.no_internet_available), Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Signup.this, context.getResources().getString(R.string.no_internet_available), Toast.LENGTH_LONG).show();
            }
        }
        else {
            String User_name = username.getText().toString();
            String User_email = email.getText().toString();
            String User_password = password.getText().toString();


            // Validating the text fields if they are empty or not.
            if (User_email.isEmpty()) {
                // Print message for user
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Signup.this, "el");

                    email.setError(context.getResources().getString(R.string.signup_email_error));
                } else {
                    email.setError("Please enter your email");
                }

            } else if (User_name.isEmpty()) {
                // Print message for users
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Signup.this, "el");

                    username.setError(context.getResources().getString(R.string.signup_username_error));
                } else {
                    username.setError("Please enter your username");
                }
            } else if (User_password.isEmpty()) {
                // Print message for user
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Signup.this, "el");

                    password.setError(context.getResources().getString(R.string.signup_password_error));
                } else {
                    password.setError("Please enter your password");
                }
            } else if (password.length() < 6) {
                // Print message for user
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Signup.this, "el");

                    password.setError(context.getResources().getString(R.string.signup_password_length_error));
                } else {
                    password.setError("Password must be at least 6 characters long");
                }
            }
            // Check thw length of the password
            else {

                // Check the authentication of the user's credentials
                auth.createUserWithEmailAndPassword(User_email, User_password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                UserModel userModel = new UserModel(User_name, User_email, User_password);
                                String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                                database.getReference().child("Users").child(id).setValue(userModel);

                                if (selected_lang.equals("ΕΛ")) {
                                    context = LocaleHelper.setLocale(Signup.this, "el");

                                    Toast.makeText(Signup.this, context.getResources().getString(R.string.signup_welcome_message), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Signup.this, "Welcome!", Toast.LENGTH_LONG).show();
                                }
                                startActivity(new Intent(Signup.this, Calendar.class));

                            } else {

                                if (selected_lang.equals("ΕΛ")) {
                                    context = LocaleHelper.setLocale(Signup.this, "el");

                                    Toast.makeText(Signup.this, context.getResources().getString(R.string.signup_error_message), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Signup.this, "Wrong credentials.Please try again", Toast.LENGTH_SHORT).show();
                                }

                                // Clear the fields
                                username.setText("");
                                email.setText("");
                                password.setText("");
                            }
                        });

            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}