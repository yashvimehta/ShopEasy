package com.example.beproject2023;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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

//import com.canhub.cropper.CropImageView;
import com.example.beproject2023.ApiHelper.ApiInterface;
import com.example.beproject2023.ApiHelper.BarcodeResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
//import com.canhub.cropper.CropImage;
//import com.canhub.cropper.CropImageActivity;

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

public class BarcodeFragment extends Fragment {

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;

    String isbnNumber;

    Bitmap bitmap;
    ImageView imageView;
    TextView messageTextView;
    Button retryButton, okayButton;
    Uri currentImageUri;

    TextInputLayout barcodeTextInputLayout;
    EditText barcodeTextInputText;

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
            Log.i("hello1", "hello1");
            Bitmap photo = MediaStore.Images.Media.getBitmap(HomePage.contextOfApplication.getContentResolver(), currentImageUri);
            Log.i("hello2", "hello2");
            imageView.setImageBitmap(photo);
            Log.i("hello3", "hello3");

            Uri tempUri = saveBitmapImage(getContext(), photo);
            Log.i("hello4", "hello3");
            String filePath = getFilePathFromUri(tempUri);
            Log.i("hello5", "hello3");

            final File file = new File(filePath);
            Log.i("hello6", "hello3");
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            Log.i("hello7", "hello3");
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            Log.i("hello8", "hello3");
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);
            Log.i("hello9", "hello3");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
            Log.i("hello10", "hello3");

            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<BarcodeResult> mCall = apiInterface.sendBarcodeImage(body);
            mCall.enqueue(new Callback<BarcodeResult>() {
                @Override
                public void onResponse(Call<BarcodeResult> call, Response<BarcodeResult> response) {
                    BarcodeResult mResult = response.body();
                    if (mResult.getGeneralSuccess()) {
                        Log.i("Success Checking", mResult.getBarcodeText() );

                        messageTextView.setVisibility(View.INVISIBLE);
                        barcodeTextInputText.setText(mResult.getBarcodeText());
                        barcodeTextInputLayout.setVisibility(View.VISIBLE);

                        okayButton.setVisibility(View.VISIBLE);
                        retryButton.setVisibility(View.INVISIBLE);


                    } else {
                        String text = "Failure";
                        messageTextView.setText(text);
                        messageTextView.setVisibility(View.VISIBLE);
                        barcodeTextInputLayout.setVisibility(View.INVISIBLE);
                        barcodeTextInputText.setText("na");
                        Log.i("Success Checking", mResult.getBarcodeError() +"");

                    }


                    progressBar.setVisibility(View.INVISIBLE);



                    if (file.exists()) {
                        file.delete();
                    }
                }

                @Override
                public void onFailure(Call<BarcodeResult> call, Throwable t) {
                    Log.i("Failure Checking", "There was an error " + t.getMessage());

                    String text = "There was some error";
                    messageTextView.setText(text);
                    messageTextView.setVisibility(View.VISIBLE);
                    barcodeTextInputLayout.setVisibility(View.INVISIBLE);
                    barcodeTextInputText.setText("na");
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

        // Image using camera
//        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
//            Log.i("12345",data.getExtras().toString());
        Bitmap photo = (Bitmap) data.getExtras().get("data");
//            CropImage.activity(tempUri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setCropShape(CropImageView.CropShape.RECTANGLE)
//                    .start(getContext(), this);
//        }
//
//        // Image from gallery
//        if (requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK) {
         Uri imageLocation = data.getData();
         currentImageUri=imageLocation;
         getPredictionsFromServer();
////            getPredictionsFromServer();
//            CropImageActivity.
//
//            CropImage.(imageLocation)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setCropShape(CropImageView.CropShape.RECTANGLE)
//                    .start(getContext(), this);
//
//            Log.i("WILL", "will call cropper");
//        }
//
//        // Image cropper
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE && resultCode == Activity.RESULT_OK && data != null) {
//
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            currentImageUri = result.getUri();
//            Log.i("IMG CROPPER", "In cropper");
//
//            getPredictionsFromServer();
//
//        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //get issue duration
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

        View view = inflater.inflate(R.layout.fragment_barcode, container, false);
        messageTextView = view.findViewById(R.id.barcodeMessageTextView);

        retryButton = view.findViewById(R.id.buttonDetect);
        okayButton = view.findViewById(R.id.barcodeOkayButton);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        barcodeTextInputLayout = view.findViewById(R.id.barcodeTextInputLayout);
        barcodeTextInputText = view.findViewById(R.id.barcodeTextInputText);


        imageView = view.findViewById(R.id.imageViewSelectImage);

        ImageView camera = view.findViewById(R.id.imageViewCamera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryButton.setVisibility(View.INVISIBLE);

                okayButton.setVisibility(View.INVISIBLE);

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

                okayButton.setVisibility(View.INVISIBLE);

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
                barcodeTextInputLayout.setVisibility(View.INVISIBLE);
                barcodeTextInputText.setText("na");
                retryButton.setVisibility(View.INVISIBLE);

            }
        });

        okayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageTextView.setText("Select or click an Image");
                messageTextView.setVisibility(View.VISIBLE);
                barcodeTextInputText.setText("na");
                barcodeTextInputText.setVisibility(View.INVISIBLE);
                barcodeTextInputLayout.setVisibility(View.INVISIBLE);
                retryButton.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                okayButton.setVisibility(View.INVISIBLE);


            }
        });

        return view;
    }

}
