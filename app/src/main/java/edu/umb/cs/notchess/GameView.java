package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class GameView extends View {
    private Board mBoard;

    public GameView(Context context) {
        super(context);

        Paint darkPaint = new Paint();
        darkPaint.setColor(ContextCompat.getColor(context, R.color.brown));
        Paint lightPaint = new Paint();
        lightPaint.setColor(ContextCompat.getColor(context, R.color.beige));
        Paint selectedPaint = new Paint();
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.white));

        mBoard = new Board(7, 6, darkPaint, lightPaint, selectedPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBoard.resize(w, h);
    }

    @Override
    public void onDraw (Canvas canvas) {
        mBoard.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                mBoard.select(event.getX(), event.getY());
                break;
        }
        invalidate();
        return true;
    }
}
