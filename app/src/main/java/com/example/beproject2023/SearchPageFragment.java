package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.HomePage;
import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ClothInfo;
import com.example.beproject2023.R;
import com.example.beproject2023.SearchCardAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchPageFragment extends Fragment {

    EditText clothNameEditText;

    TextView text;

    FrameLayout frameLayout;
    Button searchButton;
    ListView clothListView;

    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;

    SearchCardAdapter mCustomCardAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search_page, container, false);

        clothNameEditText = view.findViewById(R.id.clothNameEditText);
        text = (TextView) view.findViewById(R.id.textSearch);
        frameLayout = (FrameLayout) view.findViewById(R.id.frame);
        searchButton=view.findViewById(R.id.searchButton);
        clothListView=view.findViewById(R.id.clothListView);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        db= FirebaseFirestore.getInstance();


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchName = clothNameEditText.getText().toString();
                if (searchName.equals("")) {
                    Toast.makeText(getContext(), "Search name cannot be empty", Toast.LENGTH_SHORT).show();
                }
                else {
                    try {
                        db.collection("clothes")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int val=0;
                                            ArrayList<String[]> stringArrayList=new ArrayList<String[]>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String color = String.valueOf(document.getData().get("color"));
                                                String pattern = String.valueOf(document.getData().get("pattern"));
                                                Log.i("Color", color);
                                                Log.i("Pattern", pattern);
                                                String clothSearch  = searchName.toLowerCase();
                                                Log.i("Cloth Search", clothSearch);
                                                if(clothSearch.contains(color) && clothSearch.contains(pattern)){
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
                }
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }




}
