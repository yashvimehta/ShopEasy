package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.beproject2023.MainActivity.isAdmin;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ClothInfo extends AppCompatActivity {
    TextView titleTextView;
    ImageView thumbnailImageView;
    TextView colorTextView, sizeTextView , patternTextView , priceTextView;
    EditText inStockInputText;
    Button saveCopiesButton;

    StorageReference storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_info);


        String [] clothData=null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clothData= extras.getStringArray("clothData");
        }
        titleTextView=findViewById(R.id.titleTextView);
        titleTextView.setText(clothData[0] + " " + clothData[1]);

        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = clothData[3].split( " ");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        dropdown.setAdapter(adapter);

        thumbnailImageView = findViewById(R.id.thumbnailImageView);
        try{
            storage = FirebaseStorage.getInstance().getReference().child("cloth_images/" + clothData[4]);
            String[] str = clothData[4].split("[.]", 0);
            final File localFile= File.createTempFile(str[0],str[1] );
            storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    thumbnailImageView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Log.i("errrorr", e.toString()+"");
                    String text = "There was some error";
                }
            });
        }
        catch (Exception e){
            e.printStackTrace();
        }

        colorTextView = findViewById(R.id.colorTextView);
        colorTextView.setText(Html.fromHtml("<b>" + "Color:"+"</b> " + clothData[0]));

        sizeTextView = findViewById(R.id.sizeTextView);
        sizeTextView.setText(Html.fromHtml("<b>" + "Size(s):"+"</b> " + clothData[3]));

        patternTextView = findViewById(R.id.patternTextView);
        patternTextView.setText(Html.fromHtml("<b>" + "Pattern(s):"+"</b> " + clothData[1]));

        priceTextView = findViewById(R.id.priceTextView);
        priceTextView.setText(Html.fromHtml("<b>" + "Color:"+"</b> " + clothData[2]));



//        publishTextView=findViewById(R.id.patternTextView);
//        String publishText="";
//        if (!bookData[2].equals("na")){
//            publishText+=" <b>by</b> "+bookData[2];
//        }
//        if(!bookData[3].equals("na")){
//            publishText+=" <b>dated</b> "+bookData[3];
//        }
//        if (!publishText.equals("na")) {
//            publishTextView.setText(Html.fromHtml("<b>Published</b>" + publishText));
//        }
//        else{
//            publishTextView.setVisibility(View.INVISIBLE);
//        }
        inStockInputText=findViewById(R.id.inStockInputText);


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String[] finalClothData = clothData;
        db.collection("clothes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int val = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String barcode = String.valueOf(document.getData().get("barcode"));
                                if(finalClothData[5].equals(barcode)){  //if ISBN exists
                                    String inStock = String.valueOf(document.getData().get("in_stock"));
                                    inStockInputText.setText(inStock);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        saveCopiesButton = findViewById(R.id.saveCopiesButton);

        if(isAdmin) {
//            Log.i("Success","is admin");
//            inStockInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
//
//
//            saveCopiesButton.setVisibility(View.VISIBLE);
//            saveCopiesButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String noOfCopies = inStockInputText.getText().toString();
//                    if(noOfCopies.equals("")){
//                        Toast.makeText(ClothInfo.this, "No. of copies cannot be empty", Toast.LENGTH_SHORT).show();
//                    }
//                    else if(Integer.parseInt(noOfCopies)<0){
//                        Toast.makeText(ClothInfo.this, "No. of copies cannot be less than 0!", Toast.LENGTH_SHORT).show();
//                    }
//                    else{
//                        db.collection("Books")
//                                .get()
//                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                                        int val = 0;
//                                        if (task.isSuccessful()) {
//                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                                if(String.valueOf(document.getData().get("ISBN")).equals(finalBookData[6])) {
//                                                    db.collection("Books").document(document.getId()).update("Copies", noOfCopies);
//                                                }
//                                            }
//                                        } else {
//                                            Log.d(TAG, "Error getting documents: ", task.getException());
//                                        }
//                                    }
//                                });
//                        Toast.makeText(ClothInfo.this, "No. of copies updated!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
        }
        else{
//            noOfCopiesEditText.setInputType(InputType.TYPE_NULL);
//            saveCopiesButton.setVisibility(View.INVISIBLE);
//            Log.i("success","is not admin");
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