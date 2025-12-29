package com.example.pocketledger;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class MainContainerActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TextView tvNavTask, tvNavLedger, tvNavAi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_container);

        initViews();
        setupViewPager();
        setupNavigation();
    }

    private void initViews() {
        viewPager = findViewById(R.id.viewPager);
        tvNavTask = findViewById(R.id.tvNavTask);
        tvNavLedger = findViewById(R.id.tvNavLedger);
        tvNavAi = findViewById(R.id.tvNavAi);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setOffscreenPageLimit(3); // Keep all fragments in memory for smooth swiping

        // Start on the Ledger tab (index 0 - left)
        viewPager.setCurrentItem(0, false);

        // Listen to page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateNavigationSelection(position);
            }
        });
    }

    private void setupNavigation() {
        tvNavLedger.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        tvNavTask.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        tvNavAi.setOnClickListener(v -> viewPager.setCurrentItem(2, true));

        // Set initial selection
        updateNavigationSelection(0);
    }

    private void updateNavigationSelection(int position) {
        // Reset all tabs
        tvNavTask.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvNavTask.setTypeface(null, Typeface.NORMAL);

        tvNavLedger.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvNavLedger.setTypeface(null, Typeface.NORMAL);

        tvNavAi.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
        tvNavAi.setTypeface(null, Typeface.NORMAL);

        // Highlight selected tab
        TextView selectedTab;
        switch (position) {
            case 0:
                selectedTab = tvNavLedger;
                break;
            case 1:
                selectedTab = tvNavTask;
                break;
            case 2:
                selectedTab = tvNavAi;
                break;
            default:
                selectedTab = tvNavLedger;
                break;
        }
        selectedTab.setTextColor(ContextCompat.getColor(this, R.color.primary));
        selectedTab.setTypeface(null, Typeface.BOLD);
    }

    /**
     * Public method for fragments to navigate to a specific tab
     */
    public void navigateToTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex <= 2) {
            viewPager.setCurrentItem(tabIndex, true);
        }
    }

    /**
     * ViewPager adapter
     */
    private static class MainPagerAdapter extends FragmentStateAdapter {

        public MainPagerAdapter(@NonNull AppCompatActivity activity) {
            super(activity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new LedgerFragment();
                case 1:
                    return new TaskFragment();
                case 2:
                    return new AiFragment();
                default:
                    return new LedgerFragment(); // Default fallback
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
