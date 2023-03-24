package com.example.beproject2023;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.beproject2023.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class EditProfileFragment extends Fragment {
    Button logoutButton;
    EditText pwdInputText;
    EditText confirmPwdInputText;
    Button editProfileButton;
    FirebaseAuth firebaseAuth;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        final View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        pwdInputText = view.findViewById(R.id.pwdInputText);
        confirmPwdInputText = view.findViewById(R.id.confirmPwdInputText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pwd = pwdInputText.getText().toString();
                String confirmPwd = confirmPwdInputText.getText().toString();
                boolean correct = true;
                String message = "";
                if (pwd.equals("") || confirmPwd.equals("")) {
                    message = message.concat("Fields cannot be empty. ");
                    correct = false;
                }
                if (!pwd.equals(confirmPwd)) {
                    message = message.concat("Passwords are not matching");
                    correct = false;
                }
                if(pwd.length()<6){
                    message=message.concat("Password cannot be less than 6 characters");
                    correct=false;
                }
                if (correct) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    user.updatePassword(pwd)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("YAYAY", "Password address updated.");
                                    }
                                }
                            });

                    db.collection("Users").document(user.getUid()).update("password", pwd);
                    Toast.makeText(getActivity(), "Changes stored successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}