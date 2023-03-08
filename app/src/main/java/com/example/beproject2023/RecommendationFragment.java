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
    public static final int URL_SAFE=8;
    public static final int DEFAULT=0;


    Bitmap bitmap;
    Bitmap bitmap2;
    ImageView imageView;
    TextView messageTextView;
    Button retryButton, gotoResultButton;
    Uri currentImageUri;

    TextInputLayout trialImageTextInputLayout;
    EditText trialImageTextInputText;

    FirebaseAuth firebaseAuth;

    final String[] issueDuration = new String[1];

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
            final Bitmap[] photo_user = new Bitmap[1];
//            FirebaseUser user = firebaseAuth.getCurrentUser();
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//            db.collection("users")
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isSuccessful()) {
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    String uuid = String.valueOf(document.getData().get("uuid"));
//                                    if(uuid.equals(user.getUid())){
//                                        photo_user[0] = Bitmap.createBitmap((Bitmap) document.getData().get("image"));
//                                        break;
//                                    }
//                                }
//                            } else {
//                                Log.d(TAG, "Error getting documents: ", task.getException());
//                            }
//                        }
//                    });


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

                        String s = mResult.getVTRText().get(4);

                        byte [] encodeByte = Base64.decode(s,DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);


//                        byte[] decodedString = new byte[0];
//                        Log.i("apple1", "");
//                        String base64Image = mResult.getVTRText().get(0).split(",")[1];
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            Log.i("apple2", "");
////                            decodedString = android.util.Base64.getDecoder(tmp, URL_SAFE);
//                            Log.i("apple3", "");
//                        }
//                        Log.i("apple4", "");
//                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                        Log.i("apple5", "");

                        imageView.setImageBitmap(bitmap);


                        messageTextView.setVisibility(View.INVISIBLE);
                        trialImageTextInputText.setText(mResult.getVTRText().toString());
                        trialImageTextInputLayout.setVisibility(View.VISIBLE);

                        gotoResultButton.setVisibility(View.VISIBLE);
                        retryButton.setVisibility(View.INVISIBLE);


                    } else {
                        String text = "Failure";
                        messageTextView.setText(text);
                        messageTextView.setVisibility(View.VISIBLE);
                        trialImageTextInputLayout.setVisibility(View.INVISIBLE);
                        trialImageTextInputText.setText("na");
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
                    trialImageTextInputLayout.setVisibility(View.INVISIBLE);
                    trialImageTextInputText.setText("na");
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

        // Image using camera
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
//            Log.i("12345",data.getExtras().toString());
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//
//            Uri tempUri = saveBitmapImage(getContext(), photo);
//            CropImage.activity(tempUri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setCropShape(CropImageView.CropShape.RECTANGLE)
//                    .start(getContext(), this);
//        }
//
//        // Image from gallery
//        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
//            Uri imageLocation = data.getData();
////            currentImageUri=imageLocation;
////            getPredictionsFromServer();
//            CropImage.activity(imageLocation)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setCropShape(CropImageView.CropShape.RECTANGLE)
//                    .start(getContext(), this);
//
//            Log.i("WILL", "will call cropper");
//        }
//
//        // Image cropper
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            currentImageUri = result.getUri();
//            Log.i("IMG CROPPER", "In cropper");
//
//            getPredictionsFromServer();
//
//        }
    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

//        //get issue duration
//        DocumentReference rulesDocumentRef = db.collection("Rules").document("ruless");
//        rulesDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                if (task.isSuccessful()) {
//                    issueDuration[0] = String.valueOf(task.getResult().getData().get("issueDuration(days)"));
//                }
//            }
//        });
//
//        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        assert mUser != null;
//        DocumentReference mDocumentReference = db.collection("Users").document(mUser.getUid());

        View view = inflater.inflate(R.layout.fragment_v_t_r, container, false);
        messageTextView = view.findViewById(R.id.messageTextView);

        retryButton = view.findViewById(R.id.buttonDetect);
        gotoResultButton = view.findViewById(R.id.VTRgotoResultButton);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        trialImageTextInputLayout = view.findViewById(R.id.trialImageTextInputLayout);
        trialImageTextInputText = view.findViewById(R.id.trialImageTextInputText);


        imageView = view.findViewById(R.id.imageViewSelectImage);

        ImageView camera = view.findViewById(R.id.imageViewCamera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);

                gotoResultButton.setVisibility(View.INVISIBLE);

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

        ImageView gallery = view.findViewById(R.id.imageViewGallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);

                gotoResultButton.setVisibility(View.INVISIBLE);

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
//        gotoResultButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                }
//            }
//        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPredictionsFromServer();
                messageTextView.setText("Select or click an Image");
                messageTextView.setVisibility(View.VISIBLE);
                trialImageTextInputLayout.setVisibility(View.INVISIBLE);
                trialImageTextInputText.setText("na");
                retryButton.setVisibility(View.INVISIBLE);

            }
        });

        return view;
    }

}
