package com.loror.lororUtil.sql;

import java.util.ArrayList;

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

    public SQLiteUtil(Context context, String dbName, Class<?> entityType, int version) {
        this(context, dbName, entityType, version, null);
    }

    public SQLiteUtil(Context context, String dbName, int version, OnChange onChange) {
        this(context, dbName, null, version, onChange);
    }

    public SQLiteUtil(Context context, String dbName, Class<?> entityType, int version, OnChange onChange) {
        this.context = context;
        this.dbName = dbName;
        this.onChange = onChange;
        this.version = version;
        init(entityType);
    }

    /**
     * 初始化
     */
    protected void init(final Class<?> entityType) {
        close();
        this.helper = new DataBaseHelper(context, dbName, version, onChange != null ? onChange : new OnChange() {

            @Override
            public void onUpdate(SQLiteUtil sqLiteUtil) {
                if (entityType != null) {
                    sqLiteUtil.dropTable(entityType);
                    sqLiteUtil.createTableIfNotExists(entityType);
                }
            }

            @Override
            public void onCreate(SQLiteUtil sqLiteUtil) {
                if (entityType != null) {
                    sqLiteUtil.createTableIfNotExists(entityType);
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
    public void dropTable(Class<?> entityType) {
        database.execSQL(TableFinder.getDropTableSql(entityType));
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 创建表
     */
    public void createTableIfNotExists(Class<?> entityType) {
        database.execSQL(TableFinder.getCreateSql(entityType));
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
    public long getLastId(Class<?> entityType) {
        Cursor cursor = database.rawQuery("select last_insert_rowid() from " + TableFinder.getTableName(entityType),
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
    public void deleteById(String id, Class<?> entityType) {
        database.execSQL("delete from " + TableFinder.getTableName(entityType) + " where id = '" + id + "'");
        SQLiteDatabase.releaseMemory();
    }

    /**
     * 删除数据
     */
    public void deleteByCondition(ConditionBuilder conditionBuilder, Class<?> entityType) {
        if (conditionBuilder.getConditionCount() > 0) {
            database.execSQL("delete from " + TableFinder.getTableName(entityType) + conditionBuilder.getConditions());
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 删除所有数据
     */
    public void deleteAll(Class<?> entityType) {
        database.execSQL("delete from " + TableFinder.getTableName(entityType));
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
    public <T> T getById(String id, Class<T> entityType) {
        T entity = null;
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(entityType) + " where id = ?",
                new String[] { id });
        if (cursor.moveToNext()) {
            try {
                entity = (T) entityType.newInstance();
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
    public <T> T getFirst(Class<T> entityType) {
        T entity = null;
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(entityType) + " limit 0,2", null);
        if (cursor.moveToNext()) {
            try {
                entity = (T) entityType.newInstance();
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
    public <T> T getFirstByCondition(ConditionBuilder conditionBuilder, Class<T> entityType) {
        T entity = null;
        if (conditionBuilder.getConditionCount() > 0) {
            Cursor cursor = database.rawQuery(
                    "select * from " + TableFinder.getTableName(entityType)
                            + conditionBuilder.getNoColumnConditionsWithoutPage() + " limit 0,2",
                    conditionBuilder.getColumnArray());
            if (cursor.moveToNext()) {
                try {
                    entity = (T) entityType.newInstance();
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
    public <T> ArrayList<T> getByCondition(ConditionBuilder conditionBuilder, Class<T> entityType) {
        ArrayList<T> entitys = new ArrayList<>();
        Cursor cursor = database.rawQuery(
                "select * from " + TableFinder.getTableName(entityType) + conditionBuilder.getNoColumnConditions(),
                conditionBuilder.getColumnArray());
        while (cursor.moveToNext()) {
            try {
                T entity = (T) entityType.newInstance();
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
    public <T> ArrayList<T> getAll(Class<T> entityType) {
        ArrayList<T> entitys = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + TableFinder.getTableName(entityType), null);
        while (cursor.moveToNext()) {
            try {
                T entity = (T) entityType.newInstance();
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
    public int count(Class<?> entityType) {
        int count = 0;
        Cursor cursor = database.rawQuery("select count(1) from " + TableFinder.getTableName(entityType), null);
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
    public int countByCondition(ConditionBuilder conditionBuilder, Class<?> entityType) {
        int count = 0;
        Cursor cursor = database.rawQuery("select count(1) from " + TableFinder.getTableName(entityType)
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
}
