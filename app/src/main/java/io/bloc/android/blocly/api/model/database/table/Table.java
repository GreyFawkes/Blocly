package io.bloc.android.blocly.api.model.database.table;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Administrator on 9/23/2015.
 */
public abstract class Table {

    protected static final String COLUMN_ID = "id";

    public abstract String getName();

    public abstract String getCreateStatement();

    public void onUpgrade(SQLiteDatabase writeableDatabase, int oldVersion, int newVersion) {
        //nothing
    }
}
