package edu.umb.cs.notchess;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import java.util.ArrayList;

public enum Piece {
    W_King(1), W_Queen(2), W_Bishop(3),
    W_Knight(4), W_Rook(5), W_Pawn(6), W_Heart(7),
    B_King(-1), B_Queen(-2), B_Bishop(-3),
    B_Knight(-4), B_Rook(-5), B_Pawn(-6), B_Heart(-7);

    public final int value;
    static Rect spriteRect;
    static Bitmap[] wPieceBitmaps;
    static Bitmap[] bPieceBitmaps;
    static Bitmap[] wRotatePieceBitmaps;
    static Bitmap[] bRotatePieceBitmaps;
    private final int[][] knightMoveDirections = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2},
            {-2, -1}, {-2, 1}, {-1, 2}};

    Piece(int value) {
        this.value = value;
    }

    static void loadAssets(final Resources res) {
        Bitmap piecesBitmap = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
        int spriteSize = piecesBitmap.getHeight() / 2;
        spriteRect = new Rect(0, 0, spriteSize, spriteSize);

        // get bitmaps for each piece
        int numPiece = 7;
        wPieceBitmaps = new Bitmap[numPiece];
        bPieceBitmaps = new Bitmap[numPiece];
        for (int i = 0; i < numPiece - 1; i ++) {
            spriteRect.offsetTo(spriteSize * i, 0);
            wPieceBitmaps[i] = Bitmap.createBitmap(piecesBitmap,
                    spriteRect.left, spriteRect.top, spriteRect.width(), spriteRect.height());
            spriteRect.offsetTo(spriteSize * i, spriteSize);
            bPieceBitmaps[i] = Bitmap.createBitmap(piecesBitmap,
                    spriteRect.left, spriteRect.top, spriteRect.width(), spriteRect.height());
        }
        wPieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.heart_white);
        bPieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.heart_black);
        spriteRect.offsetTo(0, 0);  // reset for later use

        wRotatePieceBitmaps = new Bitmap[numPiece];
        bRotatePieceBitmaps = new Bitmap[numPiece];
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(180);

        // create rotated bitmap of pieces
        for (int i = 0; i < numPiece; i++) {
            wRotatePieceBitmaps[i] = Bitmap.createBitmap(wPieceBitmaps[i],
                    0, 0, spriteRect.width(), spriteRect.height(),
                    rotateMatrix, false);
            bRotatePieceBitmaps[i] = Bitmap.createBitmap(bPieceBitmaps[i],
                    0, 0, spriteRect.width(), spriteRect.height(),
                    rotateMatrix, false);
        }
    }

    public void draw(Canvas canvas, Rect dstBlock, Boolean rotate) {
        Bitmap pieceBitmap;
        if (value > 0)
            pieceBitmap = rotate ? wRotatePieceBitmaps[value-1] : wPieceBitmaps[value-1];
        else
            pieceBitmap = rotate ? bRotatePieceBitmaps[-value-1] : bPieceBitmaps[-value-1];
        canvas.drawBitmap(pieceBitmap, spriteRect, dstBlock, null);
    }

    public boolean belongsTo(int player) {
        return (value > 0 && player == 1) || (value < 0 && player == -1);
    }

    public boolean friendlyWith(Piece target) {
        if (target == null)
            return false;
        return (value > 0 && target.value > 0) || (value < 0 && target.value < 0);
    }

    public ArrayList<int[]> getMoveOptions(Piece[][] board, int xStart, int yStart) {
        ArrayList<int[]> moves = new ArrayList<>();
        int boardWidth = board[0].length;
        int boardHeight = board.length;
        int xEnd, yEnd;
        switch (this) {
            case W_Knight:
            case B_Knight:
                for (int[] dir : knightMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (xEnd >= 0 && xEnd < boardWidth && yEnd >= 0 && yEnd < boardHeight
                            && !friendlyWith(board[yEnd][xEnd]))
                        moves.add(new int[] {xStart, yStart, xEnd, yEnd});
                }
        }
        return moves;
    }
}
