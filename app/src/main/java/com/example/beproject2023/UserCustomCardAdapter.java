package com.example.beproject2023;

import static android.content.ContentValues.TAG;
import static com.example.beproject2023.SearchPageFragment.LocIn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.Billing;
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
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserCustomCardAdapter extends ArrayAdapter<String[]>  {

    static Context mContext;
    static ArrayList<String[]>mArrayList;
    Button buyNow;
    static FirebaseFirestore db;
    TextView clothName, clothDesc;
    StorageReference storage;
    FirebaseAuth firebaseAuth;

    public static ArrayList<String> itemm = new ArrayList<String>();
    public static ArrayList<String> quantity = new ArrayList<String>();
    public static ArrayList<String> price = new ArrayList<String>();

    public static ArrayList<String> transact_document_id = new ArrayList<String>();
    public static ArrayList<String> transact_barcode = new ArrayList<String>();
    public static ArrayList<String> transact_size = new ArrayList<String>();
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
        clothDesc.setText(Html.fromHtml("<b>Price: \u20B9"+mArrayList.get(position)[2] + "</b><br>Size: "+ mArrayList.get(position)[3] ));

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
                transact_document_id.removeAll(transact_document_id);
                transact_barcode.removeAll(transact_barcode);
                transact_size.removeAll(transact_size);
                price.removeAll(price);
                quantity.removeAll(quantity);
                itemm.removeAll(itemm);
                transact_document_id.add(mArrayList.get(position)[6]);
                transact_barcode.add(mArrayList.get(position)[5]);
                transact_size.add(mArrayList.get(position)[3]);
                for(int j=0;j<transact_barcode.size();j++) {
                    int finalJ = j;
                    quantity.add("1");
                    db.collection("clothes")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            String barcode1 = String.valueOf(document.getData().get("barcode"));
                                            String in_stock = String.valueOf(document.getData().get("in_stock"));
                                            String color = String.valueOf(document.getData().get("color"));
                                            String pattern = String.valueOf(document.getData().get("pattern"));
                                            String pricee = String.valueOf(document.getData().get("price"));
                                            if (barcode1.equals(transact_barcode.get(finalJ))) {
                                                price.add(pricee);
                                                itemm.add(color + " " + pattern);
                                                db.collection("clothes").document(document.getId()).update("in_stock", Integer.parseInt(in_stock) - 1);

                                            }
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                }
                transact(Integer.parseInt(mArrayList.get(position)[2]));

            }
        });

        return view;
    }
    public static void transact(int amount){
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
            Toast.makeText(mContext, "hey theree!!!!", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void buyAllTransact(){
        int sumAmount=0;
        transact_document_id.removeAll(transact_document_id);
        transact_barcode.removeAll(transact_barcode);
        transact_size.removeAll(transact_size);
        price.removeAll(price);
        quantity.removeAll(quantity);
        itemm.removeAll(itemm);
        for(int i=0;i<mArrayList.size(); i++){
            sumAmount+=Integer.parseInt(mArrayList.get(i)[2]);
            transact_document_id.add(mArrayList.get(i)[6]);
            transact_barcode.add(mArrayList.get(i)[5]);
            transact_size.add(mArrayList.get(i)[3]);
        }
        for(int j=0;j<transact_barcode.size();j++) {
            int finalJ = j;
            quantity.add("1");
            db.collection("clothes")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String barcode1 = String.valueOf(document.getData().get("barcode"));
                                    String in_stock = String.valueOf(document.getData().get("in_stock"));
                                    String color = String.valueOf(document.getData().get("color"));
                                    String pattern = String.valueOf(document.getData().get("pattern"));
                                    String pricee = String.valueOf(document.getData().get("price"));
                                    if (barcode1.equals(transact_barcode.get(finalJ))) {
                                        price.add(pricee);
                                        itemm.add(color + " " + pattern);
                                        db.collection("clothes").document(document.getId()).update("in_stock", Integer.parseInt(in_stock) - 1);

                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
        transact(sumAmount);
    }
}
