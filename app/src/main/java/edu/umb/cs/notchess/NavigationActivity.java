package edu.umb.cs.notchess;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import org.json.JSONObject;
import java.util.ArrayList;

public class NavigationActivity extends FragmentActivity {
    ChessboardEditor chessboardEditor;

    // levels page

    private int level;
    private int aiOption;

    // customization page

    private int aiOptionCustom;

    private Spinner columnsSpinner;
    private Spinner rowsSpinner;

    private View loadLevelView;
    private View saveLevelView;

    private ListView levelListView;
    private SavedLevelDbHelper dbHelper;
    private SavedLevelDbHelper.LevelList levelList;
    private int selectedLevelIdx = -1;          // index of selected item in the load level menu
    private View lastView;                      // last selected item in level list
    private Button deleteLevelButton;
    private Button loadLevelButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // setup view pager
        PagerAdapter collectionPagerAdapter = new PagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(collectionPagerAdapter);
        // setup tab
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        // setup View objects for load/save custom levels
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout mainLayout = findViewById(R.id.constrainLayout);
        // load menu
        loadLevelView = layoutInflater.inflate(R.layout.window_load_level, null);
        mainLayout.addView(loadLevelView, mainLayout.getLayoutParams());
        loadLevelView.setVisibility(View.INVISIBLE);
        // save menu
        saveLevelView = layoutInflater.inflate(R.layout.window_save_level, null);
        mainLayout.addView(saveLevelView, mainLayout.getLayoutParams());
        saveLevelView.setVisibility(View.INVISIBLE);

