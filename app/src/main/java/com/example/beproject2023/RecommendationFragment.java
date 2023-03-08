package com.example.beproject2023;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.RecommendationResult;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
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

public class RecommendationFragment extends Fragment {

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int DEFAULT=0;


    Bitmap bitmap;
    Bitmap bitmap2;
    ImageView imageView, recommendedImage1, recommendedImage2, recommendedImage3, recommendedImage4, recommendedImage5, camera, gallery;
    TextView messageTextView, instructionsRecommendationTextView;
    Button retryButton, okayButton;
    Uri currentImageUri;


    FirebaseAuth firebaseAuth;


    @SuppressLint("StaticFieldLeak")
    static ProgressBar progressBar;

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
        progressBar.setVisibility(View.VISIBLE);
        messageTextView.setVisibility(View.INVISIBLE);

        try {
            Bitmap photo = MediaStore.Images.Media.getBitmap(HomePage.contextOfApplication.getContentResolver(), currentImageUri);
            imageView.setImageBitmap(photo);

            Uri tempUri = saveBitmapImage(getContext(), photo);
            String filePath = getFilePathFromUri(tempUri);

            final File file = new File(filePath);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            MultipartBody.Part body_cloth = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<RecommendationResult> mCall = apiInterface.sendRecImage(body_cloth);
            mCall.enqueue(new Callback<RecommendationResult>() {
                @Override
                public void onResponse(Call<RecommendationResult> call, Response<RecommendationResult> response) {
                    Log.i("yashvi1", "1");
                    RecommendationResult mResult = response.body();
                    Log.i("yashvi", mResult.toString() +"");
                    if (mResult.getGeneralSuccess()) {
                        Log.i("Success Checking", mResult.getVTRText().toString() +"");

                        byte [] encodeByte1 = Base64.decode(mResult.getVTRText().get(0),DEFAULT);
                        Bitmap bitmap1 = BitmapFactory.decodeByteArray(encodeByte1, 0, encodeByte1.length);

                        byte [] encodeByte2 = Base64.decode(mResult.getVTRText().get(1),DEFAULT);
                        Bitmap bitmap2 = BitmapFactory.decodeByteArray(encodeByte2, 0, encodeByte2.length);

                        byte [] encodeByte3 = Base64.decode(mResult.getVTRText().get(2),DEFAULT);
                        Bitmap bitmap3 = BitmapFactory.decodeByteArray(encodeByte3, 0, encodeByte3.length);

                        byte [] encodeByte4 = Base64.decode(mResult.getVTRText().get(3),DEFAULT);
                        Bitmap bitmap4 = BitmapFactory.decodeByteArray(encodeByte4, 0, encodeByte4.length);

                        byte [] encodeByte5 = Base64.decode(mResult.getVTRText().get(4),DEFAULT);
                        Bitmap bitmap5 = BitmapFactory.decodeByteArray(encodeByte5, 0, encodeByte5.length);

                        recommendedImage1.setImageBitmap(bitmap1);
                        recommendedImage1.setVisibility(View.VISIBLE);

                        recommendedImage2.setImageBitmap(bitmap2);
                        recommendedImage2.setVisibility(View.VISIBLE);

                        recommendedImage3.setImageBitmap(bitmap3);
                        recommendedImage3.setVisibility(View.VISIBLE);

                        recommendedImage4.setImageBitmap(bitmap4);
                        recommendedImage4.setVisibility(View.VISIBLE);

                        recommendedImage5.setImageBitmap(bitmap5);
                        recommendedImage5.setVisibility(View.VISIBLE);

                        instructionsRecommendationTextView.setVisibility(View.INVISIBLE);
                        okayButton.setVisibility(View.VISIBLE);

                        messageTextView.setVisibility(View.INVISIBLE);

                        retryButton.setVisibility(View.INVISIBLE);
                        imageView.setVisibility(View.INVISIBLE);

                        camera.setVisibility(View.INVISIBLE);

                        gallery.setVisibility(View.INVISIBLE);



                    } else {
                        String text = "Failure";
                        messageTextView.setText(text);
                        messageTextView.setVisibility(View.VISIBLE);
                        retryButton.setVisibility(View.VISIBLE);
                        Log.i("Success Checking", mResult.getVTRError()+"");

                    }


                    progressBar.setVisibility(View.INVISIBLE);



                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<RecommendationResult> call, Throwable t) {
                    Log.i("Failure Checking", "There was an error " + t.getMessage());

                    String text = "There was some error";
                    messageTextView.setText(text);
                    messageTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);

                    retryButton.setVisibility(View.VISIBLE);

                    if (file.exists()) {
                        file.delete();
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();
            Log.i("errrrrorr", e.toString());

            String text = "There was some error";
            messageTextView.setText(text);
            messageTextView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            retryButton.setVisibility(View.VISIBLE);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        Uri imageLocation = data.getData();
        currentImageUri=imageLocation;
        getPredictionsFromServer();
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        View view = inflater.inflate(R.layout.fragment_recommendation, container, false);

        messageTextView = view.findViewById(R.id.recommendationMessageTextView);

        retryButton = view.findViewById(R.id.recommendationRetryButton);
        progressBar = view.findViewById(R.id.recommendationProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        imageView = view.findViewById(R.id.recommendationImageViewSelectImage);

        instructionsRecommendationTextView = view.findViewById(R.id.instructionsRecommendationTextView);

        okayButton = view.findViewById(R.id.recommendationOkay);

        recommendedImage1 = view.findViewById(R.id.recommendedImage1);
        recommendedImage2 = view.findViewById(R.id.recommendedImage2);
        recommendedImage3 = view.findViewById(R.id.recommendedImage3);
        recommendedImage4 = view.findViewById(R.id.recommendedImage4);
        recommendedImage5 = view.findViewById(R.id.recommendedImage5);

        camera = view.findViewById(R.id.recommendationImageViewCamera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);


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

        gallery = view.findViewById(R.id.recommendationImageViewGallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);

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
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPredictionsFromServer();
                messageTextView.setText("Select or click an Image");
                messageTextView.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.INVISIBLE);

            }
        });

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageTextView.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.INVISIBLE);
                okayButton.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                camera.setVisibility(View.VISIBLE);
                gallery.setVisibility(View.VISIBLE);
                recommendedImage1.setVisibility(View.INVISIBLE);
                recommendedImage2.setVisibility(View.INVISIBLE);
                recommendedImage3.setVisibility(View.INVISIBLE);
                recommendedImage4.setVisibility(View.INVISIBLE);
                recommendedImage5.setVisibility(View.INVISIBLE);
                instructionsRecommendationTextView.setVisibility(View.VISIBLE);

            }
        });

        return view;
    }

}
