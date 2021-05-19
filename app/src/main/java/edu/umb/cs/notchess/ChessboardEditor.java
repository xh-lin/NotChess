package edu.umb.cs.notchess;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import static edu.umb.cs.notchess.Piece.*;

import android.util.Log;
import android.view.View;

import androidx.core.content.ContextCompat;

import org.json.JSONObject;

public class ChessboardEditor {
    static final int MIN_SIZE = 3;
    static final int MAX_SIZE = 20;
    static final int DEFAULT_SIZE = 8;

    private final Context context;          // context of GameView
    private final View viewToDraw;          // the View object that draws the chess board

    static Piece[][] board;
    private int columns;                // dimension of the chess board
    private int rows;
    private int w;                          // View width
    private int h;                          // View height

    private final Paint whitePaint;         // paints for the board
    private final Paint blackPaint;
    private final Paint selectedPaint;      // the paint for highlights
    
    private int blockSize;                  // width/height of a block
    private Rect block;                     // used for drawing each block of the board

    private int selectedX = -1;
    private int selectedY = -1;

    private Piece pieceToAdd;
    private boolean buttonPressed;

    public ChessboardEditor(Context context, View viewToDraw) {
        this.context = context;
        this.viewToDraw = viewToDraw;

        // create paints
        whitePaint = new Paint();
        whitePaint.setColor(ContextCompat.getColor(context, R.color.beige));
        blackPaint = new Paint();
        blackPaint.setColor(ContextCompat.getColor(context, R.color.brown));
        selectedPaint = new Paint();
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.trans_yellow));

        Piece.loadAssets(context.getResources());   // load drawables for Piece
    }

    public void setColumns(int columns) {
        this.columns = columns;
        onSizeChanged(w, h);
        updateBoardDimension();
        viewToDraw.invalidate();
    }

    public void setRows(int rows) {
        this.rows = rows;
        onSizeChanged(w, h);
        updateBoardDimension();
        viewToDraw.invalidate();
    }

    private void updateBoardDimension() {
        if (columns != 0 && rows != 0) {
            Piece[][] newBoard = new Piece[rows][columns];
            if (board != null) {
                int minCol = Math.min(board[0].length, columns);
                int minRow = Math.min(board.length, rows);
                for (int x = 0; x < minCol; x++) {
                    for (int y = 0; y < minRow; y++)
                        newBoard[y][x] = board[y][x];
                }
            }
            board = newBoard;
        }
    }

    // being called by viewToDraw's onSizeChanged() to get the dimension of the View object
    public void onSizeChanged(int w, int h) {
        this.w = w;
        this.h = h;
        if (columns != 0 && rows != 0) {
            blockSize = Math.min(w / columns, h / rows);
            block = new Rect(0, 0, blockSize, blockSize);
        }
    }

    public void onDraw(Canvas canvas) {
        boolean white;
        for (int x = 0; x < columns; x++) {
            white = x % 2 == 0;
            for (int y = 0; y < rows; y++) {
                // draw the board
                white = !white;
                block.offsetTo(blockSize * x, blockSize * y);
                canvas.drawRect(block, white ? whitePaint : blackPaint);
                // draw highlights
                if (selectedX == x && selectedY == y) 
                    canvas.drawRect(block, selectedPaint);
                // draw a piece
                Piece piece = board[y][x];
                if (piece != null)
                    piece.draw(canvas, block, false);
            }
        }
    }

    public void select(float xPix, float yPix) {
        int x = (int) xPix / blockSize;     // clicked on which block
        int y = (int) yPix / blockSize;

        if (x >= columns || y >= rows) return;  // clicking outside of the board

        if (selectedX == x && selectedY == y) {
            selectedX = -1;
            selectedY = -1;
        } else {
            selectedX = x;
            selectedY = y;
        }

        if (buttonPressed)      // add or delete a piece
            board[y][x] = pieceToAdd;
    }

    public void buttonPressed(String tag) {
        buttonPressed = tag != null;
        if (buttonPressed) {
            if (tag.equals("Delete")) {
                pieceToAdd = null;
            } else {
                pieceToAdd = valueOf(tag);
            }
        }
    }
}
