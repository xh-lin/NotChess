package edu.umb.cs.notchess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class NavigationActivity extends FragmentActivity {
    // used in LevelsObjectFragment and CustomizationObjectFragment
    public AdapterView.OnItemSelectedListener aiOptionListener;

    int level;
    int aiOption;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        PagerAdapter collectionPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(collectionPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        aiOptionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                aiOption = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        };
    }

    /*============================================================================================*/
    /* for LevelsObjectFragment */

    public void startLevel(Piece[][] board, int aiOption) {
        Intent intent = new Intent(this, LevelActivity.class);
        startActivity(intent);

        JSONObject jsonObject = BoardParser.toJson(board);

        SharedPreferences.Editor editor = getSharedPreferences(
                getString(R.string.app_name), MODE_PRIVATE).edit();

        editor.putString(getString(R.string.start_level), jsonObject.toString());
        editor.putInt(getString(R.string.start_ai_option), aiOption);

        editor.apply();
    }

    public void onClickStartLevel(View view) {
        startLevel(Levels.boards[level], aiOption);
    }

    public void onClickStartCustomLevel(View view) {
        startLevel(ChessboardEditor.board, aiOption);
    }

    /*============================================================================================*/
    /*============================================================================================*/
    /* PagerAdapter */

    // The first page
    public static class LevelsObjectFragment extends Fragment {
        int selectedLevel = -1;
        View lastView;
        TextView levelNameTextView;
        Button startLevelButton;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_levels, container, false);

            levelNameTextView = rootView.findViewById(R.id.levelName);
            startLevelButton = rootView.findViewById(R.id.startLevelButton);
            startLevelButton.setEnabled(false);

            // drop down option for the computer player
            Spinner aiOption = rootView.findViewById(R.id.aiOption);
            aiOption.setOnItemSelectedListener(((NavigationActivity) getActivity()).aiOptionListener);

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
                if (selectedLevel != i) {
                    selectedLevel = i;
                    ((NavigationActivity) getActivity()).level = selectedLevel;   // set level

                    levelNameTextView.setText(Levels.titles[i]);    // set title name
                    view.setBackgroundColor(getResources().getColor(R.color.purple_500));   // set selected color

                    if (lastView != null)
                        lastView.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    lastView = view;

                    if (!startLevelButton.isEnabled())
                        startLevelButton.setEnabled(true);
                } else {    // deselect
                    selectedLevel = -1;

                    levelNameTextView.setText("");
                    view.setBackgroundColor(getResources().getColor(R.color.purple_200));
                    lastView = null;

                    startLevelButton.setEnabled(false);
                }
            });

            return rootView;
        }
    }

    // The second page
    public static class CustomizationObjectFragment extends Fragment {
        View lastPressedView;   // for piece selection

        @SuppressLint("ClickableViewAccessibility")
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_section_customization, container, false);
            EditGameView editGameView = rootView.findViewById(R.id.editGameView);
            Spinner columnsSpinner = rootView.findViewById(R.id.columnsSpinner);
            Spinner rowsSpinner = rootView.findViewById(R.id.rowsSpinner);

            // setup spinners with adapters
            ArrayList<String> spinnerArray = new ArrayList();
            for (int i = ChessboardEditor.MIN_SIZE; i <= ChessboardEditor.MAX_SIZE; i++)
                spinnerArray.add(String.valueOf(i));
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    spinnerArray);
            columnsSpinner.setAdapter(arrayAdapter);
            rowsSpinner.setAdapter(arrayAdapter);

            columnsSpinner.setSelection(ChessboardEditor.DEFAULT_SIZE - ChessboardEditor.MIN_SIZE);
            rowsSpinner.setSelection(ChessboardEditor.DEFAULT_SIZE - ChessboardEditor.MIN_SIZE);

            columnsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    editGameView.chessboardEditor.setColumns(i + ChessboardEditor.MIN_SIZE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            rowsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    editGameView.chessboardEditor.setRows(i + ChessboardEditor.MIN_SIZE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            // drop down option for the computer player
            Spinner aiOption = rootView.findViewById(R.id.aiOption);
            aiOption.setOnItemSelectedListener(((NavigationActivity) getActivity()).aiOptionListener);

            // OnTouchListener for imageButtons
            View.OnTouchListener onTouchListener = (view, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (lastPressedView != null)    // deselect last one
                        lastPressedView.setPressed(false);
                    if (lastPressedView != view) {  // select
                        view.setPressed(true);
                        lastPressedView = view;
                        editGameView.chessboardEditor.buttonPressed((String) view.getTag());
                    } else {                        // deselect if clicking the same one
                        lastPressedView = null;
                        editGameView.chessboardEditor.buttonPressed(null);
                    }
                }
                return true;
            };

            // delete button
            ImageButton deleteButton = rootView.findViewById(R.id.deleteButton);
            deleteButton.setOnTouchListener(onTouchListener);

            // piece selection buttons
            LinearLayout piecesLayout = rootView.findViewById(R.id.piecesLayout);
            int childCount = piecesLayout.getChildCount();
            for (int i = 0; i < childCount; i++)
                piecesLayout.getChildAt(i).setOnTouchListener(onTouchListener);

            return rootView;
        }
    }


    public static class PagerAdapter extends FragmentPagerAdapter {
        private final Fragment[] items;
        private final String[] titles;

        public PagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            items = new Fragment[]{new LevelsObjectFragment(), new CustomizationObjectFragment()};
            titles = new String[]{context.getString(R.string.levels), context.getString(R.string.customization)};
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