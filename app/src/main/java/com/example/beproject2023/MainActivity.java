package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {
    public static boolean isAdmin;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    TextView textViewToggle;
    Button buttonLogin, buttonSignup;
    EditText editTextEmail, editTextPasswordLogin;


    public void launchHomeActivity() {
        Intent intent = new Intent(MainActivity.this, HomePage.class);

        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(getApplicationContext());

        firebaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.editTextLogInEmailAddress);
        editTextPasswordLogin = findViewById(R.id.editTextLogInPassword);

        if (firebaseAuth.getCurrentUser() != null) {
            launchHomeActivity();
        }


    }


    public void doLogin(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPasswordLogin.getText().toString();

        Log.i("LOGIN", " " + email + " " + password);

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                Log.i("SUCCESS", "Logged in " );
                                Intent intent = new Intent(MainActivity.this, HomePage.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.i("FAIL", "Log in failed " + task.getException());
                                Toast.makeText(MainActivity.this, "Failed to Log In", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
}

