package com.example.healthtrackerapp.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.healthtrackerapp.R;
import com.example.healthtrackerapp.adapter.TabPagerAdapter;
import com.example.healthtrackerapp.manager.CloudinaryManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TabLayoutActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TabPagerAdapter adapter;
    private FloatingActionButton fabChat;

    private final int[] tabIcons = {
            R.drawable.home,
            R.drawable.statistic,
            R.drawable.search,  // Tab giữa - sẽ làm nổi
            R.drawable.camera,
            R.drawable.user
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        fabChat = findViewById(R.id.fabChat);

        adapter = new TabPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Không cần setText nếu bạn dùng icon
        }).attach();
        // Sau khi attach -> gắn icon và hiệu ứng
        setupTabIcons();

        // Setup chat button click listener
        fabChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabIcons() {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                View customView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
                ImageView icon = customView.findViewById(R.id.tabIcon);
                icon.setImageResource(tabIcons[i]);
                tab.setCustomView(customView);
            }
        }

        updateSelectedTabAppearance(tabLayout.getSelectedTabPosition());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateSelectedTabAppearance(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateSelectedTabAppearance(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateSelectedTabAppearance(int selectedPosition) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null && tab.getCustomView() != null) {
                View customView = tab.getCustomView();
                ImageView icon = customView.findViewById(R.id.tabIcon);

                if (i == selectedPosition) {
                    icon.setBackgroundResource(R.drawable.tab_selected_background);
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.white));
                } else {
                    icon.setBackground(null);
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.tab_unselected));
                }
            }
        }
    }
}
