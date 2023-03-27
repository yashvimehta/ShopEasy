package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.quickbook.MainActivity;
import com.example.quickbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UserCustomCardAdapter extends ArrayAdapter<String[]> {

    Context mContext;
    ArrayList<String[]>mArrayList;
    Button buyNow;
    FirebaseFirestore db;
    public static String rzpID;
    public static Button rzpButton;
    public UserCustomCardAdapter(@NonNull Context context, ArrayList<String[]> stringArrayList) {
        super(context, R.layout.content_user_custom_card_adapter, stringArrayList);
        this.mContext = context;
        this.mArrayList = stringArrayList;
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this.getContext());
        @SuppressLint("ViewHolder") View view = mLayoutInflater.inflate(R.layout.content_user_custom_card_adapter, null, true);

        TextView clothName = view.findViewById(R.id.clothName);
        TextView clothDesc = view.findViewById(R.id.clothDesc);

        clothName.setText(mArrayList.get(position)[0] + " " + mArrayList.get(position)[1]);
        clothDesc.setText("Price: "+mArrayList.get(position)[2] + "\tSize: "+ mArrayList.get(position)[3] );

        buyNow = view.findViewById(R.id.buyNow);

        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        return view;
    }

}
