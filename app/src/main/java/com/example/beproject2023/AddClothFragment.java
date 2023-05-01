package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import static com.example.beproject2023.UserCustomCardAdapter.transact_barcode;
import static com.example.beproject2023.UserCustomCardAdapter.transact_size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddClothFragment extends Fragment {

    EditText inStockInputText, colorInputText, priceInputText , patternInputText , sizeInputText, barcodeInputText;
    Button saveChanges;
    FirebaseFirestore db;
    ImageView camera, gallery, image;
    TextView textDisplay;
    Uri currentImageUri;
    Bitmap bitmapImage;
    StorageReference storageRef;
    FirebaseStorage storage;
    String flie_name;

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_cloth, container, false);
        inStockInputText=view.findViewById(R.id.inStockInputText);
        priceInputText = view.findViewById(R.id.priceInputText);
        colorInputText = view.findViewById(R.id.colorInputText);
        patternInputText = view.findViewById(R.id.patternInputText);
        sizeInputText = view.findViewById(R.id.sizeInputText);
        barcodeInputText = view.findViewById(R.id.barcodeInputText);
        saveChanges = view.findViewById(R.id.saveChanges);
        camera = view.findViewById(R.id.cameraImageView);
        gallery = view.findViewById(R.id.galleryImageView);
        image = view.findViewById(R.id.image);
        textDisplay = view.findViewById(R.id.textDisplay);
        inStockInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
        priceInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
        sizeInputText.setInputType(InputType.TYPE_CLASS_TEXT);
        db = FirebaseFirestore.getInstance();
        inStockInputText.setText("");
        priceInputText.setText("");
        sizeInputText.setText("");
        patternInputText.setText("");
        barcodeInputText.setText("");
        colorInputText.setText("");

        storage = FirebaseStorage.getInstance();

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noOfStock = inStockInputText.getText().toString();
                String price = priceInputText.getText().toString();
                String size = sizeInputText.getText().toString();
                String pattern=patternInputText.getText().toString();
                String barcode = barcodeInputText.getText().toString();
                String color=colorInputText.getText().toString();
                if(noOfStock.equals(" ") || price.equals(" ") || size.equals(" ") || pattern.equals(" ") || barcode.equals(" ") || color.equals(" ")){
                    Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();

                }
                else if(Integer.parseInt(noOfStock)<0){
                    Toast.makeText(getContext(), "No. of stock cannot be less than 0!", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.parseInt(price)<0){
                    Toast.makeText(getContext(), "Price cannot be less than 0!", Toast.LENGTH_SHORT).show();
                }
                else if(image.getVisibility() != View.VISIBLE){
                    Toast.makeText(getContext(), "Upload an image!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Map<String, Object> mMap = new HashMap<>();
                    mMap.put("barcode",barcode);
                    mMap.put("size",size);
                    mMap.put("in_stock", noOfStock);
                    mMap.put("pattern",pattern);
                    mMap.put("price",price);
                    mMap.put("color", color);
                    mMap.put("image_name", flie_name);
                    db.collection("clothes").add(mMap);
                    storageRef = storage.getReference();
                    StorageReference imageRef = storageRef.child("cloth_images/"+flie_name);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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
                    Toast.makeText(getContext(), "Fields added!", Toast.LENGTH_SHORT).show();
                    inStockInputText.setText("");
                    priceInputText.setText("");
                    sizeInputText.setText("");
                    patternInputText.setText("");
                    barcodeInputText.setText("");
                    colorInputText.setText("");
                    image.setVisibility(View.INVISIBLE);
                    camera.setVisibility(View.VISIBLE);
                    gallery.setVisibility(View.VISIBLE);
                    textDisplay.setVisibility(View.VISIBLE);

                }
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                retryButton.setVisibility(View.INVISIBLE);
                textDisplay.setVisibility(View.INVISIBLE);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (AdminHomePage.contextOfApplication.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                textDisplay.setVisibility(View.INVISIBLE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (getContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST);

                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECT_FILE);
                    }
                }

            }
        });

        return view;
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            } else {
                Toast.makeText(getContext(), "Provide permission to access camera!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);

            } else {
                Toast.makeText(getContext(), "Provide permission to access your images!", Toast.LENGTH_LONG).show();
            }
        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageLocation = data.getData();
        currentImageUri=imageLocation;
        Bitmap photo = null;
        Cursor cursor = AdminHomePage.contextOfApplication.getContentResolver().query(data.getData(), null, null, null, null);
        try {
            photo = MediaStore.Images.Media.getBitmap(AdminHomePage.contextOfApplication.getContentResolver(), currentImageUri);
            image.setImageBitmap(photo);
            bitmapImage = photo;
            Log.i("hi there", "p");
            camera.setVisibility(View.INVISIBLE);
            image.setVisibility(View.VISIBLE);
            gallery.setVisibility(View.INVISIBLE);
            textDisplay.setVisibility(View.INVISIBLE);
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
}