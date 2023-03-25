package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.beproject2023.MainActivity.isAdmin;


import java.util.HashMap;
import java.util.Map;

public class ClothInfo extends AppCompatActivity {
    TextView titleTextView;
    ImageView thumbnailImageView;
    TextView descriptionTextView;
    TextView authorTextView;
    TextView publishTextView;
    TextView isbnTextView;
    EditText noOfCopiesEditText;
    Button saveCopiesButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_info);
        String [] bookData=null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            bookData= extras.getStringArray("bookData");
        }
        titleTextView=findViewById(R.id.titleTextView);
        titleTextView.setText(bookData[0]);

        thumbnailImageView=findViewById(R.id.thumbnailImageView);
        Glide.with(getApplicationContext())
                .load(bookData[5])
                .placeholder(R.drawable.image_progress)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(thumbnailImageView);

        descriptionTextView=findViewById(R.id.descriptionTextView);
        descriptionTextView.setText(Html.fromHtml("<b>" + "Description:" + "<br>"+"</b> " + bookData[4]));
        descriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        authorTextView=findViewById(R.id.authorTextView);
        authorTextView.setText(Html.fromHtml("<b>" + "Author(s):"+"</b> " + bookData[1]));

        isbnTextView=findViewById(R.id.isbnTextView);
        isbnTextView.setText(Html.fromHtml("<b>" + "ISBN:"+"</b> " + bookData[6]));

        publishTextView=findViewById(R.id.publishTextView);
        String publishText="";
        if (!bookData[2].equals("na")){
            publishText+=" <b>by</b> "+bookData[2];
        }
        if(!bookData[3].equals("na")){
            publishText+=" <b>dated</b> "+bookData[3];
        }
        if (!publishText.equals("na")) {
            publishTextView.setText(Html.fromHtml("<b>Published</b>" + publishText));
        }
        else{
            publishTextView.setVisibility(View.INVISIBLE);
        }
        noOfCopiesEditText=findViewById(R.id.noOfCopiesInputText);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String[] finalBookData = bookData;
        db.collection("Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int val = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String isbn = String.valueOf(document.getData().get("ISBN"));
                                if(finalBookData[6].equals(isbn)){  //if ISBN exists
                                    val++;
                                }
                            }
                            if(val==0){
                                addBook(finalBookData[6], finalBookData[0]);  //add new book in DB if ISBN doesn't exist
                                noOfCopiesEditText.setText("0");
                            }
                            else{
                                db.collection("Books")
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                int val = 0;
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                                        if(String.valueOf(document.getData().get("ISBN")).equals(finalBookData[6])) {
                                                            String copies = String.valueOf(document.getData().get("Copies"));
                                                            noOfCopiesEditText.setText(copies);
                                                        }
                                                    }
                                                } else {
                                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        saveCopiesButton = findViewById(R.id.saveCopiesButton);

        if(isAdmin) {
            Log.i("Success","is admin");
            noOfCopiesEditText.setInputType(InputType.TYPE_CLASS_NUMBER);


            saveCopiesButton.setVisibility(View.VISIBLE);
            saveCopiesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String noOfCopies = noOfCopiesEditText.getText().toString();
                    if(noOfCopies.equals("")){
                        Toast.makeText(ClothInfo.this, "No. of copies cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                    else if(Integer.parseInt(noOfCopies)<0){
                        Toast.makeText(ClothInfo.this, "No. of copies cannot be less than 0!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        db.collection("Books")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        int val = 0;
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if(String.valueOf(document.getData().get("ISBN")).equals(finalBookData[6])) {
                                                    db.collection("Books").document(document.getId()).update("Copies", noOfCopies);
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                        Toast.makeText(ClothInfo.this, "No. of copies updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            noOfCopiesEditText.setInputType(InputType.TYPE_NULL);
            saveCopiesButton.setVisibility(View.INVISIBLE);
            Log.i("success","is not admin");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    public void addBook(String isbn, String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> mMap = new HashMap<>();
        mMap.put("Name", name);
        mMap.put("ISBN", isbn);
        mMap.put("Copies", "0");
        db.collection("Books").add(mMap);
    }
}