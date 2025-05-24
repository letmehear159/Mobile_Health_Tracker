package com.example.healthtrackerapp.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.healthtrackerapp.view.fragment.ImageFragment;
import com.example.healthtrackerapp.view.fragment.ChartFragment;
import com.example.healthtrackerapp.view.fragment.HomeFragment;
import com.example.healthtrackerapp.view.fragment.ProfileFragment;
import com.example.healthtrackerapp.view.fragment.SearchFragment;

public class TabPagerAdapter extends FragmentStateAdapter {

    public TabPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new ChartFragment();
            case 2: return new SearchFragment();
            case 3: return new ImageFragment();
            case 4: return new ProfileFragment();
            default: return new HomeFragment(); // fallback
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
