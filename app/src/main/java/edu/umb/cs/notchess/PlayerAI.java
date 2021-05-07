package edu.umb.cs.notchess;

import android.util.Log;
import java.util.ArrayList;

public class PlayerAI {
    static int timeLimit = 3;   // Limit for computer players' thinking time (seconds)

    static double victoryPoints = 100;
    static double pointMultiplier = 10;
    static double pieceValue = 20;
    static double victoryScoreThresh = 900;

    static int minLookAhead = 2;
    static int maxLookAhead = 6;

//    private int assignedPlayer;
    private final int boardWidth = 7;
    private final int boardHeight = 6;

    private long startTime;

//    PlayerAI(int assignedPlayer, Piece[][] board) {
//        this.assignedPlayer = assignedPlayer;
//        boardWidth = board[0].length;
//        boardHeight = board.length;
//    }

    // Compute list of legal moves for a given GameState and the player moving next
    private ArrayList<int[]> getMoveOptions(GameState state) {
        ArrayList<int[]> moves = new ArrayList<>();
        Piece tmpPiece;
        int pieceCount = 0;
        for (int xStart = 0; xStart < boardWidth; xStart++) {
            for (int yStart = 0; yStart < boardHeight; yStart++) {
                tmpPiece = state.board[yStart][xStart];
                if (tmpPiece != null && tmpPiece.belongsTo(state.playerToMove)) {
                    pieceCount++;
                    moves.addAll(tmpPiece.getMoveOptions(state.board, xStart, yStart));
                }
            }
        }
//        Log.d("AI", String.format("getMoveOptions(): p2move %d, pieceCount %d, movesSize %d",
//                state.playerToMove, pieceCount, moves.size()));
        return moves;
    }

    // For a given GameState and move to be executed, return the GameState that results from the move
    private GameState makeMove(GameState state, int[] move) {
        int xStart = move[0];
        int yStart = move[1];
        int xEnd = move[2];
        int yEnd = move[3];
        Piece[][] newBoard = new Piece[state.board.length][];
        for (int i = 0; i < state.board.length; i++)
            newBoard[i] = state.board[i].clone();
        GameState newState = new GameState(newBoard, state.wPieceCount.clone(), state.bPieceCount.clone(), -state.playerToMove);
        newState.board[yEnd][xEnd] = newState.board[yStart][xStart];
        newState.board[yStart][xStart] = null;

        Piece kicked = state.board[yEnd][xEnd];
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

        if (newState.wPieceCount[0] == 0 || newState.wPieceCount[1] == 0
                || newState.bPieceCount[0] == 0 || newState.bPieceCount[1] == 0) {
            newState.gameOver = true;
            newState.points = state.playerToMove * victoryPoints;
        }

//        Log.d("AI", String.format("makeMove(): newP2move %d, gameOver %b",
//                newState.playerToMove, newState.gameOver));
        return newState;
    }

    // Return the evaluation score for a given GameState; higher score indicates a better situation for Player MAX(1).
    private double getScore(GameState state) {
        double score = pointMultiplier * state.points;

//        Piece[] maxTargetPieces = {Piece.B_King, Piece.B_Heart};
//        Piece[] minTargetPieces = {Piece.W_King, Piece.W_Heart};
//        ArrayList<int[]> maxTargetLocations = new ArrayList<>();
//        ArrayList<int[]> minTargetLocations = new ArrayList<>();
//
//        for (int x = 0; x < boardWidth; x++) {
//            for (int y = 0; y < boardHeight; y++) {
//                for (Piece target : maxTargetPieces) {
//                    if (state.board[y][x] == target)
//                        maxTargetLocations.add(new int[] {x, y});
//                }
//                for (Piece target : minTargetPieces) {
//                    if (state.board[y][x] == target)
//                        minTargetLocations.add(new int[] {x, y});
//                }
//            }
//        }

        for (int x = 0; x < boardWidth; x++) {
            for (int y = 0; y < boardHeight; y++) {
                if (state.board[y][x] != null) {
                    if (state.board[y][x].belongsTo(-1)) {
                        int appleDist = (boardWidth - 1) - x + (boardHeight - 1) - y;
                        score += pieceValue - appleDist;
                    } else if (state.board[y][x].belongsTo(1)) {
                        int appleDist = x + y;
                        score -= pieceValue - appleDist;
                    }
                }
            }
        }

//        Log.d("AI", String.format("getScore(): p2move %d, getScore %.1f",
//                state.playerToMove, score));
        return score;
    }

    // Check whether time limit has been reached
    private boolean timeOut() {
        long duration = System.currentTimeMillis() - startTime;
//        Log.d("AI", String.format("timeOut(): duration %d", duration));
        return duration / 1000 >= timeLimit;
    }

    // Use the MinMax algorithm to look ahead <depthRemaining> moves and return the resulting score
    private double lookAhead(GameState state, int depthRemaining) {
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

//        Log.d("AI", String.format("lookAhead(): p2move %d, depthRemain %d,  bestScore %.1f",
//                state.playerToMove, depthRemaining, bestScore));
        return bestScore;
    }

    // Compute the next move to be played; keep updating <favoredMove> until computation finished or time limit reached
    public int[] getMove(final GameState state) {
        startTime = System.currentTimeMillis();
        ArrayList<int[]> moveList = getMoveOptions(state);
        int[] favoredMove = moveList.get(0);
        double favoredMoveScore = -9e9 * state.playerToMove;

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
