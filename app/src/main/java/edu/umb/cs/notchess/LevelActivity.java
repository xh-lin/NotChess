package edu.umb.cs.notchess;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LevelActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        View chessBoardView = new GameView(this, findViewById(R.id.indicatorView));
        replaceView(findViewById(R.id.boardView), chessBoardView);
    }

    private void replaceView(View oldView, View newView) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) oldView.getLayoutParams();
        parent.removeView(oldView);
        parent.addView(newView, params);
    }
}
