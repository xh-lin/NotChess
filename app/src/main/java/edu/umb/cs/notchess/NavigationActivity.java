package edu.umb.cs.notchess;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class NavigationActivity extends FragmentActivity {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    PagerAdapter demoCollectionPagerAdapter;
    ViewPager viewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        demoCollectionPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(demoCollectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }


    public static class PagerAdapter extends FragmentPagerAdapter {
        private Context context;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new LevelsObjectFragment();
                default:
                    return new CustomizeObjectFragment();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return context.getString(R.string.levels);
                default:
                    return context.getString(R.string.customize);
            }
        }
    }

    public static class LevelsObjectFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_levels, container, false);
            rootView.findViewById(R.id.buttonLevelOne)
                    .setOnClickListener(view -> {
                        Intent intent = new Intent(getActivity(), LevelActivity.class);
                        startActivity(intent);
                    });
            return rootView;
        }
    }

    public static class CustomizeObjectFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_section_customize, container, false);
        }
    }

}