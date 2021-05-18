package edu.umb.cs.notchess;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

public class GameView extends View {
    public final Chessboard mChessboard;

    public GameView(Context context) {
        super(context);

        SharedPreferences prefs = context.getSharedPreferences(
                context.getString(R.string.app_name), Context.MODE_PRIVATE);

        // get level
        String jsonStr = prefs.getString(context.getString(R.string.start_level), "");
        Piece[][] board = null;
        try {
            board = BoardParser.fromJson(new JSONObject(jsonStr));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // get spinner option: 0 -> disable, 1 -> Black, 2 -> White
        int aiOption = prefs.getInt(context.getString(R.string.start_ai_option), -1);
        int ai = 0; // disable
        switch (aiOption) {
            case 1: // Black
                ai = -1;
                break;
            case 2: // White
                ai = 1;
        }

        mChessboard = new Chessboard(context, this, board, ai);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mChessboard.onSizeChanged(w, h);
    }

    @Override
    public void onDraw(Canvas canvas) {
        mChessboard.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP)
            mChessboard.select(event.getX(), event.getY());

        invalidate();   // update the canvas
        return true;
    }
}
