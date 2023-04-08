package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.beproject2023.UserCustomCardAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CartItemFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    ListView mListView;
    StorageReference storage;
    FirebaseUser user;
    UserCustomCardAdapter mUserCustomCardAdapter;
    final String[] memberid = new String[1];
    final String[] vall = new String[2];
    int perDayFine;
    public static String docID;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cart_item, container, false);
        mListView = view.findViewById(R.id.cartItems);
        firebaseAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();

        try {
            db.collection("cart")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            user = firebaseAuth.getCurrentUser();
                            if (task.isSuccessful()) {
                                int val=0;
                                ArrayList<String[]> stringArrayList=new ArrayList<String[]>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String ss = document.getId();
                                    String useruid = String.valueOf(document.getData().get("useruid"));
                                    if(useruid.equals(user.getUid())){
                                        String size =  String.valueOf(document.getData().get("size"));
                                        String barcode = String.valueOf(document.getData().get("barcode"));

                                        db.collection("clothes")
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        int val = 0;
                                                        if (task.isSuccessful()) {
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                                String barcode1 = String.valueOf(document.getData().get("barcode"));
                                                                if(barcode1.equals(barcode)){
                                                                    String color = String.valueOf(document.getData().get("color"));
                                                                    String pattern = String.valueOf(document.getData().get("pattern"));
                                                                    String price = String.valueOf(document.getData().get("price"));
                                                                    String image_name = String.valueOf(document.getData().get("image_name"));
                                                                    String[] arrayListFeeder=new String[]{StringFormatter.capitalizeWord(color), StringFormatter.capitalizeWord(pattern), price, size, image_name, barcode, ss};
                                                                    stringArrayList.add(arrayListFeeder);
                                                                    mUserCustomCardAdapter = new UserCustomCardAdapter(requireContext(), stringArrayList);
                                                                    mListView.setAdapter(mUserCustomCardAdapter);
                                                                    mListView.setVisibility(View.VISIBLE);

                                                                }
                                                            }
                                                        } else {
                                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                                        }
                                                    }
                                                });
                                    }
                                }
                                Log.i("Length",String.valueOf(stringArrayList.size()));
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to fetch results from server", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        super.onViewCreated(view, savedInstanceState);


        return view;
    }
}