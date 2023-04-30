package com.example.beproject2023;

import static android.content.ContentValues.TAG;

import static com.example.beproject2023.UserCustomCardAdapter.transact_barcode;
import static com.example.beproject2023.UserCustomCardAdapter.transact_size;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddClothFragment extends Fragment {

    EditText inStockInputText, colorInputText, priceInputText , patternInputText , sizeInputText, barcodeInputText;
    Button saveChanges;
    FirebaseFirestore db;


    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_cloth, container, false);
        inStockInputText=view.findViewById(R.id.inStockInputText);
        priceInputText = view.findViewById(R.id.priceInputText);
        colorInputText = view.findViewById(R.id.colorInputText);
        patternInputText = view.findViewById(R.id.patternInputText);
        sizeInputText = view.findViewById(R.id.sizeInputText);
        barcodeInputText = view.findViewById(R.id.barcodeInputText);
        saveChanges = view.findViewById(R.id.saveChanges);
        inStockInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
        priceInputText.setInputType(InputType.TYPE_CLASS_NUMBER);
        sizeInputText.setInputType(InputType.TYPE_CLASS_TEXT);
        db = FirebaseFirestore.getInstance();
        inStockInputText.setText("");
        priceInputText.setText("");
        sizeInputText.setText("");
        patternInputText.setText("");
        barcodeInputText.setText("");
        colorInputText.setText("");

        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noOfStock = inStockInputText.getText().toString();
                String price = priceInputText.getText().toString();
                String size = sizeInputText.getText().toString();
                String pattern=patternInputText.getText().toString();
                String barcode = barcodeInputText.getText().toString();
                String color=colorInputText.getText().toString();
                if(noOfStock.equals(" ") || price.equals(" ") || size.equals(" ") || pattern.equals(" ") || barcode.equals(" ") || color.equals(" ")){
                    Toast.makeText(getContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();

                }
                else if(Integer.parseInt(noOfStock)<0){
                    Toast.makeText(getContext(), "No. of stock cannot be less than 0!", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.parseInt(price)<0){
                    Toast.makeText(getContext(), "Price cannot be less than 0!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Map<String, Object> mMap = new HashMap<>();
                    mMap.put("barcode",barcode);
                    mMap.put("size",size);
                    mMap.put("in_stock", noOfStock);
                    mMap.put("pattern",pattern);
                    mMap.put("price",price);
                    mMap.put("color", color);
//                    mMap.put("image_name", color);
                    db.collection("clothes").add(mMap);
                    Toast.makeText(getContext(), "Fields updated!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}