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

public class ItemsBought extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    ListView mListView;
    StorageReference storage;
    FirebaseUser user;
    ItemsBoughtCustomCardAdapter mUserCustomCardAdapter;
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
        final View view = inflater.inflate(R.layout.fragment_items_bought, container, false);
        mListView = view.findViewById(R.id.itemsBought);
        firebaseAuth = FirebaseAuth.getInstance();
        db= FirebaseFirestore.getInstance();

        try {
            db.collection("itemsBought")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            user = firebaseAuth.getCurrentUser();
                            if (task.isSuccessful()) {
                                int val=0;
                                ArrayList<String[]> stringArrayList=new ArrayList<String[]>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String useruid = String.valueOf(document.getData().get("useruid"));
                                    if(useruid.equals(user.getUid())){
                                        String sizee =  String.valueOf(document.getData().get("size"));
                                        String barcode = String.valueOf(document.getData().get("barcode"));
                                        Timestamp javaDate1 = (Timestamp) document.getData().get("date");
                                        Date javaDate = javaDate1.toDate();
                                        String[] buyDate  = String.valueOf(javaDate).split(" GMT") ;
                                        int n=buyDate[0].length();
                                        int m=buyDate[1].length();
                                        String buyDate1 = buyDate[0].substring(4, n - 9)+", "+buyDate[1].substring(m-4,m);
                                        Log.i("Date",buyDate1);
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
                                                                    String size =  String.valueOf(document.getData().get("size"));
                                                                    String image_name = String.valueOf(document.getData().get("image_name"));
                                                                    String[] arrayListFeeder=new String[]{StringFormatter.capitalizeWord(color), StringFormatter.capitalizeWord(pattern), price, sizee, image_name, barcode,buyDate1,String.valueOf(javaDate1.getSeconds())};
                                                                    stringArrayList.add(arrayListFeeder);
                                                                    sort(stringArrayList);
                                                                    mUserCustomCardAdapter = new ItemsBoughtCustomCardAdapter(requireContext(), stringArrayList);
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
    public void sort(ArrayList<String[]> stringArrayList){
        Collections.sort(stringArrayList, new Comparator<String[]>() {

            @Override
            public int compare(String[] s1, String[] s2) {
                long s1Time = Long.valueOf(s1[7]);
                long s2Time=Long.valueOf(s2[7]);
                return (int)(s2Time-s1Time);
            }
        });

    }
}