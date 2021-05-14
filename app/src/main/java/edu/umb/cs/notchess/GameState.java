package edu.umb.cs.notchess;

import androidx.annotation.NonNull;

import static edu.umb.cs.notchess.Piece.B_Bishop;
import static edu.umb.cs.notchess.Piece.B_Knight;
import static edu.umb.cs.notchess.Piece.B_Pawn;
import static edu.umb.cs.notchess.Piece.B_Queen;
import static edu.umb.cs.notchess.Piece.B_Rook;
import static edu.umb.cs.notchess.Piece.W_Bishop;
import static edu.umb.cs.notchess.Piece.W_Knight;
import static edu.umb.cs.notchess.Piece.W_Pawn;
import static edu.umb.cs.notchess.Piece.W_Queen;
import static edu.umb.cs.notchess.Piece.W_Rook;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;       // representation of the chess board
    public final boolean[][] isMoved;   // whether a piece has visited or move away from a block
    public final int[] wPieceCount;     // for determining whether the game is over ...
    public final int[] bPieceCount;     // ... {Hearts, Kings, others}
    public int[] lastMove;              // {xStart, yStart, xEnd, yEnd}
    public int playerToMove;            // 1 -> White, -1 -> Black
    public int winner;                  // 1 -> White, -1 -> Black, 0 -> game not over
    public int moveCount;               // number of moves made
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
        moveCount = 0;
        points = 0;
    }

    public boolean isGameOver() {
        return winner != 0;
    }

    public int checkWinner() {
        if (winner != 0)
            return winner;
        if (wPieceCount[1] == 0 && (wPieceCount[0] == 0 || wPieceCount[2] == 0)) {
            winner = -1;
        } else if (bPieceCount[1] == 0 && (bPieceCount[0] == 0 || bPieceCount[2] == 0)) {
            winner = 1;
        }
        return winner;
    }

    @NonNull
    public GameState clone() {
        return new GameState(board, isMoved, wPieceCount, bPieceCount, lastMove, playerToMove);
    }

    public void makeMove(int xStart, int yStart, int xEnd, int yEnd, int promote) {
        Piece toMove = board[yStart][xStart];
        Piece kicked = board[yEnd][xEnd];

        lastMove = new int[]{xStart, yStart, xEnd, yEnd};

        // make the move
        board[yEnd][xEnd] = toMove;
        board[yStart][xStart] = null;

        isMoved[yStart][xStart] = true;
        isMoved[yEnd][xEnd] = true;

        // special move: en passant (in passing)
        if (kicked == null && toMove.isPawn()) {
            int[] pawnsForward = toMove.getPawnsForward();
            int xBehind = xEnd - pawnsForward[0];
            int yBehind = yEnd - pawnsForward[1];
            Piece pieceBehind = board[yBehind][xBehind];
            if (pieceBehind != null && pieceBehind.isPawn()) {      // if it was en passant move
                kicked = pieceBehind;
                board[yBehind][xBehind] = null;
            }
        }

        // special move: promotion
        // promote: None(-1), Queen(0), Bishop(1), Knight(2), Rook(3)
        if (promote >= 0 && promote <= 3 && toMove.isPawn()) {
            boolean w = toMove.isBelongingTo(1);
            switch(promote) {
                case 0:
                    toMove = w ? W_Queen : B_Queen;
                    break;
                case 1:
                    toMove = w ? W_Bishop : B_Bishop;
                    break;
                case 2:
                    toMove = w ? W_Knight : B_Knight;
                    break;
                case 3:
                    toMove = w ? W_Rook : B_Rook;
            }
            board[yEnd][xEnd] = toMove;
        }

        // count the number of pieces left
        if (kicked != null) {
            int idx = kicked.isHeart() ? 0 : kicked.isKing() ? 1 : 2;
            if (kicked.isBelongingTo(1)) wPieceCount[idx] -= 1;
            else bPieceCount[idx] -= 1;
        }

        playerToMove = -playerToMove;   // opponent is the next player to move
        moveCount += 1;
    }
}
