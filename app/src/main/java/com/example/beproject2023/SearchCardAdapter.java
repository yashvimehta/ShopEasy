package com.example.beproject2023;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SearchCardAdapter extends ArrayAdapter<String[]> {

    Context mContext;
    ArrayList<String[]> mArrayList;

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

        TextView clothNameTextView = view.findViewById(R.id.clothNameTextView);
        final ImageView clothImageView = view.findViewById(R.id.clothImageView);
        TextView clothDetailsTextView=view.findViewById(R.id.clothDetailsTextView);
        clothNameTextView.setText(mArrayList.get(position)[0]);
        clothDetailsTextView.setText(mArrayList.get(position)[1]);
        Glide.with(getContext())
                .load(mArrayList.get(position)[5])
                .placeholder(R.drawable.image_progress)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(clothImageView);
        return view;
    }
}
