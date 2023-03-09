package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.provider.OpenableColumns;
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
import com.example.beproject2023.ApiHelper.VTRResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
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

public class VTRFragment extends Fragment {

    private static final int CAMERA_REQUEST = 1888;
    private static final int STORAGE_REQUEST = 7;
    private static final int SELECT_FILE = 8;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int DEFAULT=0;

    private StorageReference storage;

    String file_cloth_name, file_human_name;

    final Bitmap[] photo_user = new Bitmap[1];
    Bitmap bitmap;
    FirebaseUser user;
    ImageView imageView, VTRHumanImage, VTRClothImage , VTRFinalImage, camera, gallery;
    TextView messageTextView, instructionsVTRTextView;
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
        Log.i("lol5",path );
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

            Bitmap photo_user = getPhotoUser();

            Bitmap photo_cloth = MediaStore.Images.Media.getBitmap(HomePage.contextOfApplication.getContentResolver(), currentImageUri);
            imageView.setImageBitmap(photo_user);

            Log.i(photo_cloth.toString(), "poi1");
            Log.i(photo_user.toString(), "poi2");

            Uri tempUri_cloth = saveBitmapImage(getContext(), photo_cloth);
            Uri tempUri_user = saveBitmapImage(getContext(), photo_user);

            Log.i("lol", tempUri_cloth.getPath());
            Log.i("lol", tempUri_user.getPath());

            String filePath_cloth = getFilePathFromUri(tempUri_cloth);
            String filePath_user = getFilePathFromUri(tempUri_user);

            final File file_cloth = new File(filePath_cloth);
            final File file_user = new File(filePath_user);
            final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(60, TimeUnit.SECONDS)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .build();
            RequestBody requestFile_cloth = RequestBody.create(MediaType.parse("multipart/form-data"), file_cloth);
            RequestBody requestFile_user = RequestBody.create(MediaType.parse("multipart/form-data"), file_user);
            MultipartBody.Part body_cloth = MultipartBody.Part.createFormData("img2", file_cloth_name, requestFile_cloth);
            MultipartBody.Part body_user = MultipartBody.Part.createFormData("img1", file_human_name, requestFile_user);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiInterface.BASE_URL_PREDICTOR)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

            Log.i("fil1",file_cloth.getName() );
            Log.i("fil2",file_user.getName() );
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<VTRResult> mCall = apiInterface.sendVTRImage(body_user, body_cloth);
            mCall.enqueue(new Callback<VTRResult>() {
                @Override
                public void onResponse(Call<VTRResult> call, Response<VTRResult> response) {
                    VTRResult mResult = response.body();
                    if (mResult.getGeneralSuccess()) {
                        Log.i("Success Checking", mResult.getVTRText() );

                        messageTextView.setVisibility(View.INVISIBLE);

                        okayButton.setVisibility(View.VISIBLE);
                        retryButton.setVisibility(View.INVISIBLE);

                        VTRClothImage.setImageBitmap(photo_cloth);
                        VTRHumanImage.setImageBitmap(photo_user);

                        VTRClothImage.setVisibility(View.VISIBLE);
                        VTRHumanImage.setVisibility(View.VISIBLE);

                        VTRFinalImage.setVisibility(View.VISIBLE);

                        byte [] encodeByte = Base64.decode(mResult.getVTRText(),DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                        VTRFinalImage.setImageBitmap(bitmap);

                        instructionsVTRTextView.setVisibility(View.INVISIBLE);
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
                        Log.i("Success Checking", mResult.getVTRError()+" ");

                    }


                    progressBar.setVisibility(View.INVISIBLE);



                    if (file_cloth.exists()) {
                        file_cloth.delete();
                    }
                }

                @Override
                public void onFailure(Call<VTRResult> call, Throwable t) {
                    Log.i("Failure Checking", "There was an error " + t.getMessage());

                    String text = "There was some error";
                    messageTextView.setText(text);
                    messageTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);

                    retryButton.setVisibility(View.VISIBLE);

                    if (file_cloth.exists()) {
                        file_cloth.delete();
                    }

                }
            });


        } catch (Exception e) {
            e.printStackTrace();

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
        Cursor cursor = HomePage.contextOfApplication.getContentResolver().query(data.getData(), null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            file_cloth_name = cursor.getString(nameIndex);
            Log.i("frrrr", cursor.getString(nameIndex));
            cursor.close();
        }
        Log.i("power", String.valueOf(data));
        Uri imageLocation = data.getData();
        currentImageUri=imageLocation;
        getPredictionsFromServer();

    }


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        View view = inflater.inflate(R.layout.fragment_v_t_r, container, false);
        messageTextView = view.findViewById(R.id.VTRMessageTextView);

        retryButton = view.findViewById(R.id.buttonDetect);
        okayButton = view.findViewById(R.id.VTROkayButton);
        progressBar = view.findViewById(R.id.VTRProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        VTRHumanImage = view.findViewById(R.id.VTRHumanImage);
        VTRClothImage = view.findViewById(R.id.VTRClothImage);
        VTRFinalImage = view.findViewById(R.id.VTRFinalImage);

        instructionsVTRTextView = view.findViewById(R.id.instructionsVTRTextView);


        imageView = view.findViewById(R.id.VTRimageViewSelectImage);

        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String uuid = String.valueOf(document.getData().get("uuid"));
                                if(uuid.equals(mUser.getUid())){  //if ISBN exists
                                    file_human_name = (String) document.getData().get("image");
                                    Log.i("orange", file_human_name);
                                    getBitmapImageHuman(file_human_name);
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        camera = view.findViewById(R.id.VTRImageViewCamera);
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

        gallery = view.findViewById(R.id.VTRImageViewGallery);
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
                messageTextView.setText("Select or click an Image");
                messageTextView.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.INVISIBLE);

                VTRClothImage.setVisibility(View.INVISIBLE);
                VTRHumanImage.setVisibility(View.INVISIBLE);
                VTRFinalImage.setVisibility(View.INVISIBLE);

                instructionsVTRTextView.setVisibility(View.VISIBLE);
                camera.setVisibility(View.VISIBLE);
                gallery.setVisibility(View.VISIBLE);


                okayButton.setVisibility(View.INVISIBLE);

            }
        });

        return view;
    }

    public Bitmap getPhotoUser(){
        return photo_user[0];
    }

    public void getBitmapImageHuman(String file_human_name){
        try{
            storage = FirebaseStorage.getInstance().getReference().child("user_images/" + file_human_name);
            String[] str = file_human_name.split("[.]", 0);
            Log.i("orange1", str[0]);
            Log.i("orange2", str[1]);
            final File localFile= File.createTempFile(str[0],str[1] );
            storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    photo_user[0] = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    Log.i(photo_user[0].toString(), "poi");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();

                    Log.i("errrorr", e.toString()+"");

                    String text = "There was some error";
                }
            });
        } catch(Exception e){
            e.printStackTrace();
            Log.i("errrorr", e.toString()+"");

            String text = "There was some error";
        }
    }

}
