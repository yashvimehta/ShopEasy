package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static boolean isAdmin;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseUser user;

    TextView textViewToggle;
    Button buttonLogin, buttonSignup;
    EditText editTextEmail, editTextPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isAdmin=false;

        FirebaseApp.initializeApp(getApplicationContext());

        firebaseAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();

        editTextEmail = findViewById(R.id.editTextLogInEmailAddress);
        editTextPasswordLogin = findViewById(R.id.editTextLogInPassword);

        if (firebaseAuth.getCurrentUser() != null) {
            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if(String.valueOf(document.getData().get("uuid")).equals(user.getUid())  && String.valueOf(document.getData().get("isAdmin")).equals("true") ) {
                                        Log.i("ADMINNN", "ADMIN");
                                        isAdmin=true;
                                    }
                                    Log.i("isADMIN", String.valueOf(isAdmin));
                                    if(isAdmin){
                                        Intent intent = new Intent(MainActivity.this, AdminHomePage.class);
                                        Log.i("ADMIN YASH", "HEY");
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Intent intent = new Intent(MainActivity.this, HomePage.class);
                                        Log.i("USER YASH", "HEY");
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    public void doLogin(View view) {
        String email = editTextEmail.getText().toString();
        String password = editTextPasswordLogin.getText().toString();
        firebaseAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();

        Log.i("LOGIN", " " + email + " " + password);

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show();
        } else {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = firebaseAuth.getCurrentUser();

                                Log.i("SUCCESS", "Logged in " );
                                db.collection("users")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        if(String.valueOf(document.getData().get("uuid")).equals(user.getUid())  && String.valueOf(document.getData().get("isAdmin")).equals("true") ) {
                                                            Log.i("ADMINNN", "ADMIN");
                                                            isAdmin=true;
                                                        }
                                                        if(isAdmin){
                                                            Intent intent = new Intent(MainActivity.this, AdminHomePage.class);
                                                            Log.i("ADMIN YASH", "HEY");
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else{
                                                            Intent intent = new Intent(MainActivity.this, HomePage.class);
                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });

                            } else {
                                Log.i("FAIL", "Log in failed " + task.getException());
                                Toast.makeText(MainActivity.this, "Failed to Log In", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
}

