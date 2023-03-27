package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
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

import com.example.quickbook.FragmentSetAdmin.CustomCardAdapter;
import com.example.quickbook.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CartItemFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    ListView mListView;
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
        FirebaseUser user = firebaseAuth.getCurrentUser();
        db= FirebaseFirestore.getInstance();

        try {
            db.collection("cart")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                int val=0;
                                ArrayList<String[]> stringArrayList=new ArrayList<String[]>();
                                for (QueryDocumentSnapshot document : task.getResult()) {
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
                                                                    inStockInputText.setText(inStock);
                                                                }
                                                            }
                                                        } else {
                                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                                        }
                                                    }
                                                });

                                        val++;
                                        String price = String.valueOf(document.getData().get("price"));
                                        String size = String.valueOf(document.getData().get("size"));
                                        String image_name = String.valueOf(document.getData().get("image_name"));
                                        String barcode = String.valueOf(document.getData().get("barcode"));
                                        String[] arrayListFeeder=new String[]{StringFormatter.capitalizeWord(color), StringFormatter.capitalizeWord(pattern), price, size, image_name, barcode};
                                        stringArrayList.add(arrayListFeeder);
                                    }
                                }
                                if (val == 0) {
                                    Toast.makeText(getContext(), "No apparel found", Toast.LENGTH_SHORT).show();
                                }
                                Log.i("Length",String.valueOf(stringArrayList.size()));
                                mCustomCardAdapter = new SearchCardAdapter(requireContext(), stringArrayList);
                                clothListView.setAdapter(mCustomCardAdapter);
                                clothListView.setVisibility(View.VISIBLE);
                                clothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        Log.i("success", "Clicked on:" + stringArrayList.get(i)[0]);
                                        Intent intent = new Intent(getActivity().getApplication(), ClothInfo.class);
                                        intent.putExtra("clothData", stringArrayList.get(i));
                                        startActivity(intent);


                                    }
                                });
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