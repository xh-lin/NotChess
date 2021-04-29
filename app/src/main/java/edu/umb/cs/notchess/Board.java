package edu.umb.cs.notchess;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Board {
    private final int cols;
    private final int rows;

    private final Paint darkPaint;
    private final Paint lightPaint;
    private final Paint selectedPaint;

    private int blockSize;
    private Rect block;

    private int selectedCol;
    private int selectedRow;

    public Board(int cols, int rows, Paint darkPaint, Paint lightPaint, Paint selectedPaint) {
        this.cols = cols;
        this.rows = rows;
        this.darkPaint = darkPaint;
        this.lightPaint = lightPaint;
        this.selectedPaint = selectedPaint;
        this.selectedCol = -1;
        this.selectedRow = -1;
    }

    public void resize(int w, int h) {
        int blockSize = Math.min(w/cols, h/rows);
        this.blockSize = blockSize;
        block = new Rect(0, 0, blockSize, blockSize);
    }

    public void draw(Canvas canvas) {
        boolean dark;
        for (int i = 0; i < rows; i++) {
            dark = i % 2 == 0;
            for (int j = 0; j < cols; j++) {
                block.offsetTo(j*blockSize, i*blockSize);
                dark = !dark;
                canvas.drawRect(block, (j == selectedCol && i == selectedRow) ? selectedPaint :
                        dark ? darkPaint : lightPaint);
            }
        }
    }

    public void select(float x, float y) {
        int sCol = (int) x / blockSize;
        int sRow = (int) y / blockSize;
        if (sCol == selectedCol && sRow == selectedRow) {
            selectedCol = -1;
            selectedRow = -1;
        } else {
            selectedCol = sCol;
            selectedRow = sRow;
        }
    }

}
