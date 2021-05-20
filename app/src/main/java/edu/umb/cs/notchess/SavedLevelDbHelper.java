package edu.umb.cs.notchess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class SavedLevelDbHelper extends SQLiteOpenHelper {
    // getLevelList() return object
    public static class LevelList {
        public ArrayList<String> titles = new ArrayList<>();
        public ArrayList<Integer> ids = new ArrayList<>();
    }

    // class that defines the table contents
    public static class SavedLevel implements BaseColumns {
        // private constructor prevents accidentally instantiating this class
        private SavedLevel() {}

        private static final String TABLE_NAME = "saved_levels";
        private static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_BOARD = "board";
    }

    // If change the database schema, must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SaveLevel.db";

    public SavedLevelDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SavedLevel.TABLE_NAME + " (" +
                SavedLevel._ID + " INTEGER PRIMARY KEY," +
                SavedLevel.COLUMN_NAME_TITLE + " TEXT," +
                SavedLevel.COLUMN_NAME_BOARD + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // the upgrade policy is simply discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + SavedLevel.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /*============================================================================================*/
    /* Database Operations */

    public void saveLevel(String title, Piece[][] board) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        JSONObject jsonObject = BoardParser.toJson(board);

        values.put(SavedLevel.COLUMN_NAME_TITLE, title);
        values.put(SavedLevel.COLUMN_NAME_BOARD, jsonObject.toString());

        db.insert(SavedLevel.TABLE_NAME, null, values);
        db.close();
    }

    public LevelList getLevelList() {
        SQLiteDatabase db = getReadableDatabase();
        LevelList levelList = new LevelList();

        //SELECT ALL QUERY FROM THE TABLE
        String query = "SELECT * FROM " + SavedLevel.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                levelList.ids.add(cursor.getInt(0));
                levelList.titles.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return levelList;
    }

    public Piece[][] getBoard(int id) {
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + SavedLevel.TABLE_NAME + " WHERE " + SavedLevel._ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});
        Piece[][] board = null;
        if (cursor.moveToFirst()) {
            try {
                JSONObject jsonObject = new JSONObject(cursor.getString(2));
                board = BoardParser.fromJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        cursor.close();
        db.close();
        return board;
    }

    public int deleteLevel(int id) {
        SQLiteDatabase db = getWritableDatabase();
        String whereClause = SavedLevel._ID + " =?";
        int i = db.delete(SavedLevel.TABLE_NAME, whereClause, new String[]{String.valueOf(id)});
        db.close();;
        return i;
    }
}
