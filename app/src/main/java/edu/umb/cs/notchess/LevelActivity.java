package edu.umb.cs.notchess;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LevelActivity extends Activity {
    public GameView chessBoardView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level);

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
    public void onClickHideView(View view) {
        view.setVisibility(View.INVISIBLE);
    }

    // selected one of the promotion options
    public void onClickPromotion(View view) {
        int promote = Integer.parseInt(view.getTag().toString());
        ((View) view.getParent().getParent()).setVisibility(View.INVISIBLE);    // close promotion menu
        chessBoardView.mChessboard.makePromotionMove(promote);
    }

    // callback function of game reset button
    public void onClickResetBoard(View view) {
        chessBoardView.mChessboard.resetState();
        ((View) view.getParent().getParent()).setVisibility(View.INVISIBLE);    // close dialog
    }

    public void onClickCloseActivity(View view) {
        finish();
    }
}
