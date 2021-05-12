package edu.umb.cs.notchess;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;
    public final boolean[][] isMoved;
    public final int[] wPieceCount;     // {protectees, protectors}
    public final int[] bPieceCount;
    public final int[] lastMove;
    public final int playerToMove;      // 1 -> White, -1 -> Black
    public boolean gameOver;
    public double points;

    GameState(Piece[][] board, boolean[][] isMoved, int[] wPieceCount, int[] bPieceCount,
              int[] lastMove, int playerToMove) {
        Piece[][] newBoard = new Piece[board.length][];
        for (int i = 0; i < board.length; i++)
            newBoard[i] = board[i].clone();

        boolean[][] newIsMoved = new boolean[isMoved.length][];
        for (int i = 0; i < isMoved.length; i++)
            newIsMoved[i] = isMoved[i].clone();

        this.board = newBoard;
        this.isMoved = newIsMoved;
        this.wPieceCount = wPieceCount.clone();
        this.bPieceCount = bPieceCount.clone();
        this.lastMove = lastMove == null ? null : lastMove.clone();
        this.playerToMove = playerToMove;
        gameOver = false;
        points = 0;
    }
}
