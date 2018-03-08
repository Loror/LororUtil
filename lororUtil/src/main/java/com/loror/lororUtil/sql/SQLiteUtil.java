package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SQLiteUtil {
    private DataBaseHelper helper;
    private SQLiteDatabase database;
    private Context context;
    private String dbName;
    private OnChange onChange;
    private int version;

    public interface OnChange {
        void onCreate(SQLiteUtil sqLiteUtil);

        void onUpdate(SQLiteUtil sqLiteUtil);
    }

    protected interface Link {
        void link(SQLiteDatabase database);
    }

    public SQLiteUtil(Context context, String dbName) {
        this(context, dbName, null, 1, null);
    }

    public SQLiteUtil(Context context, String dbName, int version) {
        this(context, dbName, null, version, null);
    }

    public SQLiteUtil(Context context, String dbName, Class<?> table, int version) {
        this(context, dbName, table, version, null);
    }

    public SQLiteUtil(Context context, String dbName, int version, OnChange onChange) {
        this(context, dbName, null, version, onChange);
    }

    public SQLiteUtil(Context context, String dbName, Class<?> table, int version, OnChange onChange) {
        this.context = context;
        this.dbName = dbName;
        this.onChange = onChange;
        this.version = version;
        init(table);
    }

    /**
     * 初始化
     */
    protected void init(final Class<?> table) {
        close();
        this.helper = new DataBaseHelper(context, dbName, version, onChange != null ? onChange : new OnChange() {

            @Override
            public void onUpdate(SQLiteUtil sqLiteUtil) {
                if (table != null) {
                    sqLiteUtil.dropTable(table);
                    sqLiteUtil.createTableIfNotExists(table);
                }
            }

            @Override
            public void onCreate(SQLiteUtil sqLiteUtil) {
                if (table != null) {
                    sqLiteUtil.createTableIfNotExists(table);
                }
            }
        }, new Link() {

            @Override
            public void link(SQLiteDatabase database) {
                SQLiteUtil.this.database = database;
            }
        }, this);
        this.database = this.helper.getWritableDatabase();
    }

    /**
     * 获取数据库操作对象
     */
    public SQLiteDatabase getDatabase() {
        return database;
    }

    /**
     * 删除表
     */
    public void dropTable(Class<?> table) {
        database.execSQL(TableFinder.getDropTableSql(table));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 创建表
     */
    public void createTableIfNotExists(Class<?> table) {
        database.execSQL(TableFinder.getCreateSql(table));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 插入数据
     */
    public void insert(Object entity) {
        database.execSQL(TableFinder.getInsertSql(entity));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 获取最后插入数据id
     */
    public long getLastId(Class<?> table) {
        Cursor cursor = database.rawQuery("select last_insert_rowid() from " + TableFinder.getTableName(table),
                null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return id;
    }

    /**
     * 删除数据
     */
    public void delete(Object entity) {
        database.execSQL(TableFinder.getdeleteSql(entity));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 删除数据
     */
    public void deleteById(String id, Class<?> table) {
        database.execSQL("delete from " + TableFinder.getTableName(table) + " where id = '" + id + "'");
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 删除数据
     */
    public void deleteByCondition(ConditionBuilder conditionBuilder, Class<?> table) {
        if (conditionBuilder.getConditionCount() > 0) {
            database.execSQL("delete from " + TableFinder.getTableName(table) + conditionBuilder.getConditions());
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 删除所有数据
     */
    public void deleteAll(Class<?> table) {
        database.execSQL("delete from " + TableFinder.getTableName(table));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 根据id更新数据
     */
    public void updateById(Object entity) {
        database.execSQL(TableFinder.getUpdateSql(entity));
    }

    /**
     * 根据id(主键)获取数据
     */
    public <T> T getById(String id, Class<T> table) {
        T entity = null;
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(table) + " where id = ?",
                new String[]{id});
        if (cursor.moveToNext()) {
            try {
                entity = (T) table.newInstance();
                TableFinder.find(entity, cursor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return entity;
    }

    /**
     * 获取首条数据
     */
    public <T> T getFirst(Class<T> table) {
        T entity = null;
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(table) + " limit 0,2", null);
        if (cursor.moveToNext()) {
            try {
                entity = (T) table.newInstance();
                TableFinder.find(entity, cursor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return entity;
    }

    /**
     * 获取首条数据
     */
    public <T> T getFirstByCondition(ConditionBuilder conditionBuilder, Class<T> table) {
        T entity = null;
        if (conditionBuilder.getConditionCount() > 0) {
            Cursor cursor = database.rawQuery(
                    "select * from " + TableFinder.getTableName(table)
                            + conditionBuilder.getNoColumnConditionsWithoutPage() + " limit 0,2",
                    conditionBuilder.getColumnArray());
            if (cursor.moveToNext()) {
                try {
                    entity = (T) table.newInstance();
                    TableFinder.find(entity, cursor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
            SQLiteDatabase.releaseMemory();
        }
        return entity;
    }

    /**
     * 获取数据
     */
    public <T> List<T> getByCondition(ConditionBuilder conditionBuilder, Class<T> table) {
        List<T> entitys = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "select * from " + TableFinder.getTableName(table) + conditionBuilder.getNoColumnConditions(),
                conditionBuilder.getColumnArray());
        while (cursor.moveToNext()) {
            try {
                T entity = (T) table.newInstance();
                TableFinder.find(entity, cursor);
                entitys.add(entity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return entitys;
    }

    /**
     * 获取所有数据
     */
    public <T> List<T> getAll(Class<T> table) {
        List<T> entitys = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(table), null);
        while (cursor.moveToNext()) {
            try {
                T entity = (T) table.newInstance();
                entitys.add(entity);
                TableFinder.find(entity, cursor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return entitys;
    }

    /**
     * 获取条目
     */
    public int count(Class<?> table) {
        int count = 0;
        Cursor cursor = database.rawQuery("select count(1) from " + TableFinder.getTableName(table), null);
        if (cursor.moveToNext()) {
            try {
                count = cursor.getInt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return count;
    }

    /**
     * 获取条目
     */
    public int countByCondition(ConditionBuilder conditionBuilder, Class<?> table) {
        int count = 0;
        Cursor cursor = database.rawQuery("select count(1) from " + TableFinder.getTableName(table)
                + conditionBuilder.getNoColumnConditions(), conditionBuilder.getColumnArray());
        if (cursor.moveToNext()) {
            try {
                count = cursor.getInt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        SQLiteDatabase.releaseMemory();
        return count;
    }

    /**
     * 关闭
     */
    public void close() {
        if (this.database != null) {
            this.database.close();
            this.database = null;
        }
    }

    /**
     * 打开
     */
    public void reOpen() {
        if (this.database == null || !this.database.isOpen()) {
            this.database = this.helper.getWritableDatabase();
        }
    }
}
