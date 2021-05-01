package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import edu.umb.cs.notchess.Chessboard.Piece;
import static edu.umb.cs.notchess.Chessboard.Piece.*;

public class GameView extends View {
    private Chessboard mChessboard;

    public GameView(Context context, View indicatorView) {
        super(context);

        Paint whitePaint = new Paint();
        whitePaint.setColor(ContextCompat.getColor(context, R.color.beige));
        Paint blackPaint = new Paint();
        blackPaint.setColor(ContextCompat.getColor(context, R.color.brown));
        Paint selectedPaint = new Paint();
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.white));

        Piece[][] board = {
                {B_Heart, B_Knight, B_Knight, null, null, null, null},
                {B_Knight, B_Knight, B_Knight, null, null, null, null},
                {B_Knight, B_Knight, null, null, null, null, null},
                {null, null, null, null, null, W_Knight, W_Knight},
                {null, null, null, null, W_Knight, W_Knight, W_Knight},
                {null, null, null, null, W_Knight, W_Knight, W_Heart},
        };

        mChessboard = new Chessboard(context, whitePaint, blackPaint, selectedPaint, board,
                indicatorView);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChessboard.resize(w, h);
    }

    @Override
    public void onDraw (Canvas canvas) {
        mChessboard.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
                mChessboard.select(event.getX(), event.getY());
                break;
        }

        invalidate();
        return true;
    }
}
