package edu.umb.cs.notchess;

import android.util.Log;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static edu.umb.cs.notchess.Piece.*;

// for PlayerAI to do calculations
public class GameState {
    public final Piece[][] board;       // representation of the chess board
    public final int[] wPieceCount;     // for determining whether the game is over ...
    public final int[] bPieceCount;     // ... {Hearts, Kings, others}
    public final boolean[][] isMoved;   // whether a piece has visited or move away from a block
    public int[] lastMove;              // {xStart, yStart, xEnd, yEnd}
    public int playerToMove;            // 1 -> White, -1 -> Black
    public int winner;                  // 1 -> White, -1 -> Black, 0 -> game not over
    public int moveCount;               // number of moves made
    public double points;               // used for AI's MinMax algorithm

    public HashSet<List<Integer>>[][] underAttackByW;   // each block is being attacked by which ...
    public HashSet<List<Integer>>[][] underAttackByB;   // ... using List so HashSet can compare it
    public ArrayList<int[]>[][] attacking;     // each piece is attacking which blocks


    GameState(Piece[][] board, int[] wPieceCount, int[] bPieceCount,
              boolean[][] isMoved, int[] lastMove, int playerToMove) {
        int width = board[0].length;
        int height = board.length;

        Piece[][] newBoard = new Piece[height][];
        for (int i = 0; i < height; i++)
            newBoard[i] = board[i].clone();

        boolean[][] newIsMoved;
        if (isMoved == null) {
            newIsMoved = new boolean[height][width];   // default values are false
        }
        else {
            newIsMoved = new boolean[height][];
                for (int i = 0; i < height; i++)
                    newIsMoved[i] = isMoved[i].clone();
        }

        this.board = newBoard;
        this.isMoved = newIsMoved;
        this.wPieceCount = wPieceCount.clone();
        this.bPieceCount = bPieceCount.clone();
        this.lastMove = lastMove == null ? null : lastMove.clone();
        this.playerToMove = playerToMove;
        winner = 0;
        moveCount = 0;
        points = 0;

        // initialize attacking blocks for each piece
        underAttackByW = new HashSet[height][width];
        underAttackByB = new HashSet[height][width];
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                underAttackByW[y][x] = new HashSet<>();
                underAttackByB[y][x] = new HashSet<>();
            }
        }

        attacking = new ArrayList[height][width];
        for (int x = 0; x < width; x ++) {
            for (int y = 0; y < height; y ++) {
                if (board[y][x] != null)
                    updateAttacking(x, y, x, y);
            }
        }
    }

    public boolean underAttackBy(int player, int x, int y) {
        return (player == 1 ? underAttackByW : underAttackByB)[y][x].size() != 0;
    }

    private void updateAttacking(int xBefore, int yBefore, int xNow, int yNow) {
        HashSet<List<Integer>>[][] attacked = board[yNow][xNow].isBelongingTo(1) ?
                underAttackByW : underAttackByB;

        // remove old attacking info
        if (attacking[yBefore][xBefore] != null) {  // need to check b/c null when initializing
            for (int[] attackBlock : attacking[yBefore][xBefore]) {
                int xEnd = attackBlock[2];
                int yEnd = attackBlock[3];
                attacked[yEnd][xEnd].remove(Arrays.asList(xBefore, yBefore));
            }
        }
        attacking[yBefore][xBefore] = null;

        // add new attacking info
        attacking[yNow][xNow] = board[yNow][xNow].getMoveOptions(this, xNow, yNow, true);
        for (int[] attackBlock : attacking[yNow][xNow]) {
            int xEnd = attackBlock[2];
            int yEnd = attackBlock[3];
            attacked[yEnd][xEnd].add(Arrays.asList(xNow, yNow));
        }
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
        return new GameState(board, wPieceCount, bPieceCount, isMoved, lastMove, playerToMove);
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
            int[] pawnsForward = toMove.getPawnForward();
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

        updateAttacking(xStart, yStart, xEnd, yEnd);
    }
}
