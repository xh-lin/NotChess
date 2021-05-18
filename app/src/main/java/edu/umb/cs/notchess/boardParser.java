package edu.umb.cs.notchess;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class boardParser {
    static final String JSON_KEY = "board";

    static JSONObject toJson(Piece[][] board) {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (int y = 0; y < board.length; y++) {
            JSONArray rowArray = new JSONArray();
            for (int x = 0; x < board[0].length; x++) {
                Piece piece = board[y][x];
                rowArray.put(piece == null ? null : piece.name());
            }
            jsonArray.put(rowArray);
        }

        try {
            jsonObject.put(JSON_KEY, jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;
    }

    static Piece[][] fromJson(JSONObject jsonObject) {
        JSONArray jsonArray = jsonObject.optJSONArray(JSON_KEY);
        Piece[][] board = new Piece[jsonArray.length()][];

        for (int y = 0; y < jsonArray.length(); y++) {
            try {
                JSONArray rowArray = jsonArray.getJSONArray(y);
                board[y] = new Piece[rowArray.length()];
                for (int x = 0; x < rowArray.length(); x++) {
                    board[y][x] = rowArray.isNull(x) ? null : Piece.valueOf(rowArray.getString(x));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return board;
    }
}
