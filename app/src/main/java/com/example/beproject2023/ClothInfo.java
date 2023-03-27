package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.VTRResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import static com.example.beproject2023.MainActivity.isAdmin;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClothInfo extends AppCompatActivity {
    private static Context contextOfApplication;
    TextView titleTextView;
    ImageView thumbnailImageView;
    TextView colorTextView, sizeTextView , patternTextView , priceTextView;
    EditText inStockInputText;
    Button saveCopiesButton, VTRButton, addToCartButton;

    Bitmap photo_cloth, photo_user;

    StorageReference storage;
    FirebaseUser mUser;

    FirebaseFirestore db;

    String user_image_name;

    public static final int DEFAULT=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_info);

        VTRButton = findViewById(R.id.VTRButton);
        addToCartButton = findViewById(R.id.addToCartButton);

        db = FirebaseFirestore.getInstance();

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
                    photo_cloth = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    thumbnailImageView.setImageBitmap(photo_cloth);
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


        //get user image
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int val = 0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uuid = String.valueOf(document.getData().get("uuid"));
                                if(uuid.equals(mUser.getUid())){
                                    user_image_name = String.valueOf(document.getData().get("image"));
                                    try{
                                        storage = FirebaseStorage.getInstance().getReference().child("user_images/" + user_image_name);
                                        String[] str = user_image_name.split("[.]", 0);
                                        final File localFile= File.createTempFile(str[0],str[1] );
                                        storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                photo_user = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.i("errrorr", e.toString()+"");
                                            }
                                        });
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

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

        String[] finalClothData1 = clothData;
        String[] finalClothData2 = clothData;
        VTRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPredictionsFromServer(finalClothData1[4], finalClothData2);
            }
        });

        String[] finalClothData3 = clothData;
        addToCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("barcode",finalClothData3[5] );
                Log.i("user uid", mUser.getUid());

                db.collection("cart")
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                int val=0;
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        String barcode_fb = String.valueOf(document.getData().get("barcode"));
                                        String uid_fb = String.valueOf(document.getData().get("useruid"));
                                        String size = String.valueOf(document.getData().get("size"));
                                        if(finalClothData3[5].equals(barcode_fb) && uid_fb.equals(mUser.getUid()) && size.equals(dropdown.getSelectedItem().toString())){
                                            val++;
                                        }
                                    }
                                    if(val!=0){
                                        Toast.makeText(ClothInfo.this, "Item already added in cart", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Map<String, Object> mMap = new HashMap<>();
                                        mMap.put("barcode", finalClothData3[5]);
                                        mMap.put("size", dropdown.getSelectedItem().toString());
                                        mMap.put("useruid", mUser.getUid());
                                        db.collection("cart").add(mMap);
                                        Toast.makeText(ClothInfo.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void getPredictionsFromServer(String s,String[] clothData ) {

        try {

            Uri tempUri_cloth = saveBitmapImage(ClothInfo.contextOfApplication, photo_cloth);
            Uri tempUri_user = saveBitmapImage(ClothInfo.contextOfApplication, photo_user);

            Log.i("lol", tempUri_cloth.getPath());
            Log.i("lol", tempUri_user.getPath());

            String filePath_cloth = getFilePathFromUri(tempUri_cloth);
            String filePath_user = getFilePathFromUri(tempUri_user);

            final File file_cloth = new File(filePath_cloth);
            final File file_user = new File(filePath_user);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            RequestBody requestFile_cloth = RequestBody.create(MediaType.parse("multipart/form-data"), file_cloth);
            RequestBody requestFile_user = RequestBody.create(MediaType.parse("multipart/form-data"), file_user);
            MultipartBody.Part body_cloth = MultipartBody.Part.createFormData("img2", s, requestFile_cloth);
            MultipartBody.Part body_user = MultipartBody.Part.createFormData("img1", user_image_name, requestFile_user);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<VTRResult> mCall = apiInterface.sendVTRImage(body_user, body_cloth);
            mCall.enqueue(new Callback<VTRResult>() {
                @Override
                public void onResponse(Call<VTRResult> call, Response<VTRResult> response) {
                    VTRResult mResult = response.body();
                    if (mResult.getGeneralSuccess()) {
                        Log.i("Success Checking", mResult.getVTRText() );

                        byte [] encodeByte = Base64.decode(mResult.getVTRText(),DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                        Intent intent = new Intent(ClothInfo.this, VTR.class);
                        intent.putExtra("VTRImage", bitmap);
                        intent.putExtra("ClothData", clothData);
                        startActivity(intent);

                    } else {
                        Log.i("Failure Checking", mResult.getVTRError()+" ");

                    }
                    if (file_cloth.exists()) {
                        file_cloth.delete();
                    }
                }

                @Override
                public void onFailure(Call<VTRResult> call, Throwable t) {
                    Log.i("Failure Checking", "There was an error " + t.getMessage());

                    if (file_cloth.exists()) {
                        file_cloth.delete();
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();

            String text = "There was some error";
        }

    }

    public Uri saveBitmapImage(Context inContext, Bitmap inImage) {
        Log.i("SAVE", "saving image...");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), inImage, ts, null);
        Log.i("lol5",path );
        return Uri.parse(path);
    }

    public String getFilePathFromUri(Uri uri) {
        String path = "";
        if (getApplicationContext().getContentResolver() != null) {
            Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }

        return path;
    }

}