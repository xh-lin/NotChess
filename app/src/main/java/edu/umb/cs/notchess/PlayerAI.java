package edu.umb.cs.notchess;

import android.util.Log;
import java.util.ArrayList;

public class PlayerAI {
    static long startTime;
    static int timeLimit = 3000;   // Limit for computer player's thinking time (milliseconds)

    // for getScore() calculation
    static double victoryPoints = 100;
    static double pointMultiplier = 10;
    static double victoryScoreThresh = 900;

    // depth range of iterative deepening
    static int minLookAhead = 2;
    static int maxLookAhead = 6;

    static final int boardWidth = 7;
    static final int boardHeight = 6;

    static ArrayList<int[]> wProtectees = new ArrayList<>();       // location of Kings and Hearts
    static ArrayList<int[]> bProtectees = new ArrayList<>();

    // Compute list of legal moves for a given GameState and the player moving next
    private static ArrayList<int[]> getMoveOptions(GameState state) {
        ArrayList<int[]> moves = new ArrayList<>();
        Piece pieceToMove;
        for (int xStart = 0; xStart < boardWidth; xStart++) {
            for (int yStart = 0; yStart < boardHeight; yStart++) {
                pieceToMove = state.board[yStart][xStart];
                if (pieceToMove != null && pieceToMove.belongsTo(state.playerToMove))
                    moves.addAll(pieceToMove.getMoveOptions(state.board, xStart, yStart));
            }
        }

        return moves;
    }

    // For a given GameState and move to be executed, return the GameState that results from the move
    private static GameState makeMove(GameState state, int[] move) {
        int xStart = move[0];
        int yStart = move[1];
        int xEnd = move[2];
        int yEnd = move[3];

        Piece[][] newBoard = new Piece[state.board.length][];           // clone the state
        for (int i = 0; i < state.board.length; i++)
            newBoard[i] = state.board[i].clone();
        GameState newState = new GameState(newBoard, state.wPieceCount.clone(),
                state.bPieceCount.clone(), -state.playerToMove);

        newState.board[yEnd][xEnd] = newState.board[yStart][xStart];    // move the piece
        newState.board[yStart][xStart] = null;

        Piece kicked = state.board[yEnd][xEnd];                         // count remaining pieces
        if (kicked != null) {
            if (kicked.value > 0) {
                if (kicked == Piece.W_King || kicked == Piece.W_Heart)
                    newState.wPieceCount[0] -= 1;
                else
                    newState.wPieceCount[1] -= 1;
            } else {
                if (kicked == Piece.B_King || kicked == Piece.B_Heart)
                    newState.bPieceCount[0] -= 1;
                else
                    newState.bPieceCount[1] -= 1;
            }
        }

        if (newState.wPieceCount[0] == 0 || newState.wPieceCount[1] == 0    // check whether game over
                || newState.bPieceCount[0] == 0 || newState.bPieceCount[1] == 0) {
            newState.gameOver = true;
            newState.points = state.playerToMove * victoryPoints;
        }

        return newState;
    }

    private static void updateProtecteeLocations(Piece[][] board) {
        boolean protecteesMoved = false;
        Piece piece;
        for (int[] location : wProtectees) {
            piece = board[location[1]][location[0]];
            if (piece == null || !piece.isProtectee()) {
                protecteesMoved = true;
                break;
            }
        }
        if (!protecteesMoved) {
            for (int[] location : bProtectees) {
                piece = board[location[1]][location[0]];
                if (piece == null || !piece.isProtectee()) {
                    protecteesMoved = true;
                    break;
                }
            }
        }
        if (!protecteesMoved)
            return;

        wProtectees.clear();
        bProtectees.clear();
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                piece = board[y][x];
                if (piece != null && piece.isProtectee())
                    (piece.belongsTo(1) ? wProtectees : bProtectees).add(new int[]{x, y});
            }
        }
    }

    // Return the evaluation score for a given GameState; higher score indicates a better situation for Player MAX(1).
    private static double getScore(GameState state) {
        double score = pointMultiplier * state.points;

        if (state.gameOver)
            return score;

        updateProtecteeLocations(state.board);

        Piece piece;
        ArrayList<int[]> targets;
        int tmpDist;
        int minDist;
        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                piece = state.board[y][x];
                if (piece != null) {
                    minDist = Integer.MAX_VALUE;
                    targets = piece.belongsTo(1) ? bProtectees : wProtectees;

                    for (int[] location : targets) {
                        tmpDist = Math.abs(location[0] - x) + Math.abs(location[1] - y);
                        if (tmpDist < minDist)
                            minDist = tmpDist;
                    }

                    if (piece.belongsTo(1))
                        score -= minDist;
                    else
                        score += minDist;
                }
            }
        }

        return score;
    }

    // Check whether time limit has been reached
    private static boolean timeOut() {
        long duration = System.currentTimeMillis() - startTime;
        return duration >= timeLimit;
    }

    // Use the MinMax algorithm to look ahead <depthRemaining> moves and return the resulting score
    private static double lookAhead(GameState state, int depthRemaining) {
        if (depthRemaining == 0 || state.gameOver)
            return getScore(state);

        if (timeOut())
            return 0;

        double bestScore = -9e9 * state.playerToMove;

        GameState projectedState;
        double score;

        for (int[] move : getMoveOptions(state)) {
            projectedState = makeMove(state, move);
            score = lookAhead(projectedState, depthRemaining - 1);

            if ((state.playerToMove == 1 && score > bestScore)
                    || (state.playerToMove == -1 && score < bestScore))
                bestScore = score;

        }

        return bestScore;
    }

    // Compute the next move to be played; keep updating <favoredMove> until computation finished or time limit reached
    static int[] getMove(final GameState state) {
        startTime = System.currentTimeMillis();                 // Remember computation start time
        ArrayList<int[]> moveList = getMoveOptions(state);      // Get the list of possible moves
        int[] favoredMove = moveList.get(0);                    // Choose first in case run out of time
        double favoredMoveScore = -9e9 * state.playerToMove;    // Use it to remember the favored move

        int[] currBestMove;
        double currBestScore;
        GameState projectedState;
        double score;

        // Iterative deepening loop
        for (int lookAheadDepth = minLookAhead; lookAheadDepth <= maxLookAhead; lookAheadDepth++) {
            currBestMove = null;
            currBestScore = -9e9 * state.playerToMove;

            // Try every possible next move, evaluate it using MinMax, and pick the one with best score
            for (int[] move : moveList) {
                projectedState = makeMove(state, move);
                score = lookAhead(projectedState, lookAheadDepth - 1);

                if (timeOut())
                    break;

                if ((state.playerToMove == 1 && score > currBestScore)
                        || (state.playerToMove == -1 && score < currBestScore)) {
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

        return favoredMove;
    }
}
