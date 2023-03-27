package com.example.beproject2023;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.beproject2023.databinding.ActivityVtrBinding;

public class VTR extends AppCompatActivity {

    ImageView imageView;
    Button okay;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_vtr);

        imageView = findViewById(R.id.imageView);
        okay = findViewById(R.id.button);

        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("VTRImage");
        imageView.setImageBitmap(bitmap);

        String [] clothData=null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clothData= extras.getStringArray("ClothData");
        }


        String[] finalClothData = clothData;
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VTR.this, ClothInfo.class);
                intent.putExtra("clothData", finalClothData);
                startActivity(intent);
            }
        });





    }
}