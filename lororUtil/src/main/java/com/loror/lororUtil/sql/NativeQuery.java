package com.loror.lororUtil.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class NativeQuery {

    private final SQLiteDatabase database;

    public NativeQuery(SQLiteDatabase database) {
        this.database = database;
    }

    public ModelDataList executeQuery(String sql) {
        ModelDataList list = new ModelDataList();
        Cursor cursor = database.rawQuery(sql, null);
        String[] names = cursor.getColumnNames();
        while (cursor.moveToNext()) {
            ModelData data = new ModelData();
            for (String name : names) {
                String result = cursor.getString(cursor.getColumnIndex(name));
                data.add(name, result);
            }
            list.add(data);
        }
        cursor.close();
        return list;
    }

    public void executeUpdate(String sql) {
        database.execSQL(sql);
    }
}
