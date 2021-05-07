package edu.umb.cs.notchess;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;
    public final int[] wPieceCount;     // {protectees, protectors}
    public final int[] bPieceCount;
    public final int playerToMove;      // 1 -> White, -1 -> Black
    public boolean gameOver;
    public double points;

    GameState(Piece[][] board, int[] wPieceCount, int[] bPieceCount, int playerToMove) {
        this.board = board;
        this.wPieceCount = wPieceCount;
        this.bPieceCount = bPieceCount;
        this.playerToMove = playerToMove;
        gameOver = false;
        points = 0;
    }
}
