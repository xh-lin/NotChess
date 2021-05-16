package edu.umb.cs.notchess;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LevelActivity extends Activity {
    public int level;
    public int aiOption;
    public GameView chessBoardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

        level = getIntent().getExtras().getInt(getResources().getString(R.string.start_level));
        aiOption = getIntent().getExtras().getInt(getResources().getString(R.string.start_ai_option));

        chessBoardView = new GameView(this);
        replaceView(findViewById(R.id.boardView), chessBoardView);
    }

    private void replaceView(View oldView, View newView) {
        ViewGroup parent = (ViewGroup) oldView.getParent();
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) oldView.getLayoutParams();
        parent.removeView(oldView);
        parent.addView(newView, params);
    }

    // close promotion menu
    public void hideView(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    // selected one of the promotion options
    public void selectPromotion(View view) {
        int promote = Integer.parseInt(view.getTag().toString());
        ((View) view.getParent().getParent()).setVisibility(View.INVISIBLE);    // close promotion menu
        chessBoardView.mChessboard.makePromotionMove(promote);
    }

    public void resetBoard(View view) {
        chessBoardView.mChessboard.resetState();
        ((View) view.getParent().getParent()).setVisibility(View.INVISIBLE);    // close dialog
    }

    public void closeActivity(View view) {
        finish();
    }
}
