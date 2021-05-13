package edu.umb.cs.notchess;

import static edu.umb.cs.notchess.Piece.B_Pawn;
import static edu.umb.cs.notchess.Piece.W_Pawn;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;       // representation of the chess board
    public final boolean[][] isMoved;   // whether a piece has visited or move away from a block
    public final int[] wPieceCount;     // for determining whether the game is over ...
    public final int[] bPieceCount;     // ... {protectees, protectors}, protectees: Kings/Hearts
    public int[] lastMove;              // {xStart, yStart, xEnd, yEnd}
    public int playerToMove;            // 1 -> White, -1 -> Black
    public int winner;                  // 1 -> White, -1 -> Black, 0 -> game not over
    public double points;               // used for AI's MinMax algorithm

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
        winner = 0;
        points = 0;
    }

    public boolean isGameOver() {
        return winner != 0;
    }

    public GameState clone() {
        return new GameState(board, isMoved, wPieceCount, bPieceCount, lastMove, playerToMove);
    }

    public void makeMove(int xStart, int yStart, int xEnd, int yEnd) {
        Piece toMove = board[yStart][xStart];
        Piece kicked = board[yEnd][xEnd];

        lastMove = new int[]{xStart, yStart, xEnd, yEnd};

        // make the move
        board[yEnd][xEnd] = toMove;
        board[yStart][xStart] = null;

        isMoved[yStart][xStart] = true;
        isMoved[yEnd][xEnd] = true;

        // special move: en passant (in passing)
        if (kicked == null && (toMove == W_Pawn || toMove == B_Pawn)) {
            int[] pawnsForward = toMove.getPawnsForward();
            int xBehind = xEnd - pawnsForward[0];
            int yBehind = yEnd - pawnsForward[1];
            Piece pieceBehind = board[yBehind][xBehind];
            if (pieceBehind == W_Pawn || pieceBehind == B_Pawn) {   // if it was en passant move
                kicked = pieceBehind;
                board[yBehind][xBehind] = null;
            }
        }

        // count the number of pieces left
        if (kicked != null) {
            if (kicked.value > 0) wPieceCount[kicked.isProtectee() ? 0 : 1] -= 1;
            else bPieceCount[kicked.isProtectee() ? 0 : 1] -= 1;
        }

        playerToMove = -playerToMove;   // opponent is the next player to move
    }
}
