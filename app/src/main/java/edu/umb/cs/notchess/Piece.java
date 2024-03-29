package edu.umb.cs.notchess;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.core.math.MathUtils;
import java.util.ArrayList;

public enum Piece {
    W_King(1), W_Queen(2), W_Bishop(3),
    W_Knight(4), W_Rook(5), W_Pawn(6), W_Heart(7),
    B_King(-1), B_Queen(-2), B_Bishop(-3),
    B_Knight(-4), B_Rook(-5), B_Pawn(-6), B_Heart(-7);

    public final int value;
    private int pawnDirection = -1;             // 0: up, 1: down, 2: left, 3: right

    static boolean isLoaded;
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
        if (isLoaded)
            return;
        isLoaded = true;

        // load bitmaps for each piece
        int numPiece = 7;
        wPieceBitmaps = new Bitmap[numPiece];
        bPieceBitmaps = new Bitmap[numPiece];
        wRotatePieceBitmaps = new Bitmap[numPiece];
        bRotatePieceBitmaps = new Bitmap[numPiece];

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

        wRotatePieceBitmaps[0] = BitmapFactory.decodeResource(res, R.drawable.w_king_180);
        wRotatePieceBitmaps[1] = BitmapFactory.decodeResource(res, R.drawable.w_queen_180);
        wRotatePieceBitmaps[2] = BitmapFactory.decodeResource(res, R.drawable.w_bishop_180);
        wRotatePieceBitmaps[3] = BitmapFactory.decodeResource(res, R.drawable.w_knight_180);
        wRotatePieceBitmaps[4] = BitmapFactory.decodeResource(res, R.drawable.w_rook_180);
        wRotatePieceBitmaps[5] = BitmapFactory.decodeResource(res, R.drawable.w_pawn_180);
        wRotatePieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.w_heart_180);

        bRotatePieceBitmaps[0] = BitmapFactory.decodeResource(res, R.drawable.b_king_180);
        bRotatePieceBitmaps[1] = BitmapFactory.decodeResource(res, R.drawable.b_queen_180);
        bRotatePieceBitmaps[2] = BitmapFactory.decodeResource(res, R.drawable.b_bishop_180);
        bRotatePieceBitmaps[3] = BitmapFactory.decodeResource(res, R.drawable.b_knight_180);
        bRotatePieceBitmaps[4] = BitmapFactory.decodeResource(res, R.drawable.b_rook_180);
        bRotatePieceBitmaps[5] = BitmapFactory.decodeResource(res, R.drawable.b_pawn_180);
        bRotatePieceBitmaps[6] = BitmapFactory.decodeResource(res, R.drawable.b_heart_180);

        int spriteSize = wPieceBitmaps[0].getWidth();
        spriteRect = new Rect(0, 0, spriteSize, spriteSize);
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

    public int[] getPawnForward() {
        return pawnMoveDirections[getPawnDirection()][0].clone();
    }

    // check for promotion before adding a move
    private void addPawnMoves(ArrayList<int[]> moves, Piece[][] board,
                              int xStart, int yStart, int xEnd, int yEnd, boolean getAttacks) {
        if (!getAttacks && isPromotion(board, xEnd, yEnd)) {
            for (int promote = 0; promote <= 3; promote++)  // add move for each the promotion option
                moves.add(new int[]{xStart, yStart, xEnd, yEnd, promote});
        } else {    // no promotion if just want to get attacks
            moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
        }
    }

    private void addSlideMoves(ArrayList<int[]> moves, Piece[][] board,
                               int xStart, int yStart, int dx, int dy, boolean getAttacks) {
        Piece target;
        int xEnd = xStart + dx;
        int yEnd = yStart + dy;

        while (isWithinBoard(board, xEnd, yEnd)) {
            target = board[yEnd][xEnd];
            if (target == null) {
                moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
                xEnd += dx;
                yEnd += dy;
            } else if (getAttacks || !isFriendlyWith(target)) {
                moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
                break;
            } else {    // blocked by a friendly piece
                break;
            }
        }
    }

    private void addBishopMoves(ArrayList<int[]> moves, Piece[][] board,
                                int xStart, int yStart, boolean getAttacks) {
        addSlideMoves(moves, board, xStart, yStart, -1, -1, getAttacks);    // ↖ moves
        addSlideMoves(moves, board, xStart, yStart, 1, 1, getAttacks);    // ↘ moves
        addSlideMoves(moves, board, xStart, yStart, 1, -1, getAttacks);    // ↗ moves
        addSlideMoves(moves, board, xStart, yStart, -1, 1, getAttacks);    // ↙ moves
    }

    private void addRookMoves(ArrayList<int[]> moves, Piece[][] board,
                              int xStart, int yStart, boolean getAttacks) {
        addSlideMoves(moves, board, xStart, yStart, 0, -1, getAttacks);    // ↑ moves
        addSlideMoves(moves, board, xStart, yStart, 0, 1, getAttacks);    // ↓ moves
        addSlideMoves(moves, board, xStart, yStart, -1, 0, getAttacks);    // ← moves
        addSlideMoves(moves, board, xStart, yStart, 1, 0, getAttacks);    // → moves
    }

    // make sure both are king and rook on the same axis, and no pieces in between
    private boolean isCastlingEligible(GameState state,
                                       int xKing, int yKing, int xRook, int yRook) {
        final int CASTLING_DIST = 2;
        // if both king and rook have not moved ...
        if (!state.isMoved[yKing][xKing] && !state.isMoved[yRook][xRook]) {
            int xDist = Math.abs(xRook - xKing);
            int yDist = Math.abs(yRook - yKing);
            // ... and their distance is far enough ...
            if (xDist >= CASTLING_DIST || yDist >= CASTLING_DIST) {
                int xKingEnd = xKing + MathUtils.clamp(xRook - xKing, -2, 2);
                int yKingEnd = yKing + MathUtils.clamp(yRook - yKing, -2, 2);

                // ... and king is not in check and king's path not under attack
                if (yDist == 0) {   // on x-axis
                    int lowerX = Math.min(xKing, xKingEnd);
                    int upperX = Math.max(xKing, xKingEnd);
                    for (int x = lowerX; x <= upperX; x++)   // inclusive both end
                        if (state.isUnderAttackBy(-state.playerToMove, x, yKing)) return false;
                } else {            // on y-axis
                    int lowerY = Math.min(yKing, yKingEnd);
                    int upperY = Math.max(yKing, yKingEnd);
                    for (int y = lowerY; y <= upperY; y++)
                        if (state.isUnderAttackBy(-state.playerToMove, xKing, y)) return false;
                }

                return true;
            }
        }

        return false;
    }

    private void addCastlingMoves(ArrayList<int[]> moves, GameState state,
                                  int xStart, int yStart, int dx, int dy) {
        Piece target;
        int xEnd = xStart + dx;
        int yEnd = yStart + dy;

        while (isWithinBoard(state.board, xEnd, yEnd)) {
            target = state.board[yEnd][xEnd];
            if (target != null) {
                if (target.isRook() && isCastlingEligible(state, xStart, yStart, xEnd, yEnd))
                    moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
                break;  // break b/c there should be no pieces between king and rook when castling
            }
            xEnd += dx;
            yEnd += dy;
        }
    }

    // returns an array list of int array: {xStart, yStart, xEnd, yEnd, promote}
    public ArrayList<int[]> getMoveOptions(GameState state, int xStart, int yStart,
                                           boolean getAttacks) {
        ArrayList<int[]> moves = new ArrayList<>();
        int xEnd, yEnd;
        Piece target;

        switch (this) {
            case W_King:
            case B_King:
                // one step move options
                for (int[] dir : kingMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (isWithinBoard(state.board, xEnd, yEnd)) {
                        target = state.board[yEnd][xEnd];
                        if (getAttacks || target == null || !isFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
                    }
                }

                // special move: castling
                // check on the same axis of 4 directions
                if (!getAttacks) {
                    addCastlingMoves(moves, state, xStart, yStart, 0, -1);  // up
                    addCastlingMoves(moves, state, xStart, yStart, 0, 1);   // down
                    addCastlingMoves(moves, state, xStart, yStart, -1, 0);  // left
                    addCastlingMoves(moves, state, xStart, yStart, 1, 0);   // right
                }
                break;

            case W_Queen:
            case B_Queen:
                addBishopMoves(moves, state.board, xStart, yStart, getAttacks);
                addRookMoves(moves, state.board, xStart, yStart, getAttacks);
                break;

            case W_Bishop:
            case B_Bishop:
                addBishopMoves(moves, state.board, xStart, yStart, getAttacks);
                break;

            case W_Knight:
            case B_Knight:
                for (int[] dir : knightMoveDirections) {
                    xEnd = xStart + dir[0];
                    yEnd = yStart + dir[1];
                    if (isWithinBoard(state.board, xEnd, yEnd)) {
                        target = state.board[yEnd][xEnd];
                        if (getAttacks || target == null || !isFriendlyWith(target))
                            moves.add(new int[]{xStart, yStart, xEnd, yEnd, -1});
                    }
                }
                break;

            case W_Rook:
            case B_Rook:
                addRookMoves(moves, state.board, xStart, yStart, getAttacks);
                break;

            case W_Pawn:
            case B_Pawn:
                final int TWO_STEPS = 2;        // a pawn can move two steps in the beginning
                int steps = state.isMoved[yStart][xStart] ? 1 : TWO_STEPS;
                int[][] pawnMoveDir = pawnMoveDirections[getPawnDirection()];   // {move, kick1, kick2}

                xEnd = xStart;
                yEnd = yStart;
                for (int i = 0; i < steps; i++) {   // move forward
                    xEnd += pawnMoveDir[0][0];
                    yEnd += pawnMoveDir[0][1];
                    if (!getAttacks && isWithinBoard(state.board, xEnd, yEnd) && state.board[yEnd][xEnd] == null) {
                        addPawnMoves(moves, state.board, xStart, yStart, xEnd, yEnd, false);
                    } else {
                        break;
                    }
                }

                for (int i = 1; i <= 2; i++) {  // kick1, kick2
                    xEnd = xStart + pawnMoveDir[i][0];
                    yEnd = yStart + pawnMoveDir[i][1];
                    if (isWithinBoard(state.board, xEnd, yEnd)) {
                        target = state.board[yEnd][xEnd];
                        if (getAttacks || (target != null && !isFriendlyWith(target)))
                            addPawnMoves(moves, state.board, xStart, yStart, xEnd, yEnd, getAttacks);
                    }

                    // special move: en passant (in passing)
                    if (state.lastMove != null) {
                        int xSide = xEnd - pawnMoveDir[0][0];
                        int ySide = yEnd - pawnMoveDir[0][1];
                        int xLastStart = state.lastMove[0];
                        int yLastStart = state.lastMove[1];
                        int xLastEnd = state.lastMove[2];
                        int yLastEnd = state.lastMove[3];

                        // if opponent's last move moved a piece to this pawn's side ...
                        if (xSide == xLastEnd && ySide == yLastEnd) {
                            Piece lastMovedPiece = state.board[yLastEnd][xLastEnd];
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
                                        addPawnMoves(moves, state.board, xStart, yStart, xEnd, yEnd, getAttacks);
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

    public boolean isFriendlyWith(Piece target) {
        return (value > 0 && target.value > 0) || (value < 0 && target.value < 0);
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
            int[] forward = getPawnForward();
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
