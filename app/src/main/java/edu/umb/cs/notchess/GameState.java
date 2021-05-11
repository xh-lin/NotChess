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
