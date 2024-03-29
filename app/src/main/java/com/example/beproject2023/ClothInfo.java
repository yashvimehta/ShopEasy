package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.RecommendationResult;
import com.example.beproject2023.ApiHelper.VTRResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.beproject2023.MainActivity.isAdmin;
//import com.example.beproject2023.BgRemover;

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
    Bitmap finalBit;

//    public ClothInfo(Bitmap bit){
//        finalBit = bit;
//    }
    private static Context contextOfApplication;
    TextView titleTextView, textView2, similarProducts;
    ImageView thumbnailImageView, recommendImageView1, recommendImageView2, recommendImageView3, recommendImageView4, recommendImageView5,vtrImageView;
    EditText inStockInputText, colorInputText, priceInputText , patternInputText , sizeInputText;
    Button saveChangesButton, VTRButton, addToCartButton,backButton;
    TextInputLayout noOfCopiesTextInputLayout,colorTextInputLayout,patternTextInputLayout,sizeTextInputLayout,priceTextInputLayout;
    Spinner spinner;
    HorizontalScrollView scrollView;
    public Bitmap photo_cloth, photo_user;

    StorageReference storage;
    FirebaseUser mUser;

    FirebaseFirestore db;

    String user_image_name, image_name1 , image_name2 , image_name3 , image_name4 , image_name5;
    Spinner dropdown;

    public static final int DEFAULT=0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_info);

        VTRButton = findViewById(R.id.VTRButton);
        addToCartButton = findViewById(R.id.addToCartButton);
        saveChangesButton= findViewById(R.id.saveChangesButton);

        recommendImageView1 = findViewById(R.id.recommendImageView1);
        recommendImageView2 = findViewById(R.id.recommendImageView2);
        recommendImageView3 = findViewById(R.id.recommendImageView3);
        recommendImageView4 = findViewById(R.id.recommendImageView4);
        recommendImageView5 = findViewById(R.id.recommendImageView5);
        noOfCopiesTextInputLayout=findViewById(R.id.noOfCopiesTextInputLayout);
        colorTextInputLayout=findViewById(R.id.colorTextInputLayout);
        patternTextInputLayout=findViewById(R.id.patternTextInputLayout);
        sizeTextInputLayout=findViewById(R.id.sizeTextInputLayout);
        priceTextInputLayout=findViewById(R.id.priceTextInputLayout);

        inStockInputText=findViewById(R.id.inStockInputText);
        priceInputText = findViewById(R.id.priceInputText);
        colorInputText = findViewById(R.id.colorInputText);
        patternInputText = findViewById(R.id.patternInputText);
        sizeInputText = findViewById(R.id.sizeInputText);
        vtrImageView=findViewById(R.id.vtrImageView);
        backButton=findViewById(R.id.backButton);
        spinner=findViewById(R.id.spinner);
        textView2= findViewById(R.id.textView2);
        similarProducts = findViewById(R.id.similarProducts);
        scrollView= (HorizontalScrollView) findViewById(R.id.scrollView);
        db = FirebaseFirestore.getInstance();

        String [] clothData=null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clothData= extras.getStringArray("clothData");
        }
        titleTextView=findViewById(R.id.titleTextView);
        titleTextView.setText(clothData[0] + " " + clothData[1]);

        dropdown = findViewById(R.id.spinner);
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
//                    BgRemover bgRemover = new BgRemover(photo_cloth);
//                    thumbnailImageView.setImageBitmap(finalBit);

