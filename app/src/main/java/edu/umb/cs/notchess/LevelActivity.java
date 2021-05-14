package edu.umb.cs.notchess;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LevelActivity extends Activity {
    public int level;
    public int aiOption;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        level = getIntent().getExtras().getInt(getResources().getString(R.string.level_selected));
        aiOption = getIntent().getExtras().getInt(getResources().getString(R.string.ai_option));
        
        View chessBoardView = new GameView(this);
        replaceView(findViewById(R.id.boardView), chessBoardView);



//        LayoutInflater layoutInflater;
//        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//        ConstraintLayout mainLayout = findViewById(R.id.constrainLayout);
//        View view = layoutInflater.inflate(R.layout.promotion_white, null);
//        view.setX(30);
//        view.setY(30);
//        mainLayout.addView(view, 2);
    }

    private void replaceView(View oldView, View newView) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) oldView.getLayoutParams();
        parent.removeView(oldView);
        parent.addView(newView, params);
    }
}
