package edu.umb.cs.notchess;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
        private int[][] knightMoves = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};

        static Bitmap wHeartBitmap;
        static Bitmap bHeartBitmap;
        static Rect heartRect;
        static Bitmap piecesBitmap;
        static int spriteSize;
        static Rect spriteRect;

        Piece(final int value) {
            this.value = value;
        }

        static void loadAssets(Resources res) {
            wHeartBitmap = BitmapFactory.decodeResource(res, R.drawable.heart_white);
            bHeartBitmap = BitmapFactory.decodeResource(res, R.drawable.heart_black);
            heartRect = new Rect(0, 0, bHeartBitmap.getWidth(), bHeartBitmap.getHeight());

            piecesBitmap = BitmapFactory.decodeResource(res, R.drawable.chess_pieces);
            spriteSize = piecesBitmap.getHeight() / 2;
            spriteRect = new Rect(0, 0, spriteSize, spriteSize);
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

        public void draw(Canvas canvas, Rect dstBlock) {
            switch (this) {
                case W_King:
                    spriteRect.offsetTo(0, 0);
                    break;
                case W_Queen:
                    spriteRect.offsetTo(spriteSize, 0);
                    break;
                case W_Bishop:
                    spriteRect.offsetTo(spriteSize * 2, 0);
                    break;
                case W_Knight:
                    spriteRect.offsetTo(spriteSize * 3, 0);
                    break;
                case W_Rook:
                    spriteRect.offsetTo(spriteSize * 4, 0);
                    break;
                case W_Pawn:
                    spriteRect.offsetTo(spriteSize * 5, 0);
                    break;
                case W_Heart:
                    canvas.drawBitmap(wHeartBitmap, heartRect, dstBlock, null);
                    return;
                case B_King:
                    spriteRect.offsetTo(0, spriteSize);
                    break;
                case B_Queen:
                    spriteRect.offsetTo(spriteSize, spriteSize);
                    break;
                case B_Bishop:
                    spriteRect.offsetTo(spriteSize * 2, spriteSize);
                    break;
                case B_Knight:
                    spriteRect.offsetTo(spriteSize * 3, spriteSize);
                    break;
                case B_Rook:
                    spriteRect.offsetTo(spriteSize * 4, spriteSize);
                    break;
                case B_Pawn:
                    spriteRect.offsetTo(spriteSize * 5, spriteSize);
                    break;
                case B_Heart:
                    canvas.drawBitmap(bHeartBitmap, heartRect, dstBlock, null);
                    return;
            }
            canvas.drawBitmap(piecesBitmap, spriteRect, dstBlock, null);
        }
    }



    private final Context context;
    private final Paint whitePaint;
    private final Paint blackPaint;
    private final Paint selectedPaint;

    private final int width;
    private final int height;
    private Piece[][] board;

    private final View indicatorView;
    static String winnerTextKey = "winnerTextKey";

    private int[] wPieces = {0, 0};     // {protectees, protectors}
    private int[] bPieces = {0, 0};

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
                        white ? whitePaint : blackPaint);
                if (board[y][x] != null)
                    board[y][x].draw(canvas, block);
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
            if (piece != null && (playerToMove == 1 && piece.value > 0 || playerToMove == -1 && piece.value < 0)) {  // player to move selects a piece
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
            gameOverDialog("Black Wins");
        } else if (bPieces[0] == 0 || bPieces[1] == 0) {
            winner = 1;
            gameOverDialog("White Wins");
        }
    }

    private void gameOverDialog(String text) {
        Intent intent = new Intent(context, GameOverDialogActivity.class);
        intent.putExtra(winnerTextKey, text);
        context.startActivity(intent);
    }

}