//                    BackgroundRemover.bitmapForProcessing(
//                            bitmap,
//                            object: OnBackgroundChangeListener{
//                        override fun onSuccess(bitmap: Bitmap) {
//                            //do what ever you want to do with this bitmap
//                        }
//
//                        override fun onFailed(exception: Exception) {
//                            //exception
//                        }
//                    }
//)
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

        //String textString = "<b>Color: </b>"+ clothData[0];
        colorInputText.setText(clothData[0]);
        //textString="<b>Sizes: </b>"+ clothData[3].replace(" ",", ");
        sizeInputText.setText(clothData[3]);
        //textString="<b>Pattern: </b>"+ clothData[1];
        patternInputText.setText(clothData[1]);
        //textString="<b>Price: </b>\u20B9"+ clothData[2];
        priceInputText.setText(clothData[2]);
        inStockInputText.setText(clothData[6]);

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
        if(isAdmin) {

            Log.i("Success","is admin");
            textView2.setVisibility(View.INVISIBLE);
            dropdown.setVisibility(View.INVISIBLE);
            VTRButton.setVisibility(View.INVISIBLE);
            addToCartButton.setVisibility(View.INVISIBLE);
            similarProducts.setVisibility(View.INVISIBLE);
            scrollView.setVisibility(View.GONE);
            spinner.setVisibility(View.INVISIBLE);
            recommendImageView1.setVisibility(View.INVISIBLE);
            recommendImageView2.setVisibility(View.INVISIBLE);
            recommendImageView3.setVisibility(View.INVISIBLE);
            recommendImageView4.setVisibility(View.INVISIBLE);
            recommendImageView5.setVisibility(View.INVISIBLE);

            inStockInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
            priceInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
            sizeInputText.setInputType(InputType.TYPE_CLASS_TEXT);

            colorInputText.setInputType(InputType.TYPE_NULL);
            patternInputText.setInputType(InputType.TYPE_CLASS_TEXT);

            saveChangesButton.setVisibility(View.VISIBLE);
            String[] finalClothData = clothData;
            saveChangesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String noOfStock = inStockInputText.getText().toString();
                    String price = priceInputText.getText().toString();
                    String size = sizeInputText.getText().toString();
                    String pattern=patternInputText.getText().toString();
                    if(noOfStock.equals("") || price.equals("") || size.equals("") ||pattern.equals("")){
                        Toast.makeText(ClothInfo.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                    else if(Integer.parseInt(noOfStock)<0){
                        Toast.makeText(ClothInfo.this, "No. of stock cannot be less than 0!", Toast.LENGTH_SHORT).show();
                    }
                    else if(Integer.parseInt(price)<0){
                        Toast.makeText(ClothInfo.this, "Price cannot be less than 0!", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        db.collection("clothes")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        int val = 0;
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                if(String.valueOf(document.getData().get("barcode")).equals(finalClothData[5])) {
                                                    db.collection("clothes").document(document.getId()).update("in_stock", noOfStock);
                                                    db.collection("clothes").document(document.getId()).update("price", price);
                                                    db.collection("clothes").document(document.getId()).update("size", size);
                                                    db.collection("clothes").document(document.getId()).update("pattern", pattern);
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                    }
                                });
                        Toast.makeText(ClothInfo.this, "Fields updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            inStockInputText.setInputType(InputType.TYPE_NULL);
            colorInputText.setInputType(InputType.TYPE_NULL);
            patternInputText.setInputType(InputType.TYPE_NULL);
            priceInputText.setInputType(InputType.TYPE_NULL);
            sizeInputText.setInputType(InputType.TYPE_NULL);
            saveChangesButton.setVisibility(View.INVISIBLE);
            Log.i("success","is not admin");
        }

        getPredictionsFromServer1(clothData);
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
                                    Map<String, Object> mMap = new HashMap<>();
                                    mMap.put("barcode", finalClothData3[5]);
                                    mMap.put("size", dropdown.getSelectedItem().toString());
                                    mMap.put("useruid", mUser.getUid());
                                    db.collection("cart").add(mMap);
                                    Toast.makeText(ClothInfo.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d(TAG, "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        recommendImageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) recommendImageView1.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                onClickRecommendation(image_name1, bitmap);
            }
        });
        recommendImageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) recommendImageView1.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                onClickRecommendation(image_name2, bitmap);
            }
        });
        recommendImageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) recommendImageView1.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                onClickRecommendation(image_name3, bitmap);
            }
        });
        recommendImageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) recommendImageView1.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                onClickRecommendation(image_name4, bitmap);
            }
        });
        recommendImageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapDrawable drawable = (BitmapDrawable) recommendImageView1.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                onClickRecommendation(image_name5, bitmap);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void getPredictionsFromServer(String s,String[] clothData ) {

        thumbnailImageView.setVisibility(View.INVISIBLE);
        vtrImageView.setVisibility(View.VISIBLE);
        noOfCopiesTextInputLayout.setVisibility(View.INVISIBLE);
        colorTextInputLayout.setVisibility(View.INVISIBLE);
        sizeTextInputLayout.setVisibility(View.INVISIBLE);
        patternTextInputLayout.setVisibility(View.INVISIBLE);
        priceTextInputLayout.setVisibility(View.INVISIBLE);
        colorInputText.setVisibility(View.INVISIBLE);
        sizeInputText.setVisibility(View.INVISIBLE);
        patternInputText.setVisibility(View.INVISIBLE);
        priceInputText.setVisibility(View.INVISIBLE);
        VTRButton.setVisibility(View.INVISIBLE);
        addToCartButton.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);
        similarProducts.setVisibility(View.INVISIBLE);
        scrollView.setVisibility(View.INVISIBLE);
        recommendImageView1.setVisibility(View.INVISIBLE);
        recommendImageView2.setVisibility(View.INVISIBLE);
        recommendImageView3.setVisibility(View.INVISIBLE);
        recommendImageView4.setVisibility(View.INVISIBLE);
        recommendImageView5.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);

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
                        vtrImageView.setImageBitmap(bitmap);
                        backButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setVTRInvsible();

                            }
                        });
