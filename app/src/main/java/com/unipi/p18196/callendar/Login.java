package com.unipi.p18196.callendar;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {

    private static final int REQUEST_CODE = 101010;
    TextView move, fingerprint, title, move_label;
    EditText email, password;
    Button login;
    Spinner lang;
    FirebaseAuth auth;
    FirebaseUser user;
    Executor executor;
    Context context;
    Resources resources;
    BiometricPrompt biometricPrompt;
    BiometricPrompt.PromptInfo promptInfo;
    SharedPreferences sharedPreferences;
    FirebaseDatabase database;

    public static String selected_lang = "EN";

    private static Login singleton;

    public static Login getInstance() {
        return singleton;
    }

    private boolean canFingerprint = false;


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get fire base instance
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();


        // Define the view fields based on id from the xml file
        title = findViewById(R.id.login_title);
        move = findViewById(R.id.move_link);
        move_label = findViewById(R.id.move_label);
        login = findViewById(R.id.login_button);
        email = findViewById(R.id.login_email);
        password = findViewById(R.id.login_password);
        lang = (Spinner) findViewById(R.id.select_language);
        fingerprint = findViewById(R.id.fingerprint_login);
        fingerprint.setVisibility(View.INVISIBLE);


        List<String> spinnerArray = new ArrayList<String>();
        spinnerArray.add("EN");
        spinnerArray.add("ΕΛ");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lang.setAdapter(adapter);

        if(Signup.selected_lang.equals("ΕΛ"))
        {
            lang.setSelection(1);
        }
        lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                selected_lang = lang.getSelectedItem().toString();

                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Login.this, "el");
                    resources = context.getResources();

                    setTitle(resources.getString(R.string.login_app_name));
                    title.setText(resources.getString(R.string.login_title));
                    email.setHint(resources.getString(R.string.login_email));
                    password.setHint(resources.getString(R.string.login_password));
                    login.setText(resources.getString(R.string.login_button));
                    move_label.setText(resources.getString(R.string.login_button_label));
                    move.setText(resources.getString(R.string.login_button_link));
                    fingerprint.setText(resources.getString(R.string.login_fingerprint));
                }
                else
                {
                    context = LocaleHelper.setLocale(Login.this, "en");
                    resources = context.getResources();

                    setTitle(resources.getString(R.string.login_app_name));
                    title.setText(resources.getString(R.string.login_title));
                    email.setHint(resources.getString(R.string.login_email));
                    password.setHint(resources.getString(R.string.login_password));
                    login.setText(resources.getString(R.string.login_button));
                    move_label.setText(resources.getString(R.string.login_button_label));
                    move.setText(resources.getString(R.string.login_button_link));
                    fingerprint.setText(resources.getString(R.string.login_fingerprint));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate())
        {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("Calendar", "App can authenticate using biometrics.");
                canFingerprint = true;
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("Calendar", "No biometric features available on this device.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("Calendar", "Biometric features are currently unavailable.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                break;
        }

        sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isLogin = sharedPreferences.getBoolean("isLogin", false);
        if(isLogin && canFingerprint)
        {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");
            fingerprint.setVisibility(View.VISIBLE);
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(Login.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result)
            {
                super.onAuthenticationSucceeded(result);
                String email = sharedPreferences.getString("email", "");
                String password = sharedPreferences.getString("password", "");
                loginUser(email, password);
            }

            @Override
            public void onAuthenticationFailed()
            {
                super.onAuthenticationFailed();

                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Login.this, "el");
                    resources = context.getResources();

                    Toast.makeText(getApplicationContext(), resources.getString(R.string.login_error_message_fingerprint),
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Authentication failed",
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });


        fingerprint.setOnClickListener(view -> {
            if(canFingerprint) {
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Login.this, "el");

                    resources = context.getResources();
                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(resources.getString(R.string.login_message_fingerprint_title))
                            .setSubtitle(resources.getString(R.string.login_message_fingerprint_))
                            .setNegativeButtonText(resources.getString(R.string.login_message_fingerprint_password))
                            .build();
                } else {
                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Biometric login")
                            .setSubtitle("Log in using your biometric credential")
                            .setNegativeButtonText("Use account password")
                            .build();
                }
                biometricPrompt.authenticate(promptInfo);
            }

        });

        move.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, Signup.class));
        });

        login.setOnClickListener(
                v -> loginUser(email.getText().toString(), password.getText().toString())

        );

        singleton = this;
    }

    // Method for login the user
    public void loginUser(String UserEmail, String UserPassword)
    {
        if(!isNetworkAvailable())
        {
            if (selected_lang.equals("ΕΛ")) {
                context = LocaleHelper.setLocale(Login.this, "el");
                Toast.makeText(Login.this, context.getResources().getString(R.string.no_internet_available), Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(Login.this, context.getResources().getString(R.string.no_internet_available), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            // Validating the text fields if they are empty or not.
            if (UserEmail.isEmpty()) {
                // Print message for users

                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Login.this, "el");

                    email.setError(context.getResources().getString(R.string.login_email_error));
                }
                else{
                    email.setError("Please enter your email");
                }
            }
            else if (UserPassword.isEmpty()) {
                // Print message for user
                if (selected_lang.equals("ΕΛ")) {
                    context = LocaleHelper.setLocale(Login.this, "el");

                    password.setError(context.getResources().getString(R.string.login_password_error));
                }
                else{
                    password.setError("Please enter your password");
                }
            }
            else
            {
                // Check the authentication of the user's credentials
                auth.signInWithEmailAndPassword(UserEmail, UserPassword)
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()) {
                                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                                editor.putString("email",UserEmail);
                                editor.putString("password", UserPassword);
                                editor.putBoolean("isLogin", true);
                                editor.apply();
                                database.getReference().child("Users").child(String.valueOf(auth.getUid())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if(task.isSuccessful())
                                        {
                                            String username  = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).child("username").getValue()).toString();
                                            Intent intent = new Intent(Login.this, Calendar.class);
                                            intent.putExtra("username", username);

                                            if (selected_lang.equals("ΕΛ")) {
                                                context = LocaleHelper.setLocale(Login.this, "el");

                                                Toast.makeText(Login.this, context.getResources().getString(R.string.login_welcome_message), Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                Toast.makeText(Login.this,"Welcome back !",Toast.LENGTH_SHORT).show();
                                            }
                                            startActivity(intent);
                                        }
                                    }
                                });

                            }
                            else {

                                if (selected_lang.equals("ΕΛ")) {
                                    context = LocaleHelper.setLocale(Login.this, "el");

                                    Toast.makeText(Login.this, context.getResources().getString(R.string.login_error_message), Toast.LENGTH_LONG).show();
                                }
                                else{
                                    Toast.makeText(Login.this,"Wrong credentials. Please try again!" ,Toast.LENGTH_SHORT).show();
                                }

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