package com.example.beproject2023;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public class AdminFragmentAdapter extends FragmentPagerAdapter {
    public AdminFragmentAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        if (position==0){
            return new EditProfileFragment();
        }
        else {
            return new SearchPageFragment();
        }
    }

    @Override
    public int getCount()
    {
        return 2;
    }
};

