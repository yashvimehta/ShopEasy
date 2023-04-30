package com.example.beproject2023;

import static com.example.beproject2023.UserCustomCardAdapter.buyAllTransact;
import static com.example.beproject2023.UserCustomCardAdapter.transact_barcode;
import static com.example.beproject2023.UserCustomCardAdapter.transact_size;

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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class SignUp extends AppCompatActivity {

    Button signUp;
    TextView goToLogin;
    EditText email, pwd, repwd;
    FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        signUp=findViewById(R.id.signUpButton);
        goToLogin=findViewById(R.id.goToLogin);
        email=findViewById(R.id.editTextSignUpEmailAddress);
        pwd=findViewById(R.id.editTextSignUpPassword);
        repwd=findViewById(R.id.editTextSignUpRePassword);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Log.i("mmmm,", email.toString());

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String em,p,rep;
                em=email.getText().toString();
                p=pwd.getText().toString();
                rep=repwd.getText().toString();
                Log.i("pwd",pwd+"11");
                Log.i("repwd",repwd+"11");
                if (em.toString().equals("") || p.toString().equals("")) {
                    Toast.makeText(SignUp.this, "Please enter all details", Toast.LENGTH_SHORT).show();
                }
                else if(!p.equals(rep)){
                    Toast.makeText(SignUp.this, "Both passwords should match", Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.createUserWithEmailAndPassword(em, p)
                            .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Sign Up successful", Toast.LENGTH_SHORT).show();
                                        Map<String, Object> mMap = new HashMap<>();
                                        mMap.put("email",em);
                                        mMap.put("password",p);
                                        mMap.put("uuid", mAuth.getCurrentUser().getUid());
                                        mMap.put("isAdmin", false);
                                        db.collection("users").add(mMap);
                                        Intent intent = new Intent(SignUp.this, UploadImage.class);
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(SignUp.this, "Sign Up failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}