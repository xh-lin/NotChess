package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EditGameView extends View {
    public final ChessboardEditor chessboardEditor;

    public EditGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        chessboardEditor = new ChessboardEditor(context, this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        chessboardEditor.onSizeChanged(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        chessboardEditor.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP)
            chessboardEditor.select(event.getX(), event.getY());

        invalidate();   // update the canvas
        return true;
    }
}
