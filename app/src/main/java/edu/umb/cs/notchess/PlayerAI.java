package edu.umb.cs.notchess;

import android.util.Log;
import androidx.core.math.MathUtils;
import java.util.ArrayList;
import java.util.Collections;
import static edu.umb.cs.notchess.Piece.*;

public class PlayerAI {
    // for getScore() calculation
    static double victoryPoints = 1000;
    static double victoryScoreThresh = victoryPoints - 1;

    // depth range of iterative deepening
    static int minLookAhead = 2;
    static int maxLookAhead = 20;

    // chess board dimension
    static int boardWidth;
    static int boardHeight;

    // location of Kings and Hearts, used for score calculation
    static ArrayList<int[]> wProtectees;
    static ArrayList<int[]> bProtectees;

    static int mPlayer;             // computer player is White(1) or Black(0)
    static long startTime;          // for remembering when the program has started
    static int timeLimit = 3000;    // Think time limit of computer player (milliseconds)

    /*============================================================================================*/
    /* helper functions */

    // whether Kings have moved
    private static boolean needUpdateProtecteeLocations(Piece[][] board, ArrayList<int[]> protectees) {
        if (protectees.size() == 0)             // first time calling
            return true;
        Piece piece;
        for (int[] location : protectees) {     // pieces moved
            piece = board[location[1]][location[0]];
            if (piece == null || !(piece.isHeart() || piece.isKing()))
                return true;
        }
        return false;
    }

    // get new coordinates of Kings
    private static void updateProtecteeLocations(Piece[][] board) {
        if (needUpdateProtecteeLocations(board, wProtectees)
                || needUpdateProtecteeLocations(board, bProtectees)) {
            Piece piece;
            wProtectees.clear();
            bProtectees.clear();
            for (int x = 0; x < boardWidth; x++) {
                for (int y = 0; y < boardHeight; y++) {
                    piece = board[y][x];
                    if (piece != null && (piece.isHeart() || piece.isKing()))
                        (piece.isBelongingTo(1) ? wProtectees : bProtectees).add(new int[]{x, y});
                }
            }
        }
    }

    // get the distance between a piece and the closest King/Heart
    private static int moveDistFromTargets(int player, int[] move) {
        ArrayList<int[]> targets = (player == 1) ? bProtectees : wProtectees;
        int xEnd = move[2];
        int yEnd = move[3];
        int minDist = Integer.MAX_VALUE;
        int tmpDist;
        for (int[] target : targets) {   // Manhattan distance
            tmpDist = Math.abs(target[0] - xEnd) + Math.abs(target[1] - yEnd);
            if (tmpDist < minDist)
                minDist = tmpDist;
        }
        return minDist;
    }

    /*============================================================================================*/
    /* algorithm to get a move */

    // Compute list of legal moves for a given GameState and the player moving next
    private static ArrayList<int[]> getMoveOptions(GameState state) {
        ArrayList<int[]> moves = new ArrayList<>();

        for (int xStart = 0; xStart < boardWidth; xStart++) {
            for (int yStart = 0; yStart < boardHeight; yStart++) {
                Piece pieceToMove = state.board[yStart][xStart];
                if (pieceToMove != null && pieceToMove.isBelongingTo(state.playerToMove))
                    moves.addAll(pieceToMove.getMoveOptions(state.board, xStart, yStart,
                            state.isMoved[yStart][xStart], state.lastMove));
            }
        }

        // moves that end up closer to the targets get computed first
        updateProtecteeLocations(state.board);
        Collections.sort(moves, (move1, move2) ->
                moveDistFromTargets(state.playerToMove, move1)
                        - moveDistFromTargets(state.playerToMove, move2));

        return moves;
    }

    // For a given GameState and move to be executed, return the GameState that results from the move
    private static GameState makeMove(GameState state, int[] move) {
        GameState newState = state.clone();
        newState.makeMove(move[0], move[1], move[2], move[3], -1);

        if (newState.checkWinner() != 0)
            newState.points = state.playerToMove * victoryPoints;

        return newState;
    }


