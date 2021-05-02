package edu.umb.cs.notchess;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Chessboard {
    public enum Piece {
        W_King(1), W_Queen(2), W_Bishop(3),
        W_Knight(4), W_Rook(5), W_Pawn(6), W_Heart(7),
        B_King(-1), B_Queen(-2), B_Bishop(-3),
        B_Knight(-4), B_Rook(-5), B_Pawn(-6), B_Heart(-7);

        public final int value;
        private final int[][] knightMoves = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1},
                {-2, 1}, {-1, 2}};

        static Rect spriteRect;
        static Bitmap[] wPieceBitmaps;
        static Bitmap[] bPieceBitmaps;
        static Bitmap[] wrPieceBitmaps;
        static Bitmap[] brPieceBitmaps;

        Piece(int value) {
            this.value = value;
        }

        static void loadAssets(Resources res) {
            Bitmap piecesBitmap = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
            int spriteSize = piecesBitmap.getHeight() / 2;
            spriteRect = new Rect(0, 0, spriteSize, spriteSize);

            // get bitmap for pieces
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
            spriteRect.offsetTo(0, 0);

            wrPieceBitmaps = new Bitmap[numPiece];
            brPieceBitmaps = new Bitmap[numPiece];
            Matrix rotateMatrix = new Matrix();
            rotateMatrix.postRotate(180);

            // create rotated bitmap of pieces
            for (int i = 0; i < numPiece; i++) {
                wrPieceBitmaps[i] = Bitmap.createBitmap(wPieceBitmaps[i],
                        0, 0, wPieceBitmaps[i].getWidth(), wPieceBitmaps[i].getHeight(),
                        rotateMatrix, false);
                brPieceBitmaps[i] = Bitmap.createBitmap(bPieceBitmaps[i],
                        0, 0, wPieceBitmaps[i].getWidth(), wPieceBitmaps[i].getHeight(),
                        rotateMatrix, false);
            }
        }

        public void draw(Canvas canvas, Rect dstBlock, Boolean rotate) {
            Bitmap pieceBitmap;
            if (value > 1)
                pieceBitmap = rotate ? wrPieceBitmaps[value-1] : wPieceBitmaps[value-1];
            else
                pieceBitmap = rotate ? brPieceBitmaps[-value-1] : bPieceBitmaps[-value-1];
            canvas.drawBitmap(pieceBitmap, spriteRect, dstBlock, null);
        }
        
        public boolean validMove(Piece[][] board, int xStart, int yStart, int xEnd, int yEnd) {
            Piece target = board[yEnd][xEnd];
            if (target != null && (this.value > 0 && target.value > 0 || this.value < 0 && target.value < 0))
                return false;

            switch (this) {
                case W_Knight:
                case B_Knight:
                    for (int[] move : knightMoves)
                        if (xStart+move[0] == xEnd && yStart+move[1] == yEnd) return true;
                    break;
            }

            return false;
        }
    }



    private final Context context;
    private final Paint whitePaint;
    private final Paint blackPaint;
    private final Paint selectedPaint;

    private final int width;
    private final int height;
    private final Piece[][] board;

    private final View indicatorView;
    private final boolean rotatePieces;

    private final int[] wPieces = {0, 0};     // {protectees, protectors}
    private final int[] bPieces = {0, 0};

    // for drawing
    private int blockSize;
    private Rect block;

    // for making moves
    private int selectedX = -1;
    private int selectedY = -1;

    private int playerToMove = 1;       // 1 -> White, -1 -> Black
    private int winner = 0;             // 1 -> White, -1 -> Black, 0 -> game not over

    public Chessboard(Context context, Paint whitePaint, Paint blackPaint, Paint selectedPaint,
                      Piece[][] board, View indicatorView) {
        this.context = context;
        this.whitePaint = whitePaint;
        this.blackPaint = blackPaint;
        this.selectedPaint = selectedPaint;
        this.width = board[0].length;
        this.height = board.length;
        this.board = board;
        this.indicatorView = indicatorView;
        this.rotatePieces = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.rotate_pieces), true);

        for (Piece[] row : board) {     // counting pieces
            for (Piece piece : row) {
                if (piece != null) {
                    if (piece.value > 0) {
                        if (piece == Piece.W_King || piece == Piece.W_Heart)
                            wPieces[0] += 1;
                        else
                            wPieces[1] += 1;
                    } else {
                        if (piece == Piece.B_King || piece == Piece.B_Heart)
                            bPieces[0] += 1;
                        else
                            bPieces[1] += 1;
                    }
                }
            }
        }

        Piece.loadAssets(context.getResources());
        changeMoveIndicator();
    }

    public void resize(int w, int h) {
        int newBlockSize = Math.min(w/width, h/height);
        this.blockSize = newBlockSize;
        block = new Rect(0, 0, newBlockSize, newBlockSize);
    }

    public void draw(Canvas canvas) {
        boolean white;
        for (int x = 0; x < width; x++) {
            white = x % 2 == 0;
            for (int y = 0; y < height; y++) {
                white = !white;
                block.offsetTo(x*blockSize, y*blockSize);
                canvas.drawRect(block, (x == selectedX && y == selectedY) ? selectedPaint :
                        white ? whitePaint : blackPaint);   // draw the board
                if (board[y][x] != null)    // draw a piece
                    board[y][x].draw(canvas, block, playerToMove != 1 && rotatePieces);
            }
        }
    }

    public void select(float xPix, float yPix) {
        if (winner != 0) checkGameState();

        int x = (int) xPix / blockSize;
        int y = (int) yPix / blockSize;

        if (x >= width || y >= height) return;  // clicking outside of the board

        if (selectedX == -1) {  // nothing selected
            Piece piece = board[y][x];
            if (piece != null &&
                    (playerToMove == 1 && piece.value > 0
                    || playerToMove == -1 && piece.value < 0)) {  // player to move selects a piece
                selectedX = x;
                selectedY = y;
            }
        } else {
            if (selectedX == x && selectedY == y) {  // deselect
                selectedX = -1;
            } else if (board[selectedY][selectedX].validMove(board, selectedX, selectedY, x, y)) {  // make a move
                Piece kicked = board[y][x];
                if (kicked != null) {
                    if (kicked.value > 0) {
                        if (kicked == Piece.W_King || kicked == Piece.W_Heart)
                            wPieces[0] -= 1;
                        else
                            wPieces[1] -= 1;
                    } else {
                        if (kicked == Piece.B_King || kicked == Piece.B_Heart)
                            bPieces[0] -= 1;
                        else
                            bPieces[1] -= 1;
                    }
                }

                board[y][x] = board[selectedY][selectedX];
                board[selectedY][selectedX] = null;
                selectedX = -1;
                playerToMove *= -1;     // opponent is the next player to move
                changeMoveIndicator();

                checkGameState();
            }
        }
    }

    private void changeMoveIndicator() {
        if (playerToMove == 1)
            indicatorView.setBackgroundColor(context.getResources().getColor(R.color.beige));
        else
            indicatorView.setBackgroundColor(context.getResources().getColor(R.color.brown));
    }

    private void checkGameState() {
        if (wPieces[0] == 0 || wPieces[1] == 0) {
            winner = -1;
            gameOverDialog(context.getString(R.string.black_wins));
        } else if (bPieces[0] == 0 || bPieces[1] == 0) {
            winner = 1;
            gameOverDialog(context.getString(R.string.white_wins));
        }
    }

    private void gameOverDialog(String text) {
        Intent intent = new Intent(context, GameOverDialogActivity.class);
        intent.putExtra(context.getString(R.string.winner_text), text);
        context.startActivity(intent);
    }

}
