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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class Chessboard {
    private final Context context;          // context of GameView
    private final View gameView;            // the GameView object that draws the chess board
    private final TextView indicatorView;   // for showing who is the player to move now
    
    private GameState state;
    private final GameState initState;       // initial game state for reset
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

    private int[] promoteMove;
    private final View wPromotionView;          // for promotion
    private final View bPromotionView;

    private final View gameOverView;

    public Chessboard(Context context, View gameView) {
        LevelActivity levelActivity = ((LevelActivity) context);
        int level = levelActivity.level;        // index of Levels.boards
        int aiOption = levelActivity.aiOption;  // spinner items: 0 -> disable, 1 -> Black, 2 -> White

        this.context = context;
        this.gameView = gameView;
        indicatorView = levelActivity.findViewById(R.id.indicatorView);

        // load View objects for pawn promotion
        LayoutInflater layoutInflater;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ConstraintLayout mainLayout = levelActivity.findViewById(R.id.constrainLayout);

        wPromotionView = layoutInflater.inflate(R.layout.window_promotion_white, null);
        mainLayout.addView(wPromotionView, mainLayout.getLayoutParams());
        wPromotionView.setVisibility(View.INVISIBLE);

        bPromotionView = layoutInflater.inflate(R.layout.window_promotion_black, null);
        mainLayout.addView(bPromotionView, mainLayout.getLayoutParams());
        bPromotionView.setVisibility(View.INVISIBLE);

        // load View object for the game over dialog
        gameOverView = layoutInflater.inflate(R.layout.window_game_over, null);
        mainLayout.addView(gameOverView, mainLayout.getLayoutParams());
        gameOverView.setVisibility(View.INVISIBLE);

        // preparing the game state
        Piece[][] board = Levels.boards[level]; // load the chess board by index

        width = board[0].length;           // get dimension
        height = board.length;

        boolean[][] isMoved = new boolean[height][width];   // default values are false

        // count the number of pieces for determining whether game is over
        int[] wPieceCount = new int[]{0, 0, 0};     // {Hearts, Kings, others}
        int[] bPieceCount = new int[]{0, 0, 0};
        for (Piece[] row : board) {
            for (Piece piece : row) {
                if (piece != null) {
                    int idx = piece.isHeart() ? 0 : piece.isKing() ? 1 : 2;
                    if (piece.isBelongingTo(1)) wPieceCount[idx] += 1;
                    else bPieceCount[idx] += 1;
                }
            }
        }

        state = new GameState(board, isMoved, wPieceCount, bPieceCount, null, 1);
        initState = state.clone();

        // playerAI option
        switch (aiOption) {
            case 1: // Black
                ai = -1;
                break;
            case 2: // White
                ai = 1;
        }

        // paints
        whitePaint = new Paint();
        whitePaint.setColor(ContextCompat.getColor(context, R.color.beige));
        blackPaint = new Paint();
        blackPaint.setColor(ContextCompat.getColor(context, R.color.brown));
        selectedPaint = new Paint();
        selectedPaint.setColor(ContextCompat.getColor(context, R.color.trans_yellow));

        // load rotate pieces setting
        rotatePieces = PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.rotate_pieces), true);

        // load drawables for move options drawing
        moveBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.move);
        kickBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.kick);
        spriteRect = new Rect(0, 0, moveBitmap.getWidth(), moveBitmap.getHeight());

        Piece.loadAssets(context.getResources());   // load drawables for Piece
        updateMoveIndicator();

        if (ai == state.playerToMove)
            new AIThink().execute(ai);      // execute playerAI if it is the first to move
    }

    /*============================================================================================*/
    /* drawing */

    // being called by GameView.onSizeChanged() to get the dimension of the View object
    public void resize(int w, int h) {
        int newBlockSize = Math.min(w/width, h/height);
        blockSize = newBlockSize;
        block = new Rect(0, 0, newBlockSize, newBlockSize);
    }

    private boolean isNeedRotate() {
        return rotatePieces && ai == 0 && state.playerToMove == -1;
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
                    piece.draw(canvas, block, isNeedRotate());
            }
        }

        // draw move options
        if (moveList != null) {
            for (int[] move : moveList) {
                if (move[4] <= 0) {    // avoid drawing promotion multiple times
                    int xEnd = move[2];
                    int yEnd = move[3];
                    block.offsetTo(blockSize * xEnd, blockSize * yEnd);
                    canvas.drawBitmap(state.board[yEnd][xEnd] == null ? moveBitmap : kickBitmap,
                            spriteRect, block, null);
                }
            }
        }
    }

    /*============================================================================================*/
    /* making moves */

    private void deselect() {
        selectedX = -1;
        moveList = null;
    }

    private void makeMove(int xStart, int yStart, int xEnd, int yEnd, int promote) {
        state.makeMove(xStart, yStart, xEnd, yEnd, promote);
        deselect();
        updateMoveIndicator();              // update text of telling who's move next
        checkGameState();                   // check whether game is over

        if (!state.isGameOver() && state.playerToMove == ai)
            new AIThink().execute();        // execute playerAI if it is the next to move
    }

    private boolean isValidMove(int xEnd, int yEnd) {
        if (moveList == null || moveList.size() == 0)
            return false;
        for (int[] move : moveList) {
            if (xEnd == move[2] && yEnd == move[3])
                return true;
        }
        return false;
    }

    public void makePromotionMove(int promote) {
        View promotionView = state.playerToMove == 1 ? wPromotionView : bPromotionView;
        promotionView.setVisibility(View.INVISIBLE);        // close promotion menu
        makeMove(promoteMove[0], promoteMove[1], promoteMove[2], promoteMove[3], promote);
        gameView.invalidate();
    }

    // handles player action of selecting and making moves
    public void select(float xPix, float yPix) {
        if (state.isGameOver()) checkGameState();  // game is already over

        int x = (int) xPix / blockSize;     // clicked on which block
        int y = (int) yPix / blockSize;

        if (x >= width || y >= height) return;  // clicking outside of the board

        Piece piece = state.board[y][x];
        if (selectedX == x && selectedY == y) { // deselect
            deselect();
        } else if (piece != null && piece.isBelongingTo(state.playerToMove) && state.playerToMove != ai) {
            selectedX = x;  // player selects a piece
            selectedY = y;
            moveList = piece.getMoveOptions(state.board, x, y, state.isMoved[y][x], state.lastMove);
        } else if (isValidMove(x, y)) {    // player makes a move
            Piece selectedPiece = state.board[selectedY][selectedX];
            if (selectedPiece.isPromotion(state.board, x, y)) {
                promoteMove = new int[]{selectedX, selectedY, x, y};    // save current move
                showPromotionView();                                    // display promotion menu
            } else {
                makeMove(selectedX, selectedY, x, y, -1);
            }
        }
    }

    /*============================================================================================*/
    /* UI */

    public void resetState() {
        state = initState.clone();
        state.playerToMove = 1;
        gameView.invalidate();
        updateMoveIndicator();

        if (ai == state.playerToMove)
            new AIThink().execute(ai);  // execute playerAI if it is the first to move
    }

    private void showGameOverDialog(String text) {
        gameOverView.bringToFront();
        gameOverView.setVisibility(View.VISIBLE);
    }

    // check whether game is over
    private void checkGameState() {
        switch (state.checkWinner()) {
            case 1:
                showGameOverDialog(context.getString(R.string.white_wins));
                break;
            case -1:
                showGameOverDialog(context.getString(R.string.black_wins));
        }
    }

    private void updateMoveIndicator() {
        int id;
        if (state.playerToMove == 1)
            id = ai == 1 ? R.string.white_is_thinking : R.string.whites_move;
        else
            id = ai == -1 ? R.string.black_is_thinking : R.string.blacks_move;
        String text = String.format(context.getResources().getString(id), state.moveCount);
        indicatorView.setText(text);
    }

    private void showPromotionView() {
        View promotionView = state.playerToMove == 1 ? wPromotionView : bPromotionView;
        promotionView.bringToFront();
        promotionView.setVisibility(View.VISIBLE);
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
            makeMove(move[0], move[1], move[2], move[3], move[4]);
            gameView.invalidate();  // update canvas
        }
    }

}
