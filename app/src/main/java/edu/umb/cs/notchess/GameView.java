package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class GameView extends View {
    private final Chessboard mChessboard;

    public GameView(Context context, int level, int aiOption, TextView indicatorView) {
        super(context);
        mChessboard = new Chessboard(context, level, aiOption, this, indicatorView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mChessboard.resize(w, h);
    }

    @Override
    public void onDraw (Canvas canvas) {
        mChessboard.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            mChessboard.select(event.getX(), event.getY());
        }

        invalidate();   // update the canvas
        return true;
    }
}
