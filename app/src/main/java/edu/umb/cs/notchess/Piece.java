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
    private int pawnDirection = -1;             // 0: up, 1: down, 2: left, 3: right

    static Rect spriteRect;                     // for drawing a sprite
    static Bitmap[] wPieceBitmaps;              // containing sprites
    static Bitmap[] bPieceBitmaps;
    static Bitmap[] wRotatePieceBitmaps;        // containing sprites rotated 180 degrees
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

    /*============================================================================================*/
    /* drawing */

    static void loadAssets(final Resources res) {
        // load bitmaps for each piece
        int numPiece = 7;
        wPieceBitmaps = new Bitmap[numPiece];
        bPieceBitmaps = new Bitmap[numPiece];

        wPieceBitmaps[0] = BitmapFactory.decodeResource(res, R.drawable.w_king);
        wPieceBitmaps[1] = BitmapFactory.decodeResource(res, R.drawable.w_queen);
        wPieceBitmaps[2] = BitmapFactory.decodeResource(res, R.drawable.w_bishop);
        wPieceBitmaps[3] = BitmapFactory.decodeResource(res, R.drawable.w_knight);
        wPieceBitmaps[4] = BitmapFactory.decodeResource(res, R.drawable.w_rook);
        wPieceBitmaps[5] = BitmapFactory.decodeResource(res, R.drawable.w_pawn);
        wPieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.w_heart);

        bPieceBitmaps[0] = BitmapFactory.decodeResource(res, R.drawable.b_king);
        bPieceBitmaps[1] = BitmapFactory.decodeResource(res, R.drawable.b_queen);
        bPieceBitmaps[2] = BitmapFactory.decodeResource(res, R.drawable.b_bishop);
        bPieceBitmaps[3] = BitmapFactory.decodeResource(res, R.drawable.b_knight);
        bPieceBitmaps[4] = BitmapFactory.decodeResource(res, R.drawable.b_rook);
        bPieceBitmaps[5] = BitmapFactory.decodeResource(res, R.drawable.b_pawn);
        bPieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.b_heart);

        wRotatePieceBitmaps = new Bitmap[numPiece];
        bRotatePieceBitmaps = new Bitmap[numPiece];
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.postRotate(180);

        int spriteSize = wPieceBitmaps[0].getWidth();
        spriteRect = new Rect(0, 0, spriteSize, spriteSize);

        // create rotated bitmap of pieces
        for (int i = 0; i < numPiece; i++) {
            wRotatePieceBitmaps[i] = Bitmap.createBitmap(wPieceBitmaps[i],
                    0, 0, spriteSize, spriteSize,
                    rotateMatrix, false);
            bRotatePieceBitmaps[i] = Bitmap.createBitmap(bPieceBitmaps[i],
                    0, 0, spriteSize, spriteSize,
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

    /*============================================================================================*/
    /* get move options */

    // set the moving direction of W_Pawn or B_Pawn
    public boolean setPawnDirection(int direction) {
        if (this.isPawn() && direction >= 0 && direction <= 3) {
            pawnDirection = direction;
            return true;
        }
        return false;   // returns false if this is not a pawn or invalid input
    }

    private int getPawnDirection() {
        if (pawnDirection == -1) {  // return default value
            if (this == W_Pawn)
                return 0;           // up
            else if (this == B_Pawn)
                return 1;           // down
        }
        return pawnDirection;       // will return -1 if not pawn
    }

    public int[] getPawnsForward() {
        return pawnMoveDirections[getPawnDirection()][0].clone();
    }

    private void addSlideMoves(ArrayList<int[]> moves, Piece[][] board,
                               int xStart, int yStart, int dx, int dy) {
        Piece target;
        int xEnd = xStart + dx;
        int yEnd = yStart + dy;

        while (isWithinBoard(board, xEnd, yEnd)) {
            target = board[yEnd][xEnd];
            if (target == null) {
                moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                xEnd += dx;
                yEnd += dy;
            } else if (isNotFriendlyWith(target)) {
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

    public ArrayList<int[]> getMoveOptions(Piece[][] board, int xStart, int yStart, boolean isMoved,
                                           int[] lastMove) {
        ArrayList<int[]> moves = new ArrayList<>();
        int xEnd, yEnd;
        Piece target;

        switch (this) {
            case W_King:
            case B_King:
                for (int[] dir : kingMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (isWithinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target == null || isNotFriendlyWith(target))
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
                    if (isWithinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target == null || isNotFriendlyWith(target))
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
                final int TWO_STEPS = 2;        // a pawn can move two steps in the beginning
                int steps = isMoved ? 1 : TWO_STEPS;
                int[][] pawnMoveDir = pawnMoveDirections[getPawnDirection()];   // {move, kick1, kick2}

                xEnd = xStart;
                yEnd = yStart;
                for (int i = 0; i < steps; i++) {   // move forward
                    xEnd += pawnMoveDir[0][0];
                    yEnd += pawnMoveDir[0][1];
                    if (isWithinBoard(board, xEnd, yEnd) && board[yEnd][xEnd] == null) {
                        moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    } else {
                        break;
                    }
                }

                for (int i = 1; i <= 2; i++) {  // kick1, kick2
                    xEnd = xStart + pawnMoveDir[i][0];
                    yEnd = yStart + pawnMoveDir[i][1];
                    if (isWithinBoard(board, xEnd, yEnd)) {
                        target = board[yEnd][xEnd];
                        if (target != null && isNotFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                    }

                    // special move: en passant (in passing)
                    if (lastMove != null) {
                        int xSide = xEnd - pawnMoveDir[0][0];
                        int ySide = yEnd - pawnMoveDir[0][1];
                        int xLastStart = lastMove[0];
                        int yLastStart = lastMove[1];
                        int xLastEnd = lastMove[2];
                        int yLastEnd = lastMove[3];

                        // if opponent's last move moved a piece to this pawn's side ...
                        if (xSide == xLastEnd && ySide == yLastEnd) {
                            Piece lastMovedPiece = board[yLastEnd][xLastEnd];
                            // ... and that was a pawn ...
                            if (lastMovedPiece.isPawn()) {
                                int lastMoveSteps = Math.abs(xLastEnd - xLastStart) + Math.abs(yLastEnd - yLastStart);
                                // ... and that pawn moved two steps ...
                                if (lastMoveSteps == TWO_STEPS) {
                                    // opposite direction of last move
                                    int xLastOpDir = (xLastStart - xLastEnd) / TWO_STEPS;
                                    int yLastOpDir = (yLastStart - yLastEnd) / TWO_STEPS;
                                    // ... and that pawn moves in a opposite direction of this pawn ...
                                    if (xLastOpDir == pawnMoveDir[0][0] && yLastOpDir == pawnMoveDir[0][1]) {
                                        // ... then this pawn can capture that passing pawn
                                        moves.add(new int[]{xStart, yStart, xEnd, yEnd});
                                    }
                                }
                            }
                        }
                    }
                }
        }

        return moves;
    }

    /*============================================================================================*/
    /* utils */

    private boolean isNotFriendlyWith(Piece target) {
        return (value > 0 && target.value < 0) || (value < 0 && target.value > 0);
    }

    private boolean isWithinBoard(Piece[][] board, int x, int y) {
        return x >= 0 && x < board[0].length && y >= 0 && y < board.length;
    }

    public boolean isBelongingTo(int player) {
        return (value > 0 && player == 1) || (value < 0 && player == -1);
    }

    // does a pawn reach the edge?
    public boolean isPromotion(Piece[][] board, int xEnd, int yEnd) {
        if (this.isPawn()) {
            int[] forward = getPawnsForward();
            int frontX = xEnd + forward[0];
            int frontY = yEnd + forward[1];
            return !isWithinBoard(board, frontX, frontY);
        }
        return false;
    }

    public boolean isKing() {
        return this == W_King || this == B_King;
    }

    public boolean isQueen() {
        return this == W_Queen || this == B_Queen;
    }

    public boolean isBishop() {
        return this == W_Bishop || this == B_Bishop;
    }

    public boolean isKnight() {
        return this == W_Knight || this == B_Knight;
    }

    public boolean isRook() {
        return this == W_Rook || this == B_Rook;
    }

    public boolean isPawn() {
        return this == W_Pawn || this == B_Pawn;
    }

    public boolean isHeart() {
        return this == W_Heart || this == B_Heart;
    }
}
