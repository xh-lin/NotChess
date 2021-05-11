package edu.umb.cs.notchess;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class Chessboard {
    private final Context context;
    private final int level;                // index of Levels.boards
    private final int aiOption;             // 0 -> disable, 1 -> Black, 2 -> White
    private final View gameView;            // the view to draw the board
    private final TextView indicatorView;   // showing who's move now

    private final Piece[][] board;
    private final int width;
    private final int height;

    private final int[] wPieceCount;    // game over if either is zero: {protectees, protectors}
    private final int[] bPieceCount;    // protectees = King, Heart    protectors = other pieces

    // for drawing

    private final Paint whitePaint;
    private final Paint blackPaint;
    private final Paint selectedPaint;

    private int blockSize;
    private Rect block;                 // for drawing each block of the board
    private final boolean rotatePieces; // rotate pieces option from the setting

    private final Bitmap moveBitmap;
    private final Bitmap kickBitmap;
    private final Rect spriteRect;      // for drawing moveBitmap and kickBitmap
    private int[] lastMove;             // {xStart, yStart, xEnd, yEnd}

    // for making moves

    private int selectedX = -1;         // piece currently selected by human player
    private int selectedY = -1;
    private ArrayList<int[]> moveList;  // move options of currently selected piece

    private int playerToMove = 1;       // 1 -> White, -1 -> Black
    private int winner = 0;             // 1 -> White, -1 -> Black, 0 -> game not over
    private int ai = 0;                 // 1 -> White, -1 -> Black, 0 -> disable
    private int moveCount = 0;          // for indicatorView to display

    public Chessboard(Context context, int level, int aiOption, View gameView, TextView indicatorView) {
        this.context = context;
        this.level = level;
        this.aiOption = aiOption;
        this.gameView = gameView;
        this.indicatorView = indicatorView;

        // load the chess board
        Piece[][] board = Levels.boards[level];
        this.board = new Piece[board.length][];
        for (int i = 0; i < board.length; i++)
            this.board[i] = board[i].clone();
        this.width = board[0].length;
        this.height = board.length;

        // count number of pieces for determining whether game is over
        int[][] wbPC = countPieces(board);
        wPieceCount = wbPC[0];
        bPieceCount = wbPC[1];

        // playerAI option
        switch (aiOption) {
            case 1: // Black
                this.ai = -1;
                break;
            case 2: // White
                this.ai = 1;
        }

        // paints
        this.whitePaint = new Paint();
        this.whitePaint.setColor(ContextCompat.getColor(context, R.color.beige));
        this.blackPaint = new Paint();
        this.blackPaint.setColor(ContextCompat.getColor(context, R.color.brown));
        this.selectedPaint = new Paint();
        this.selectedPaint.setColor(ContextCompat.getColor(context, R.color.light_yellow));

        // rotate pieces setting
        this.rotatePieces = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.rotate_pieces), true);

        // load drawables for move options drawing
        this.moveBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.move);
        this.kickBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.kick);
        this.spriteRect = new Rect(0, 0, moveBitmap.getWidth(), moveBitmap.getHeight());

        Piece.loadAssets(context.getResources());   // load drawables for Piece
        updateMoveIndicator();

        // execute playerAI if it is the first to move
        if (ai == playerToMove)
            new AIThink().execute(ai);
    }

    int[][] countPieces(Piece[][] board) {
        int[] wPC = {0, 0};     // {protectees, protectors}
        int[] bPC = {0, 0};

        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece != null) {
                    if (piece.value > 0) {  // White piece
                        if (piece.isProtectee())
                            wPC[0] += 1;
                        else
                            wPC[1] += 1;
                    } else {                // Black piece
                        if (piece.isProtectee())
                            bPC[0] += 1;
                        else
                            bPC[1] += 1;
                    }
                }
            }
        }

        return new int[][]{wPC, bPC};
    }

    // being called by GameView.onSizeChanged() to get the dimension of the View object
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
                // draw the board
                white = !white;
                block.offsetTo(blockSize * x, blockSize * y);
                canvas.drawRect(block, white ? whitePaint : blackPaint);
                // draw highlights for selected and last move
                if (selectedX == x && selectedY == y
                        || lastMove != null && (lastMove[0] == x && lastMove[1] == y
                        || lastMove[2] == x && lastMove[3] == y))
                    canvas.drawRect(block, selectedPaint);
                // draw a piece
                Piece piece = board[y][x];
                if (piece != null)
                    piece.draw(canvas, block, playerToMove == -1 && rotatePieces && ai == 0);
            }
        }
        // draw move options
        if (moveList != null) {
            for (int[] move : moveList) {
                int xEnd = move[2];
                int yEnd = move[3];
                block.offsetTo(blockSize * xEnd, blockSize * yEnd);
                canvas.drawBitmap(board[yEnd][xEnd] == null ? moveBitmap : kickBitmap,
                        spriteRect, block, null);
            }
        }
    }

    // handles player action of selecting and making moves
    public void select(float xPix, float yPix) {
        if (winner != 0) checkGameState();  // game is already over

        int x = (int) xPix / blockSize;     // clicked on which block
        int y = (int) yPix / blockSize;

        if (x >= width || y >= height) return;  // clicking outside of the board

        Piece piece = board[y][x];
        if (selectedX == x && selectedY == y) { // deselect
            deselect();
        } else if (piece != null && piece.belongsTo(playerToMove) && playerToMove != ai) {
            selectedX = x;  // player selects a piece
            selectedY = y;
            moveList = piece.getMoveOptions(board, x, y);
        } else if (selectedX != -1 && validMove(x, y)) {    // player makes a move
            makeMove(selectedX, selectedY, x, y);
            if (winner == 0 && playerToMove == ai)
                new AIThink().execute(ai);
        }
    }

    private boolean validMove(int xEnd, int yEnd) {
        for (int[] move : moveList) {
            if (xEnd == move[2] && yEnd == move[3])
                return true;
        }
        return false;
    }

    private void makeMove(int xStart, int yStart, int xEnd, int yEnd) {
        lastMove = new int[]{xStart, yStart, xEnd, yEnd};
        moveCount += 1;
        Piece kicked = board[yEnd][xEnd];
        if (kicked != null) {
            if (kicked.value > 0) {
                if (kicked == Piece.W_King || kicked == Piece.W_Heart)
                    wPieceCount[0] -= 1;
                else
                    wPieceCount[1] -= 1;
            } else {
                if (kicked == Piece.B_King || kicked == Piece.B_Heart)
                    bPieceCount[0] -= 1;
                else
                    bPieceCount[1] -= 1;
            }
        }

        board[yEnd][xEnd] = board[yStart][xStart];
        board[yStart][xStart] = null;
        playerToMove = -playerToMove;     // opponent is the next player to move
        deselect();

        updateMoveIndicator();
        checkGameState();
    }

    private void deselect() {
        selectedX = -1;
        moveList = null;
    }

    private void updateMoveIndicator() {
        int id;
        if (playerToMove == 1)
            id = ai == 1 ? R.string.white_is_thinking : R.string.whites_move;
        else
            id = ai == -1 ? R.string.black_is_thinking : R.string.blacks_move;
        String text = String.format(context.getResources().getString(id), moveCount);
        indicatorView.setText(text);
    }

    // check whether game is over
    private void checkGameState() {
        if (wPieceCount[0] == 0 || wPieceCount[1] == 0) {
            winner = -1;
            gameOverDialog(context.getString(R.string.black_wins));
        } else if (bPieceCount[0] == 0 || bPieceCount[1] == 0) {
            winner = 1;
            gameOverDialog(context.getString(R.string.white_wins));
        }
    }

    private void gameOverDialog(String text) {
        Intent intent = new Intent(context, GameOverDialogActivity.class);
        intent.putExtra(context.getString(R.string.winner_text), text);
        intent.putExtra(context.getString(R.string.level_selected), level);
        intent.putExtra(context.getString(R.string.ai_option), aiOption);
        context.startActivity(intent);
    }



    private class AIThink extends AsyncTask<Integer, Void, int[]> {
        @Override
        protected int[] doInBackground(Integer... integers) {
            // preparing information for PlayerAI
            Piece[][] newBoard = new Piece[board.length][];
            for (int i = 0; i < board.length; i++)
                newBoard[i] = board[i].clone();
            GameState state = new GameState(newBoard, wPieceCount.clone(), bPieceCount.clone(), integers[0]);
            // execute playerAI
            return PlayerAI.getMove(state);
        }

        @Override
        protected void onPostExecute(int[] move) {
            super.onPostExecute(move);
            makeMove(move[0], move[1], move[2], move[3]);
            gameView.invalidate();  // update canvas
        }
    }

}
