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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import static edu.umb.cs.notchess.Piece.*;

public class Chessboard {
    private final Context context;          // context of GameView
    private final int level;                // index of Levels.boards
    private final int aiOption;             // spinner items: 0 -> disable, 1 -> Black, 2 -> White
    private final View gameView;            // the GameView object that draws the chess board
    private final TextView indicatorView;   // for showing who is the player to move now

    private final GameState state;
    private final int width;                // dimension of the chess board
    private final int height;

    /* for drawing */

    private final Paint whitePaint;         // paints for the board
    private final Paint blackPaint;
    private final Paint selectedPaint;      // the paint for highlights

    private int blockSize;                  // width/height of a block
    private Rect block;                     // used for drawing each block of the board
    private final boolean rotatePieces;     // rotate pieces option from the setting

    private final Bitmap moveBitmap;        // for showing move options
    private final Bitmap kickBitmap;        // for showing move options to kick a piece
    private final Rect spriteRect;          // for drawing moveBitmap and kickBitmap

    /* for making moves */

    private int selectedX = -1;             // piece coordinate currently selected by human player
    private int selectedY = -1;
    private ArrayList<int[]> moveList;      // move options of currently selected piece

    private int ai = 0;                     // 1 -> White, -1 -> Black, 0 -> disable
    private int moveCount = 0;              // for indicatorView to display

    public Chessboard(Context context, int level, int aiOption, View gameView, TextView indicatorView) {
        this.context = context;
        this.level = level;
        this.aiOption = aiOption;
        this.gameView = gameView;
        this.indicatorView = indicatorView;

        // load the chess board
        Piece[][] board = Levels.boards[level]; // get board by index

        this.width = board[0].length;           // get dimension
        this.height = board.length;

        boolean[][] isMoved = new boolean[height][width];   // default values are false

        // count the number of pieces for determining whether game is over
        int[] wPieceCount = new int[]{0, 0};     // {protectees, protectors}
        int[] bPieceCount = new int[]{0, 0};
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece != null) {
                    if (piece.value > 0)    // a White piece
                        wPieceCount[piece.isProtectee() ? 0 : 1] += 1;
                    else                    // a Black piece
                        bPieceCount[piece.isProtectee() ? 0 : 1] += 1;
                }
            }
        }

        state = new GameState(board, isMoved, wPieceCount, bPieceCount, null,
                1);

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

        // load rotate pieces setting
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
        if (ai == state.playerToMove)
            new AIThink().execute(ai);
    }

    /*============================================================================================*/
    /* drawing */

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
                        || state.lastMove != null && (state.lastMove[0] == x && state.lastMove[1] == y
                        || state.lastMove[2] == x && state.lastMove[3] == y))
                    canvas.drawRect(block, selectedPaint);
                // draw a piece
                Piece piece = state.board[y][x];
                if (piece != null)
                    piece.draw(canvas, block, state.playerToMove == -1 && rotatePieces && ai == 0);
            }
        }
        // draw move options
        if (moveList != null) {
            for (int[] move : moveList) {
                int xEnd = move[2];
                int yEnd = move[3];
                block.offsetTo(blockSize * xEnd, blockSize * yEnd);
                canvas.drawBitmap(state.board[yEnd][xEnd] == null ? moveBitmap : kickBitmap,
                        spriteRect, block, null);
            }
        }
    }

    /*============================================================================================*/
    /* making moves */

    // handles player action of selecting and making moves
    public void select(float xPix, float yPix) {
        if (state.isGameOver()) checkGameState();  // game is already over

        int x = (int) xPix / blockSize;     // clicked on which block
        int y = (int) yPix / blockSize;

        if (x >= width || y >= height) return;  // clicking outside of the board

        Piece piece = state.board[y][x];
        if (selectedX == x && selectedY == y) { // deselect
            deselect();
        } else if (piece != null && piece.belongsTo(state.playerToMove) && state.playerToMove != ai) {
            selectedX = x;  // player selects a piece
            selectedY = y;
            moveList = piece.getMoveOptions(state.board, x, y, state.isMoved[y][x], state.lastMove);
        } else if (validMove(x, y)) {    // player makes a move
            makeMove(selectedX, selectedY, x, y);
            if (!state.isGameOver() && state.playerToMove == ai)
                new AIThink().execute();
        }
    }

    private boolean validMove(int xEnd, int yEnd) {
        if (moveList == null || moveList.size() == 0)
            return false;
        for (int[] move : moveList) {
            if (xEnd == move[2] && yEnd == move[3])
                return true;
        }
        return false;
    }

    private void makeMove(int xStart, int yStart, int xEnd, int yEnd) {
        state.makeMove(xStart, yStart, xEnd, yEnd);
        moveCount += 1;
        deselect();
        updateMoveIndicator();              // update text of telling who's move next
        checkGameState();                   // check whether game is over
    }

    private void deselect() {
        selectedX = -1;
        moveList = null;
    }

    /*============================================================================================*/
    /* UI */

    private void updateMoveIndicator() {
        int id;
        if (state.playerToMove == 1)
            id = ai == 1 ? R.string.white_is_thinking : R.string.whites_move;
        else
            id = ai == -1 ? R.string.black_is_thinking : R.string.blacks_move;
        String text = String.format(context.getResources().getString(id), moveCount);
        indicatorView.setText(text);
    }

    // check whether game is over
    private void checkGameState() {
        // check whether game is already over
        if (state.winner == -1)
            gameOverDialog(context.getString(R.string.black_wins));
        else if (state.winner == 1)
            gameOverDialog(context.getString(R.string.white_wins));

        if (state.wPieceCount[0] == 0 || state.wPieceCount[1] == 0) {
            state.winner = -1;
            gameOverDialog(context.getString(R.string.black_wins));
        } else if (state.bPieceCount[0] == 0 || state.bPieceCount[1] == 0) {
            state.winner = 1;
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

    /*============================================================================================*/
    /* AI */

    private class AIThink extends AsyncTask<Integer, Void, int[]> {
        @Override
        protected int[] doInBackground(Integer... integers) {
            // execute playerAI
            return PlayerAI.getMove(state.clone());
        }

        @Override
        protected void onPostExecute(int[] move) {
            super.onPostExecute(move);
            makeMove(move[0], move[1], move[2], move[3]);
            gameView.invalidate();  // update canvas
        }
    }

}
