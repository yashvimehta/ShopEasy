package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.beproject2023.ApiHelper.BarcodeResult;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
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

public class SearchPageFragment extends Fragment {

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int DEFAULT=0;

    EditText clothNameEditText;
    Uri currentImageUri;
    TextView text;
    FrameLayout frameLayout;
    Button searchButton;
    ListView clothListView;
    ImageView cameraImageView , galleryImageView;
    FirebaseFirestore db;
    FirebaseAuth firebaseAuth;
    SearchCardAdapter mCustomCardAdapter;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search_page, container, false);

        clothNameEditText = view.findViewById(R.id.clothNameEditText);
        cameraImageView =  view.findViewById(R.id.cameraImageView1);
        galleryImageView =  view.findViewById(R.id.galleryImageView1);
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
                                                if(clothSearch.contains(color) || clothSearch.contains(pattern)){
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

        cameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (HomePage.contextOfApplication.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);

                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
            }
        });

        galleryImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (HomePage.contextOfApplication.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_REQUEST);

                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECT_FILE);
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

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            } else {
                Toast.makeText(getActivity(), "Provide permission to access camera!", Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == STORAGE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECT_FILE);

            } else {
                Toast.makeText(getActivity(), "Provide permission to access your images!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public Uri saveBitmapImage(Context inContext, Bitmap inImage) {
        Log.i("SAVE", "saving image...");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, ts, null);
        return Uri.parse(path);
    }

    public String getFilePathFromUri(Uri uri) {
        String path = "";
        if (HomePage.contextOfApplication.getContentResolver() != null) {
            Cursor cursor = HomePage.contextOfApplication.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }

        return path;
    }

    public void getPredictionsFromServer() {

        try {
            Bitmap photo = MediaStore.Images.Media.getBitmap(HomePage.contextOfApplication.getContentResolver(), currentImageUri);
            Uri tempUri = saveBitmapImage(getContext(), photo);
            String filePath = getFilePathFromUri(tempUri);
            final File file = new File(filePath);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<BarcodeResult> mCall = apiInterface.sendBarcodeImage(body);
            mCall.enqueue(new Callback<BarcodeResult>() {
                @Override
                public void onResponse(Call<BarcodeResult> call, Response<BarcodeResult> response) {
                    BarcodeResult mResult = response.body();
                    if (mResult.getGeneralSuccess()) {
                        Log.i("Success Checking", mResult.getBarcodeText() );
                        String s = mResult.getBarcodeText();


                        //get text from db mResult.getBarcodeText()

                        db.collection("clothes")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            int val=0;
                                            ArrayList<String[]> stringArrayList=new ArrayList<String[]>();
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String barcode = String.valueOf(document.getData().get("barcode"));
                                                if(barcode.equals(s)){
                                                    val++;
                                                    String price = String.valueOf(document.getData().get("price"));
                                                    String size = String.valueOf(document.getData().get("size"));
                                                    String image_name = String.valueOf(document.getData().get("image_name"));
                                                    String color = String.valueOf(document.getData().get("color"));
                                                    String pattern = String.valueOf(document.getData().get("pattern"));
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
                    } else {
                        String text = "Failure";
                        Log.i("Success Checking", mResult.getBarcodeError() +"");

                    }
                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<BarcodeResult> call, Throwable t) {
                    Log.i("Failure Checking", "There was an error " + t.getMessage());
                    String text = "There was some error";

                    if (file.exists()) {
                        file.delete();
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.i("errrrrorr", e.toString());
            String text = "There was some error";
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        Uri imageLocation = data.getData();
        currentImageUri=imageLocation;
        getPredictionsFromServer();
    }



}
