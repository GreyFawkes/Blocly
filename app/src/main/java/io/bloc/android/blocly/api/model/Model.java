package io.bloc.android.blocly.api.model;

/**
 * Created by Administrator on 9/26/2015.
 */
public abstract class Model {

    private final long rowId;

    public Model(long rowId) {
        this.rowId = rowId;
    }

    public long getRowId() {
        return rowId;
    }

}
