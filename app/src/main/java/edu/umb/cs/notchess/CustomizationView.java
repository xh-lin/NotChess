package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class CustomizationView extends View {
    public CustomizationView(Context context) {
        super(context);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

    }

    @Override
    public void onDraw (Canvas canvas) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        invalidate();   // update the canvas
        return true;
    }
}
