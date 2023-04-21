package com.example.beproject2023;
import static android.content.ContentValues.TAG;
import static com.example.beproject2023.SearchPageFragment.LocIn;
import static com.example.beproject2023.UserCustomCardAdapter.itemm;
import static com.example.beproject2023.UserCustomCardAdapter.price;
import static com.example.beproject2023.UserCustomCardAdapter.quantity;
import static com.example.beproject2023.UserCustomCardAdapter.transact_document_id;
import static com.example.beproject2023.UserCustomCardAdapter.transact_barcode;
import static com.example.beproject2023.UserCustomCardAdapter.transact_size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.BarcodeResult;
import com.example.beproject2023.ApiHelper.Billing;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomePage extends AppCompatActivity implements PaymentResultWithDataListener {

    public static Context contextOfApplication;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        ShoppingFragmentAdapter adapter = new ShoppingFragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(1);
        contextOfApplication = getApplicationContext();
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        // decrement in_stock by 1 for that cloth
        for(int j=0;j<transact_barcode.size();j++){
            int finalJ = j;
            db.collection("clothes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String barcode1 = String.valueOf(document.getData().get("barcode"));
                                    String in_stock = String.valueOf(document.getData().get("in_stock"));
                                    if(barcode1.equals(transact_barcode.get(finalJ))){
                                        db.collection("clothes").document(document.getId()).update("in_stock", Integer.parseInt(in_stock)-1);

                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
            //delete from cart
            db.collection("cart").document(transact_document_id.get(j)).delete();



            //add to itemsBought collection
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("barcode",transact_barcode.get(j));
            mMap.put("size",transact_size.get(j));
            mMap.put("useruid", firebaseAuth.getCurrentUser().getUid());
            //TODO ADD Date
            Calendar c = Calendar.getInstance();
            mMap.put("date",c.getTime());
            db.collection("itemsBought").add(mMap);
            Log.i("pppp11111", "kkk1111");
        }
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Log.i("pooo",  TextUtils.join(",", itemm));
        Call<Billing> mCall = apiInterface.sendMail("yashvimehta45@gmail.com", TextUtils.join(",", itemm),TextUtils.join(",", quantity),TextUtils.join(",", price));
        mCall.enqueue(new Callback<Billing>() {
            @Override
            public void onResponse(Call<Billing> call, Response<Billing> response) {
                Billing mResult = response.body();
                if (mResult.getGeneralSuccess()) {
                } else {
                    String text = "Failure";

                }
            }

            @Override
            public void onFailure(Call<Billing> call, Throwable t) {

            }
        });

        if(LocIn){
            Toast.makeText(HomePage.this, "Bill has been mailed to you, please show it at the counter", Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(HomePage.this, "Your items will be delivered to you soon", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {

    }
}
