package edu.umb.cs.notchess;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;
    public final boolean[][] isMoved;
    public final int[] wPieceCount;     // {protectees, protectors}
    public final int[] bPieceCount;
    public final int playerToMove;      // 1 -> White, -1 -> Black
    public boolean gameOver;
    public double points;

    GameState(Piece[][] board, boolean[][] isMoved, int[] wPieceCount, int[] bPieceCount,
              int playerToMove) {
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
        this.playerToMove = playerToMove;
        gameOver = false;
        points = 0;
    }

//    public String toString() {
//        StringBuilder buffer = new StringBuilder();
//        buffer.append("board:\n");
//        for (Piece[] row : board) {
//            for (Piece piece : row)
//                buffer.append(String.format("%3d", (piece == null) ? 0 : piece.value));
//            buffer.append("\n");
//        }
//        buffer.append("\n");
//
//        buffer.append(String.format("wPieceCount[]: {%d, %d}\n", wPieceCount[0], wPieceCount[1]));
//        buffer.append(String.format("bPieceCount[]: {%d, %d}\n", bPieceCount[0], bPieceCount[1]));
//        buffer.append(String.format("playerToMove: %d\n", playerToMove));
//        buffer.append(String.format("gameOver: %b\n", gameOver));
//        buffer.append(String.format("points: %.1f\n", points));
//        return buffer.toString();
//    }
}
