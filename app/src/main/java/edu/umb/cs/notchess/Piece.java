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
    private int pawnDirection = -1;

    static Rect spriteRect;
    static Bitmap[] wPieceBitmaps;
    static Bitmap[] bPieceBitmaps;
    static Bitmap[] wRotatePieceBitmaps;
    static Bitmap[] bRotatePieceBitmaps;

    private final int[][] kingMoveDirections = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {1, 0},
            {-1, 1}, {0, 1}, {1, 1}};
    private final int[][] knightMoveDirections = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2},
            {-2, -1}, {-2, 1}, {-1, 2}};
    // pawn move directions: up, down, left, right
    private final int[][][] pawnMoveDirections = {
            {{0, -1}, {-1, -1}, {1, -1}},      // {move, kick1, kick2}
            {{0, 1}, {-1, 1}, {1, 1}},
            {{-1, 0}, {-1, -1}, {-1, 1}},
            {{1, 0}, {1, -1}, {1, 1}}
    };

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

    public boolean isProtectee() {
        switch (this) {
            case W_King:
            case W_Heart:
            case B_King:
            case B_Heart:
                return true;
            default:
                return false;
        }
    }

    private boolean notFriendlyWith(Piece target) {
        return (value > 0 && target.value < 0) || (value < 0 && target.value > 0);
    }

    private boolean withinBoard(Piece[][] board, int x, int y) {
        return x >= 0 && x < board[0].length && y >= 0 && y < board.length;
    }

    private int getPawnDirection() {
        if (pawnDirection == -1) {  // return default value
            if (this == W_Pawn)
                return 0;           // up
            else if (this == B_Pawn)
                return 1;           // down
        }
        return pawnDirection;       // returns -1 of not a pawn
    }

    public boolean setPawnDirection(int direction) {
        if ((this == W_Pawn || this == B_Pawn) && direction >= 0 && direction <= 3) {
            pawnDirection = direction;
            return true;
        }
        return false;
    }

    private void addSlideMoves(ArrayList<int[]> moves, Piece[][] board,
                               int xStart, int yStart, int dx, int dy) {
        Piece target;
        int xEnd = xStart + dx;
        int yEnd = yStart + dy;

        while (withinBoard(board, xEnd, yEnd)) {
            target = board[yEnd][xEnd];
            if (target == null) {
                moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                xEnd += dx;
                yEnd += dy;
            } else if (notFriendlyWith(target)) {
                moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                break;
            } else {    // blocked by a friendly piece
                break;
            }
        }
    }

    private void addBishopMoves(ArrayList<int[]> moves, Piece[][] board,
                                int xStart, int yStart) {
        addSlideMoves(moves, board, xStart, yStart, -1, -1);    // ↖ moves
        addSlideMoves(moves, board, xStart, yStart, 1, 1);    // ↘ moves
        addSlideMoves(moves, board, xStart, yStart, 1, -1);    // ↗ moves
        addSlideMoves(moves, board, xStart, yStart, -1, 1);    // ↙ moves
    }

    private void addRookMoves(ArrayList<int[]> moves, Piece[][] board,
                              int xStart, int yStart) {
        addSlideMoves(moves, board, xStart, yStart, 0, -1);    // ↑ moves
        addSlideMoves(moves, board, xStart, yStart, 0, 1);    // ↓ moves
        addSlideMoves(moves, board, xStart, yStart, -1, 0);    // ← moves
        addSlideMoves(moves, board, xStart, yStart, 1, 0);    // → moves
    }

    public ArrayList<int[]> getMoveOptions(Piece[][] board, int xStart, int yStart) {
        ArrayList<int[]> moves = new ArrayList<>();
        int xEnd, yEnd;
        Piece target;

        switch (this) {
            case W_King:
            case B_King:
                for (int[] dir : kingMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (withinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target == null || notFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    }
                }
                break;

            case W_Queen:
            case B_Queen:
                addBishopMoves(moves, board, xStart, yStart);
                addRookMoves(moves, board, xStart, yStart);
                break;

            case W_Bishop:
            case B_Bishop:
                addBishopMoves(moves, board, xStart, yStart);
                break;

            case W_Knight:
            case B_Knight:
                for (int[] dir : knightMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (withinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target == null || notFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    }
                }
                break;

            case W_Rook:
            case B_Rook:
                addRookMoves(moves, board, xStart, yStart);
                break;

            case W_Pawn:
            case B_Pawn:
                int[][] pawnMoveDir = pawnMoveDirections[getPawnDirection()];

                xEnd = xStart;
                yEnd = yStart;
                for (int i = 0; i < 2; i++) {   // move forward up to two steps
                    xEnd += pawnMoveDir[0][0];
                    yEnd += pawnMoveDir[0][1];
                    if (withinBoard(board, xEnd, yEnd) && board[yEnd][xEnd] == null) {
                        moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    } else {
                        break;
                    }
                }

                for (int i = 1; i <= 2; i++) {  // kick1, kick2
                    xEnd = xStart + pawnMoveDir[i][0];
                    yEnd = yStart + pawnMoveDir[i][1];
                    if (withinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target != null && notFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    }
                }
        }

        return moves;
    }
}
