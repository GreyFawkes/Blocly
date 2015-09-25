package io.bloc.android.blocly.api.model.database.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 9/23/2015.
 */
public abstract class Table {

    public interface Builder {
        long insert(SQLiteDatabase writableDB);
    }

    protected static final String COLUMN_ID = "id";

    public abstract String getName();

    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writeableDatabase, int oldVersion, int newVersion) {
        //nothing
    }

    protected static String getString(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return "";
        }
        return cursor.getString(columnIndex);
    }

    // #1b
    protected static long getLong(Cursor cursor, String column) {
        int columnIndex = cursor.getColumnIndex(column);
        if (columnIndex == -1) {
            return -1l;
        }
        return cursor.getLong(columnIndex);
    }

    // #2
    protected static boolean getBoolean(Cursor cursor, String column) {
        return getLong(cursor, column) == 1l;
    }
}
