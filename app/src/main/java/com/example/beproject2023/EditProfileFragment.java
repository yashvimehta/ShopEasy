package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class EditProfileFragment extends Fragment {
    Button logoutButton;
    EditText pwdInputText;
    EditText confirmPwdInputText;
    Button editProfileButton, cancelButton, confirmButton;
    FirebaseAuth firebaseAuth;

    TextView changeProfileImageText;

    ImageView camera, gallery, profile;

    FirebaseFirestore db;

    String flie_name, old_image_name;

    Uri currentImageUri;

    Bitmap image;


    StorageReference storageRef;

    FirebaseStorage storage;

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int DEFAULT=0;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();
        final View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        pwdInputText = view.findViewById(R.id.pwdInputText);
        confirmPwdInputText = view.findViewById(R.id.confirmPwdInputText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        logoutButton = view.findViewById(R.id.logoutButton);

        cancelButton = view.findViewById(R.id.cancelButton);
        confirmButton = view.findViewById(R.id.confirmButton);

        camera = view.findViewById(R.id.cameraImageView);
        gallery = view.findViewById(R.id.galleryImageView);
        profile = view.findViewById(R.id.profileImageView);

        storage = FirebaseStorage.getInstance();

        changeProfileImageText = view.findViewById(R.id.changeProfileImageText);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        db= FirebaseFirestore.getInstance();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                retryButton.setVisibility(View.INVISIBLE);
                changeProfileImageText.setVisibility(View.INVISIBLE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (HomePage.contextOfApplication.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);

                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                retryButton.setVisibility(View.INVISIBLE);
                changeProfileImageText.setVisibility(View.INVISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (HomePage.contextOfApplication.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST);

                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECT_FILE);
                    }
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile.setVisibility(View.INVISIBLE);
                camera.setVisibility(View.VISIBLE);
                gallery.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                confirmButton.setVisibility(View.INVISIBLE);
                changeProfileImageText.setVisibility(View.VISIBLE);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profile.setVisibility(View.INVISIBLE);
                camera.setVisibility(View.VISIBLE);
                gallery.setVisibility(View.VISIBLE);
                cancelButton.setVisibility(View.INVISIBLE);
                confirmButton.setVisibility(View.INVISIBLE);
                changeProfileImageText.setVisibility(View.VISIBLE);

                //add to db

                db.collection("users")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    int val=0;
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String uuid = String.valueOf(document.getData().get("uuid"));
                                        if(user.getUid().equals(uuid)){
                                            old_image_name = String.valueOf(document.getData().get("image"));
                                            db.collection("users").document(document.getId()).update("image", flie_name);
                                            StorageReference desertRef = storageRef.child("user_images/"+old_image_name);
                                            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("YAY", "Delete successful");
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    Log.i("NAY", "Delete unsuccessful");
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });

                storageRef = storage.getReference();
                StorageReference imageRef = storageRef.child("user_images/"+flie_name);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();

                UploadTask uploadTask = imageRef.putBytes(data);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });

                // Delete the file

            }
        });



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

                    db.collection("users")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String uuid = String.valueOf(document.getData().get("uuid"));
                                            if(user.getUid().equals(uuid)){
                                                db.collection("users").document(document.getId()).update("password", pwd);
                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                    Toast.makeText(getActivity(), "Changes stored successfully", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageLocation = data.getData();
        currentImageUri=imageLocation;
        Bitmap photo = null;
        Cursor cursor = HomePage.contextOfApplication.getContentResolver().query(data.getData(), null, null, null, null);
        try {
            photo = MediaStore.Images.Media.getBitmap(HomePage.contextOfApplication.getContentResolver(), currentImageUri);
            profile.setImageBitmap(photo);
            image = photo;
            Log.i("hi there", "p");
            camera.setVisibility(View.INVISIBLE);
            profile.setVisibility(View.VISIBLE);
            gallery.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            flie_name = cursor.getString(nameIndex);
            Log.i("frrrr", flie_name);
            cursor.close();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            } else {
                Toast.makeText(getActivity(), "Provide permission to access camera!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);

            } else {
                Toast.makeText(getActivity(), "Provide permission to access your images!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}