//                        Intent intent = new Intent(ClothInfo.this, VTR.class);
//                        intent.putExtra("VTRImage", bitmap);
//
//                        intent.putExtra("ClothData", clothData);
//                        startActivity(intent);

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
                    Toast.makeText(ClothInfo.this, "There was some error", Toast.LENGTH_SHORT).show();
                    setVTRInvsible();
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
    public void setVTRInvsible(){
        backButton.setVisibility(View.INVISIBLE);
        vtrImageView.setVisibility(View.INVISIBLE);
        thumbnailImageView.setVisibility(View.VISIBLE);
        vtrImageView.setImageResource(R.drawable.loading);
        noOfCopiesTextInputLayout.setVisibility(View.VISIBLE);
        colorTextInputLayout.setVisibility(View.VISIBLE);
        sizeTextInputLayout.setVisibility(View.VISIBLE);
        patternTextInputLayout.setVisibility(View.VISIBLE);
        priceTextInputLayout.setVisibility(View.VISIBLE);
        colorInputText.setVisibility(View.VISIBLE);
        sizeInputText.setVisibility(View.VISIBLE);
        patternInputText.setVisibility(View.VISIBLE);
        priceInputText.setVisibility(View.VISIBLE);
        VTRButton.setVisibility(View.VISIBLE);
        addToCartButton.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        textView2.setVisibility(View.VISIBLE);
        similarProducts.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        recommendImageView1.setVisibility(View.VISIBLE);
        recommendImageView2.setVisibility(View.VISIBLE);
        recommendImageView3.setVisibility(View.VISIBLE);
        recommendImageView4.setVisibility(View.VISIBLE);
        recommendImageView5.setVisibility(View.VISIBLE);
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

    public void getPredictionsFromServer1(String[] clothData) {

        try {

            storage = FirebaseStorage.getInstance().getReference().child("cloth_images/" + clothData[4]);
            String[] str = clothData[4].split("[.]", 0);
            final File localFile= File.createTempFile(str[0],str[1] );
            storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    photo_cloth = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                    BackgroundRemover.bitmapForProcessing(
//                            bitmap,
//                            object: OnBackgroundChangeListener{
//                        override fun onSuccess(bitmap: Bitmap) {
//                            //do what ever you want to do with this bitmap
//                        }
//
//                        override fun onFailed(exception: Exception) {
//                            //exception
//                        }
//                    }
//)
                    thumbnailImageView.setImageBitmap(photo_cloth);
                    Bitmap bmap = photo_cloth;
                    Log.i("THErE", "THERE");

                    Uri tempUri = saveBitmapImage(ClothInfo.contextOfApplication, bmap);
                    String filePath = getFilePathFromUri(tempUri);

                    final File file = new File(filePath);
                    final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .readTimeout(60, TimeUnit.SECONDS)
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .build();
                    RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part body_cloth = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                            .addConverterFactory(GsonConverterFactory.create())
                            .client(okHttpClient)
                            .build();

                    ApiInterface apiInterface = retrofit.create(ApiInterface.class);
                    Call<RecommendationResult> mCall = apiInterface.sendRecImage(body_cloth);
                    mCall.enqueue(new Callback<RecommendationResult>() {
                        @Override
                        public void onResponse(Call<RecommendationResult> call, Response<RecommendationResult> response) {
                            Log.i("yashvi1", "1");
                            RecommendationResult mResult = response.body();
                            Log.i("yashvi", mResult.toString() +"");
                            if (mResult.getGeneralSuccess()) {
                                Log.i("Success Checking", mResult.getRecommendationText().toString() +"");

                                byte [] encodeByte1 = Base64.decode(mResult.getRecommendationText().get(0),DEFAULT);
                                Bitmap bitmap1 = BitmapFactory.decodeByteArray(encodeByte1, 0, encodeByte1.length);

                                byte [] encodeByte2 = Base64.decode(mResult.getRecommendationText().get(1),DEFAULT);
                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(encodeByte2, 0, encodeByte2.length);

                                byte [] encodeByte3 = Base64.decode(mResult.getRecommendationText().get(2),DEFAULT);
                                Bitmap bitmap3 = BitmapFactory.decodeByteArray(encodeByte3, 0, encodeByte3.length);

                                byte [] encodeByte4 = Base64.decode(mResult.getRecommendationText().get(3),DEFAULT);
                                Bitmap bitmap4 = BitmapFactory.decodeByteArray(encodeByte4, 0, encodeByte4.length);

                                byte [] encodeByte5 = Base64.decode(mResult.getRecommendationText().get(4),DEFAULT);
                                Bitmap bitmap5 = BitmapFactory.decodeByteArray(encodeByte5, 0, encodeByte5.length);

                                recommendImageView1.setImageBitmap(bitmap1);
                                recommendImageView2.setImageBitmap(bitmap2);
                                recommendImageView3.setImageBitmap(bitmap3);
                                recommendImageView4.setImageBitmap(bitmap4);
                                recommendImageView5.setImageBitmap(bitmap5);

                                image_name1 = mResult.getRecommendationText().get(5);
                                image_name2 = mResult.getRecommendationText().get(6);
                                image_name3 = mResult.getRecommendationText().get(7);
                                image_name4 = mResult.getRecommendationText().get(8);
                                image_name5 = mResult.getRecommendationText().get(9);


                                Log.i("Image1",image_name1);
                                Log.i("Image2",image_name2);
                                Log.i("Image3",image_name3);
                                Log.i("Image4",image_name4);
                                Log.i("Image5",image_name5);
                            } else {
                                String text = "Failure";
                                Log.i("Success Checking", mResult.getRecommendationError()+"");

                            }
                            if (file.exists()) {
                                file.delete();
                            }
                        }

                        @Override
                        public void onFailure(Call<RecommendationResult> call, Throwable t) {
                            Log.i("Failure Checking", "There was an error " + t.getMessage());
                            String text = "There was some error";
                            if (file.exists()) {
                                file.delete();
                            }

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    Log.i("errrorr", e.toString()+"");
                    String text = "There was some error";
                }
            });



        } catch (Exception e) {
            e.printStackTrace();
            Log.i("errrrrorr", e.toString());
            String text = "There was some error";
        }

    }

    public void onClickRecommendation(String image_name, Bitmap image){
        db.collection("clothes")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        int val=0;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String image_name_1 = String.valueOf(document.getData().get("image_name"));
                                if(image_name.equals(image_name_1)){
                                    val++;
                                    //item already in db, just pass it to intent
                                    String color = String.valueOf(document.getData().get("color"));
                                    String pattern = String.valueOf(document.getData().get("pattern"));
                                    String price = String.valueOf(document.getData().get("price"));
                                    String size = String.valueOf(document.getData().get("size"));
                                    String barcode = String.valueOf(document.getData().get("barcode"));
                                    String in_stock = String.valueOf(document.getData().get("in_stock"));
                                    String [] clothData1 = new String[]{color, pattern, price ,size , image_name , barcode , in_stock};
                                    Intent intent = new Intent(getApplicationContext(), ClothInfo.class);
                                    intent.putExtra("clothData", clothData1);
                                    startActivity(intent);

                                }
                            }
                            if(val==0){
                                //item not present in db, so add the item
                                Map<String, Object> mMap = new HashMap<>();
                                mMap.put("barcode","4005404003028");
                                mMap.put("size", "S M L");
                                mMap.put("price", "400");
                                mMap.put("pattern", "Sleeves");
                                mMap.put("color", "Red");
                                mMap.put("in_stock", "3");
                                mMap.put("image_name", image_name);
                                db.collection("clothes").add(mMap);
                                String [] clothData1 = new String[]{"Black", "Sleeves", "400" ,"S M L" , image_name , "4005404003028" , "3"};

                                //add to storage
                                storage = FirebaseStorage.getInstance().getReference().child("cloth_images/" + image_name);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] data = baos.toByteArray();

                                UploadTask uploadTask = storage.putBytes(data);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Intent intent = new Intent(getApplicationContext(), ClothInfo.class);
                                        intent.putExtra("clothData", clothData1);
                                        startActivity(intent);
                                    }
                                });
                            }
                        } else {
                            Log.i("Error", "Error getting documents");
                        }
                    }
                });
    }

}