package com.example.beproject2023;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

public class SearchCardAdapter extends ArrayAdapter<String[]> {

    Context mContext;
    StorageReference storageRef;
    ArrayList<String[]> mArrayList;
    StorageReference storage;

    public SearchCardAdapter(@NonNull Context context, ArrayList<String[]> stringArrayList) {
        super(context, R.layout.activity_search_card_adapter,stringArrayList);
        this.mContext = context;
        this.mArrayList = stringArrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater mLayoutInflater = LayoutInflater.from(mContext);
        @SuppressLint("ViewHolder") View view = mLayoutInflater.inflate(R.layout.activity_search_card_adapter, null, true);

//        storage = FirebaseStorage.getInstance();
//        storageRef = storage.getReference();

        TextView clothNameTextView = view.findViewById(R.id.clothNameTextView);
        final ImageView clothImageView = view.findViewById(R.id.clothImageView);
        TextView clothDetailsTextView=view.findViewById(R.id.clothDetailsTextView);
        clothNameTextView.setText(mArrayList.get(position)[0] + " " + mArrayList.get(position)[1]);
        clothDetailsTextView.setText("Price: "+mArrayList.get(position)[2] + "\tSize: "+ mArrayList.get(position)[3] );
        try{
            storage = FirebaseStorage.getInstance().getReference().child("cloth_images/" + mArrayList.get(position)[4]);
            Log.i("YASHVIII", mArrayList.get(position)[4]);
            String[] str = mArrayList.get(position)[4].split("[.]", 0);
            final File localFile= File.createTempFile(str[0],str[1] );
            storage.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    clothImageView.setImageBitmap(BitmapFactory.decodeFile(localFile.getAbsolutePath()));
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

//        final long ONE_MEGABYTE = 1024 * 1024;
//        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                clothImageView.setImageBitmap(bitmap);
//            }
//        });
//            final File localFile = File.createTempFile("image", "jpg");
//            StorageReference desertRef = storageRef.child("cloth_images/"+mArrayList.get(position)[4]);
//            desertRef.getFile(localFile).addOnSuccessListener(
//                    new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
//                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
//                            clothImageView.setImageBitmap(bitmap);
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {}
//            });

        return view;
    }
}
