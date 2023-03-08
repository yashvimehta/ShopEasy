package com.example.beproject2023;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class HomePage extends AppCompatActivity {

    public static Context contextOfApplication;


//    public void takeToResult(View view) {
//        Button mButton = (Button) view;
//
//        Intent intent = new Intent(this, ResultsActivity.class);
//        intent.putExtra("item", mButton.getText().toString());
//        startActivity(intent);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
//
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        ViewPager viewPager = (ViewPager)findViewById(R.id.pager);
        Log.i("hey1", "hey1");

        ShoppingFragmentAdapter adapter = new ShoppingFragmentAdapter(getSupportFragmentManager());
        Log.i("hey2", "hey1");
        viewPager.setAdapter(adapter);
        Log.i("hey3", "hey1");
        viewPager.setCurrentItem(1);
        Log.i("hey4", "hey1");

        contextOfApplication = getApplicationContext();
        Log.i("hey5", "hey1");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.i("hey6", "hey1");

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
