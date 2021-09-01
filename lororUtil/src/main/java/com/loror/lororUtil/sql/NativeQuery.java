package com.loror.lororUtil.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.lang.reflect.Constructor;

public class NativeQuery {

    private final SQLiteDatabase database;

    public NativeQuery(SQLiteDatabase database) {
        this.database = database;
    }

    /**
     * 执行查询
     */
    public ModelDataList executeQuery(String sql) {
        return executeQuery(sql, null);
    }

    /**
     * 执行查询
     */
    public ModelDataList executeQuery(String sql, String[] selectionArgs) {
        ModelDataList list = new ModelDataList();
        Cursor cursor = database.rawQuery(sql, selectionArgs);
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

    /**
     * 执行更新/删除
     */
    public void executeUpdate(String sql) {
        executeUpdate(sql, null);
    }

    /**
     * 执行更新/删除
     */
    public void executeUpdate(String sql, Object[] bindArgs) {
        if (bindArgs == null) {
            database.execSQL(sql);
        } else {
            database.execSQL(sql, bindArgs);
        }
    }

    /**
     * 执行更新/删除，并接收影响行数
     */
    public int executeUpdateStatement(String sql) {
        return executeUpdateStatement(sql, null);
    }

    /**
     * 执行更新/删除，并接收影响行数
     */
    public int executeUpdateStatement(String sql, Object[] bindArgs) {
        database.acquireReference();
        try {
            Constructor<SQLiteStatement> constructor = SQLiteStatement.class.getDeclaredConstructor(SQLiteDatabase.class, String.class, Object[].class);
            SQLiteStatement statement = constructor.newInstance(database, sql, bindArgs);
            try {
                return statement.executeUpdateDelete();
            } finally {
                statement.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.releaseReference();
        }
        executeUpdate(sql, bindArgs);
        return 1;
    }
}
