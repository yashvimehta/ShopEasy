package com.example.beproject2023;

import static android.content.ContentValues.TAG;
import static com.example.beproject2023.SearchPageFragment.LocIn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.razorpay.PaymentData;
import com.razorpay.PaymentResultListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultWithDataListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserCustomCardAdapter extends ArrayAdapter<String[]>  {

    Context mContext;
    ArrayList<String[]>mArrayList;
    Button buyNow;
    FirebaseFirestore db;
    TextView clothName, clothDesc;
    StorageReference storage;
    FirebaseAuth firebaseAuth;

    public static String transact_document_id;
    public static String transact_barcode;
    public static String transact_size;
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

        clothName = view.findViewById(R.id.clothName);
        clothDesc = view.findViewById(R.id.clothDesc);
        final ImageView imageViewCart = view.findViewById(R.id.imageViewCart);

        clothName.setText(mArrayList.get(position)[0] + " " + mArrayList.get(position)[1]);
        clothDesc.setText("Price: "+mArrayList.get(position)[2] + "\tSize: "+ mArrayList.get(position)[3] );

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        try{
            storage = FirebaseStorage.getInstance().getReference().child("cloth_images/" + mArrayList.get(position)[4]);
            String[] str = mArrayList.get(position)[4].split("[.]", 0);
            final File localFile= File.createTempFile(str[0],str[1] );
            storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    imageViewCart.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
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

        buyNow = view.findViewById(R.id.buyNow);
        buyNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transact(Integer.parseInt(mArrayList.get(position)[2]),mArrayList.get(position)[6],  mArrayList.get(position)[5], mArrayList.get(position)[3]);


            }
        });

        return view;
    }
    public void transact(int amount, String card_document_id, String barcode_cloth, String size){
        transact_document_id = card_document_id;
        transact_barcode = barcode_cloth;
        transact_size = size;

        // initialize Razorpay account.
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_nNQTEixTzHLBjc");
        checkout.setImage(R.drawable.shopping_bag);

        // initialize json object
        JSONObject object = new JSONObject();
        try {
            object.put("name", "ShopEasy");
            object.put("description", "Fee payment");
            object.put("theme.color", "");
            object.put("currency", "INR");
            object.put("amount", amount*100);
            checkout.open((Activity)mContext, object);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



}
