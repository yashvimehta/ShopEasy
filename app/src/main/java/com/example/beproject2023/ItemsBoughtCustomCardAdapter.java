package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.beproject2023.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.razorpay.Checkout;
//import com.razorpay.Checkout;
//import com.razorpay.PaymentResultListener;
//import com.google.firebase.storage.FirebaseStorage;
//import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ItemsBoughtCustomCardAdapter extends ArrayAdapter<String[]> {

    Context mContext;
    ArrayList<String[]>mArrayList;
    FirebaseFirestore db;
    TextView clothName, clothDesc, costTextView;
    StorageReference storage;
    public ItemsBoughtCustomCardAdapter(@NonNull Context context, ArrayList<String[]> stringArrayList) {
        super(context, R.layout.content_items_bought_custom_card_adapter, stringArrayList);
        this.mContext = context;
        this.mArrayList = stringArrayList;
    }


    @SuppressLint("MissingInflatedId")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(this.getContext());
        @SuppressLint("ViewHolder") View view = mLayoutInflater.inflate(R.layout.content_items_bought_custom_card_adapter, null, true);

        clothName = view.findViewById(R.id.clothNamee);
        clothDesc = view.findViewById(R.id.clothDesc);
        costTextView=view.findViewById(R.id.costTextView);
        final ImageView imageViewCart = view.findViewById(R.id.imageViewCart);

        clothName.setText(mArrayList.get(position)[0] + " " + mArrayList.get(position)[1]);
        clothDesc.setText("Size: "+ mArrayList.get(position)[3]+"\nDate: "+mArrayList.get(position)[6]);
        costTextView.setText("\u20B9"+mArrayList.get(position)[2]);

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

        return view;
    }

}
