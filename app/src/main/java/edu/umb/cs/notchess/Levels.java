package edu.umb.cs.notchess;

import static edu.umb.cs.notchess.Piece.*;

public class Levels {
    static final String[] titles = {"Hold Your Horse", "Regular Chess", "en passant"};

    static final Piece[][][] boards = {
            {
                    {B_Heart, B_Knight, B_Knight, null, null, null, null},
                    {B_Knight, B_Knight, B_Knight, null, null, null, null},
                    {B_Knight, B_Knight, null, null, null, null, null},
                    {null, null, null, null, null, W_Knight, W_Knight},
                    {null, null, null, null, W_Knight, W_Knight, W_Knight},
                    {null, null, null, null, W_Knight, W_Knight, W_Heart},
            },
            {
                    {B_Rook, B_Knight, B_Bishop, B_Queen, B_King, B_Bishop, B_Knight, B_Rook},
                    {B_Pawn, B_Pawn, B_Pawn, B_Pawn, B_Pawn, B_Pawn, B_Pawn, B_Pawn},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null, null},
                    {W_Pawn, W_Pawn, W_Pawn, W_Pawn, W_Pawn, W_Pawn, W_Pawn, W_Pawn},
                    {W_Rook, W_Knight, W_Bishop, W_Queen, W_King, W_Bishop, W_Knight, W_Rook}
            },
            {
                    {null, null, null, null, null, B_Pawn, null, B_King},
                    {null, null, null, null, null, B_Pawn, null, null},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, null, null, null, null},
                    {null, null, null, null, W_Pawn, null, null, null},
                    {W_King, null, null, null, W_Pawn, null, null, null}
            }
    };
}
