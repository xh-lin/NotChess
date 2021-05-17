package edu.umb.cs.notchess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import java.util.Objects;

public class NavigationActivity extends FragmentActivity {
    PagerAdapter collectionPagerAdapter;
    ViewPager viewPager;
    int level;
    int aiOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        collectionPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(collectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void startLevel(View view) {
        Intent intent = new Intent(this, LevelActivity.class);
        startActivity(intent);

        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.app_name), MODE_PRIVATE).edit();
        editor.putInt(getString(R.string.start_level), level);
        editor.putInt(getString(R.string.start_ai_option), aiOption);
        editor.apply();
    }

    private void setLevel(int level) {
        this.level = level;
    }

    private void setAIOption(int aiOption) {
        this.aiOption = aiOption;
    }

    /*============================================================================================*/
    /* PagerAdapter */

    public static class LevelsObjectFragment extends Fragment {
        int level = -1;
        View lastView;
        TextView levelNameTextView;
        Button startLevelButton;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_levels, container, false);

            levelNameTextView = rootView.findViewById(R.id.level_name);
            startLevelButton = rootView.findViewById(R.id.start_level);
            startLevelButton.setEnabled(false);

            // drop down option for the computer player
            Spinner aiOption = rootView.findViewById(R.id.ai_option);
            aiOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    ((NavigationActivity) Objects.requireNonNull(getActivity())).setAIOption(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            // load titles of each level
            int levelCount = Levels.titles.length;
            String[] levels = new String[levelCount];
            for (int i = 0; i < levelCount; i++)
                levels[i] = String.valueOf(i+1);

            // add buttons for level option
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.list_item_level, levels);
            GridView gridView = rootView.findViewById(R.id.gridView);
            gridView.setAdapter(adapter);

            // handle selecting level buttons
            gridView.setOnItemClickListener((adapterView, view, i, l) -> {
                if (level != i) {
                    level = i;
                    ((NavigationActivity) getActivity()).setLevel(level);   // set level

                    levelNameTextView.setText(Levels.titles[i]);    // set title name
                    view.setBackgroundColor(getResources().getColor(R.color.purple_500));   // set selected color

                    if (lastView != null)
                        lastView.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    lastView = view;

                    if (!startLevelButton.isEnabled())
                        startLevelButton.setEnabled(true);
                } else {    // deselect
                    level = -1;

                    levelNameTextView.setText("");
                    view.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    lastView = null;

                    startLevelButton.setEnabled(false);
                }
            });

            return rootView;
        }
    }


    public static class CustomizeObjectFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_section_customize, container, false);
        }
    }


    public static class PagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] items;
        private final String[] titles;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            items = new Fragment[]{new LevelsObjectFragment(), new CustomizeObjectFragment()};
            titles = new String[]{context.getString(R.string.levels), context.getString(R.string.customize)};
        }

        @NonNull
        @Override
        public Fragment getItem(int i) {
            return items[i];
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}