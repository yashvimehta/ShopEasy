package com.example.beproject2023;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public class ShoppingFragmentAdapter extends FragmentPagerAdapter {
    public ShoppingFragmentAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        if (position==0){
            return new CartItemFragment();
        }
        else if (position == 1) {
            return new SearchPageFragment();
        }
        else if (position == 2) {
            return new RecommendationFragment();
        }
        else if(position == 3){
            return new EditProfileFragment();
        }
        else{
            return new BarcodeFragment();
        }
    }

    @Override
    public int getCount()
    {
        return 5;
    }
};

