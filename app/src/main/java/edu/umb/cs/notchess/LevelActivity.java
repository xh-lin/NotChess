package edu.umb.cs.notchess;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LevelActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        int level = getIntent().getExtras().getInt(getResources().getString(R.string.level_selected));
        int aiOption = getIntent().getExtras().getInt(getResources().getString(R.string.ai_option));

        TextView indicatorView = findViewById(R.id.indicatorView);
        View chessBoardView = new GameView(this, level, aiOption, indicatorView);
        replaceView(findViewById(R.id.boardView), chessBoardView);
    }

    private void replaceView(View oldView, View newView) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) oldView.getLayoutParams();
        parent.removeView(oldView);
        parent.addView(newView, params);
    }
}