        // load menu: setup click listener for level list
        deleteLevelButton = loadLevelView.findViewById(R.id.deleteLevelButton);
        loadLevelButton = loadLevelView.findViewById(R.id.loadLevelButton);
        deleteLevelButton.setEnabled(false);
        loadLevelButton.setEnabled(false);
        levelListView = loadLevelView.findViewById(R.id.levelListView);
        levelListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (selectedLevelIdx != i) {
                selectedLevelIdx = i;   // set select level index
                view.setBackgroundColor(getResources().getColor(R.color.purple_500));   // set selected color
                if (lastView != null)
                    lastView.setBackgroundColor(getResources().getColor(R.color.purple_200));
                lastView = view;
                deleteLevelButton.setEnabled(true);
                loadLevelButton.setEnabled(true);
            } else {    // click the same item again
                deselectLevelItem();
                view.setBackgroundColor(getResources().getColor(R.color.purple_200));
            }

        });

        // save menu: disable Button if EditText is empty
        EditText titleEditText = saveLevelView.findViewById(R.id.titleEditText);
        Button saveLevelButton = saveLevelView.findViewById(R.id.saveLevelButton);
        saveLevelButton.setEnabled(false);
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                saveLevelButton.setEnabled(!charSequence.toString().equals(""));
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        dbHelper = new SavedLevelDbHelper(this);
    }

    public void setChessboardEditor(ChessboardEditor chessboardEditor) {
        this.chessboardEditor = chessboardEditor;
    }

    public void setColumnsSpinner(Spinner columnsSpinner) {
        this.columnsSpinner = columnsSpinner;
    }

    public void setRowsSpinner(Spinner rowsSpinner) {
        this.rowsSpinner = rowsSpinner;
    }

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

    /*============================================================================================*/
    /* for LevelsObjectFragment */

    public void onClickStartLevel(View view) {
        startLevel(Levels.boards[level], aiOption);
    }

    /*============================================================================================*/
    /* for CustomizationObjectFragment */

    private void deselectLevelItem() {
        selectedLevelIdx = -1;
        lastView = null;
        deleteLevelButton.setEnabled(false);
        loadLevelButton.setEnabled(false);
    }

    private void updateCustomBoard(Piece[][] newBoard) {
        chessboardEditor.setBoard(newBoard);
    }

    // close load/save level menu
    public void onClickHideView(View view) {
        view.setVisibility(View.INVISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);      // hide keyboard
        deselectLevelItem();
    }

    public void onClickDelete(View view) {
        dbHelper.deleteLevel(levelList.ids.get(selectedLevelIdx));
        deselectLevelItem();                    // deselect list item
        levelList = dbHelper.getLevelList();    // query list with names and ids
        levelListView.setAdapter(new ArrayAdapter<>(this,   // update ListView
                R.layout.list_item_saved_level,
                levelList.titles));
    }

    public void onClickLoad(View view) {
        assert selectedLevelIdx != -1;      // should only be called when an item is selected
        Piece[][] newBoard = dbHelper.getBoard(levelList.ids.get(selectedLevelIdx));
        updateCustomBoard(newBoard);
        columnsSpinner.setSelection(newBoard[0].length - ChessboardEditor.MIN_SIZE);
        rowsSpinner.setSelection(newBoard.length - ChessboardEditor.MIN_SIZE);

        onClickHideView(loadLevelView);     // close menu
        deselectLevelItem();                // deselect list item
    }

    // open the load level menu
    public void onClickLoadLevelView(View view) {
        levelList = dbHelper.getLevelList();    // query list with names and ids
        levelListView.setAdapter(new ArrayAdapter<>(this,   // update ListView
                R.layout.list_item_saved_level,
                levelList.titles));
        // display load level menu
        loadLevelView.bringToFront();
        loadLevelView.setVisibility(View.VISIBLE);
    }

    public void onClickSave(View view) {
        EditText editText = findViewById(R.id.titleEditText);
        String title = editText.getText().toString();           // get title
        dbHelper.saveLevel(title, chessboardEditor.getBoard()); // save into database
        onClickHideView(saveLevelView);                         // close menu
    }

    // open the save level menu
    public void onClickSaveLevelView(View view) {
        saveLevelView.bringToFront();
        saveLevelView.setVisibility(View.VISIBLE);
    }

    // start the custom level
    public void onClickStartCustomLevel(View view) {
        startLevel(chessboardEditor.getBoard(), aiOptionCustom);
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
            NavigationActivity navigationActivity = ((NavigationActivity) getActivity());
            assert navigationActivity != null;

            levelNameTextView = rootView.findViewById(R.id.levelName);
            startLevelButton = rootView.findViewById(R.id.startLevelButton);
            startLevelButton.setEnabled(false);

            // drop down option for the computer player
            Spinner aiOption = rootView.findViewById(R.id.aiOption);
            aiOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    navigationActivity.aiOption = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
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
                if (selectedLevel != i) {
                    selectedLevel = i;
                    navigationActivity.level = selectedLevel;   // set level

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
            ChessboardEditor chessboardEditor = editGameView.chessboardEditor;
            Spinner columnsSpinner = rootView.findViewById(R.id.columnsSpinner);
            Spinner rowsSpinner = rootView.findViewById(R.id.rowsSpinner);
            NavigationActivity navigationActivity = ((NavigationActivity) getActivity());
            assert navigationActivity != null;

            // setup spinners with adapters
            ArrayList<String> spinnerArray = new ArrayList<>();
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
                    chessboardEditor.setColumns(i + ChessboardEditor.MIN_SIZE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            rowsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    chessboardEditor.setRows(i + ChessboardEditor.MIN_SIZE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            // drop down option for the computer player
            Spinner aiOption = rootView.findViewById(R.id.aiOption);
            aiOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    navigationActivity.aiOptionCustom = i;
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {}
            });

            // OnTouchListener for imageButtons
            View.OnTouchListener onTouchListener = (view, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    if (lastPressedView != null)    // deselect last one
                        lastPressedView.setPressed(false);
                    if (lastPressedView != view) {  // select
                        view.setPressed(true);
                        lastPressedView = view;
                        chessboardEditor.buttonPressed((String) view.getTag());
                    } else {                        // deselect if clicking the same one
                        lastPressedView = null;
                        chessboardEditor.buttonPressed(null);
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

            navigationActivity.setChessboardEditor(chessboardEditor);
            navigationActivity.setColumnsSpinner(columnsSpinner);
            navigationActivity.setRowsSpinner(rowsSpinner);

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