    // Return the evaluation score for a given GameState; higher score indicates a better situation for Player MAX(1).
    private static double getScore(GameState state) {
        double score = state.points;

        if (state.isGameOver())
            return score;

        Piece piece;
        ArrayList<int[]> targets;
        int tmpDist;
        int minDist;

        updateProtecteeLocations(state.board);

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                piece = state.board[y][x];
                if (piece != null) {
                    minDist = Integer.MAX_VALUE;
                    targets = piece.isBelongingTo(1) ? bProtectees : wProtectees;

                    for (int[] location : targets) {
                        tmpDist = Math.abs(location[0] - x) + Math.abs(location[1] - y);
                        if (tmpDist < minDist)
                            minDist = tmpDist;
                    }

                    if (piece.isBelongingTo(1))
                        score -= minDist;
                    else
                        score += minDist;
                }
            }
        }

        return MathUtils.clamp(score, -victoryScoreThresh+1, victoryScoreThresh-1);
    }

    // Check whether time limit has been reached
    private static boolean timeOut() {
        long duration = System.currentTimeMillis() - startTime;
        return duration >= timeLimit;
    }

    // Use the MinMax algorithm to look ahead <depthRemaining> moves and return the resulting score
    private static double lookAhead(GameState state, int depthRemaining, double alphaBeta) {
        if (depthRemaining == 0 || state.isGameOver())
            return getScore(state);

        if (timeOut())
            return 9e9 * state.playerToMove;    // make ancestor ignore this score

        double bestScore = -9e9 * state.playerToMove;

        // Try out every possible move and score the resulting state
        for (int[] move : getMoveOptions(state)) {
            GameState projectedState = makeMove(state, move);
            double score = lookAhead(projectedState, depthRemaining - 1, bestScore);

            if ((state.playerToMove == 1 && score > bestScore)
                    || (state.playerToMove == -1 && score < bestScore))
                bestScore = score;  // Update bestScore if we have a new highest/lowest score for MAX/MIN

            if ((state.playerToMove == 1 && bestScore > alphaBeta)
                    || (state.playerToMove == -1 && bestScore < alphaBeta))
                break;
        }

        return bestScore;
    }

    // Compute the next move to be played; keep updating <favoredMove> until computation finished or time limit reached
    static int[] getMove(final GameState state) {
        startTime = System.currentTimeMillis();                 // Remember computation start time

        mPlayer = state.playerToMove;
        boardWidth = state.board[0].length;
        boardHeight = state.board.length;

        wProtectees = new ArrayList<>();
        bProtectees = new ArrayList<>();

        ArrayList<int[]> moveList = getMoveOptions(state);      // Get the list of possible moves
        int[] favoredMove = moveList.get(0);                    // Choose first in case run out of time
        double favoredMoveScore = -9e9 * mPlayer;    // Use it to remember the favored move

        int[] incompleteMove = favoredMove;
        double incompleteMoveScore = favoredMoveScore;

        // Iterative deepening loop
        for (int lookAheadDepth = minLookAhead; lookAheadDepth <= maxLookAhead; lookAheadDepth++) {
            int[] currBestMove = null;
            double currBestScore = -9e9 * mPlayer;

            // Try every possible next move, evaluate it using MinMax, and pick the one with best score
            for (int[] move : moveList) {
                GameState projectedState = makeMove(state, move);
                double score = lookAhead(projectedState, lookAheadDepth - 1, currBestScore);
                incompleteMove = move;
                incompleteMoveScore = score;

                if (timeOut())
                    break;

                if ((mPlayer == 1 && score > currBestScore)
                        || (mPlayer == -1 && score < currBestScore)) {
                    currBestMove = move;
                    currBestScore = score;
                }
            }

            if (!timeOut()) {
                favoredMove = currBestMove;
                favoredMoveScore = currBestScore;

                long duration = System.currentTimeMillis() - startTime;
                Log.i("AI", String.format("-- PlayerAI: Depth %d finished at %d ms, favored move (%d,%d)->(%d,%d), score = %.1f",
                        lookAheadDepth, duration, favoredMove[0], favoredMove[1], favoredMove[2], favoredMove[3], favoredMoveScore));
            } else {
                Log.i("AI", "-- PlayerAI: Timeout!");
            }

            if (timeOut() || Math.abs(favoredMoveScore) >= victoryScoreThresh)
                break;
        }

        return (favoredMoveScore * mPlayer < 0 && incompleteMoveScore * mPlayer > 0) ? incompleteMove : favoredMove;
    }
}
