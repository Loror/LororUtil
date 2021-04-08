package com.loror.lororUtil.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SQLiteUtil {

    private final Context context;
    private final String dbName;
    private final int version;
    private SQLiteDatabase database;
    protected boolean mitiProgress = true;
    private final List<Class<?>> execTableCreated = new LinkedList<>();//已执行过创建
    private final List<Class<?>> execTableUpdated = new LinkedList<>();//已执行过更新表
    private final HashMap<Class<?>, ModelInfo> classModel = new HashMap<>();
    private OnClose onClose;//执行close方法时候调用，如果为空则执行关闭连接

    public interface OnChange {
        void onCreate(SQLiteUtil sqLiteUtil);

        void onUpdate(SQLiteUtil sqLiteUtil);
    }

    protected interface Link {
        void link(SQLiteDatabase database);
    }

    public interface OnClose {
        void close(SQLiteUtil sqLiteUtil);
    }

    public SQLiteUtil(Context context, String dbName) {
        this(context, dbName, 1);
    }

    public SQLiteUtil(Context context, String dbName, int version) {
        this(context, dbName, version, null);
    }

    public SQLiteUtil(Context context, String dbName, int version, OnChange onChange) {
        this.context = context.getApplicationContext();
        this.dbName = dbName;
        this.version = version;
        init(onChange);
    }

    /**
     * 初始化
     */
    private void init(OnChange onChange) {
        close();
        DataBaseHelper helper = new DataBaseHelper(context, dbName, version, onChange,
                new Link() {

                    @Override
                    public void link(SQLiteDatabase database) {
                        SQLiteUtil.this.database = database;
                    }
                }, this);
        this.database = helper.getWritableDatabase();
    }

    public String getDbName() {
        return dbName;
    }

    public SQLiteUtil setMultiProgress(boolean multiProgress) {
        this.mitiProgress = multiProgress;
        return this;
    }

    public SQLiteUtil setOnClose(OnClose onClose) {
        this.onClose = onClose;
        return this;
    }

    /**
     * 获取数据库操作对象
     */
    public SQLiteDatabase getDatabase() {
        return database;
    }

    private ModelInfo getModel(Class<?> table) {
        if (table == null) {
            return null;
        }
        ModelInfo model = classModel.get(table);
        if (model == null) {
            model = new ModelInfo(table);
            classModel.put(table, model);
        }
        return model;
    }

    /**
     * 删除表
     */
    public void dropTable(Class<?> table) {
        database.execSQL(TableFinder.getDropTableSql(getModel(table)));
        execTableCreated.remove(table);
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 创建表
     */
    public void createTableIfNotExists(Class<?> table) {
        if (execTableCreated.contains(table)) {
            return;
        }
        execTableCreated.add(table);
        database.execSQL(TableFinder.getCreateSql(getModel(table)));
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 表字段增加
     * 注意：执行更新表后，可能表未及时变化，下次执行会再次执行更新表而报错，需保证close之前只执行一次
     */
    public void changeTableIfColumnAdd(Class<?> table) {
        if (execTableUpdated.contains(table)) {
            return;
        }
        execTableUpdated.add(table);
        Cursor cursor = database.rawQuery("select * from " + getModel(table).getTableName() + " where 0", null);
        String[] columnNames = cursor.getColumnNames();
        cursor.close();

        String idName = null;
        List<String> newColumnNames = new ArrayList<>();
        HashMap<String, Class<?>> objectHashMap = new HashMap<>();
        HashMap<String, Column> columnHashMap = new HashMap<>();
        ModelInfo modelInfo = getModel(table);
        for (ModelInfo.ColumnInfo columnInfo : modelInfo.getColumnInfos()) {
            Field field = columnInfo.getField();
            if (columnInfo.isPrimaryKey()) {
                idName = columnInfo.getName();
            } else {
                Column column = field.getAnnotation(Column.class);
                newColumnNames.add(columnInfo.getName());
                columnHashMap.put(columnInfo.getName(), column);
                objectHashMap.put(columnInfo.getName(), field.getType());
            }
        }

        int i;
        List<String> different = new ArrayList<>();
        for (i = 0; i < columnNames.length; i++) {
            String columnName = columnNames[i];
            if (idName != null && idName.equals(columnName)) {
                continue;
            }
            boolean remove = newColumnNames.remove(columnName);
            if (!remove) {
                different.add(columnName);
            }
        }
        if (different.size() > 0) {
            StringBuilder builder = new StringBuilder("[");
            for (String var : different) {
                builder.append(var)
                        .append(",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("]");
            throw new IllegalStateException("cannot reduce column at this function:" + builder.toString());
        }
        database.beginTransaction();
        for (i = 0; i < newColumnNames.size(); i++) {
            String newColumnName = newColumnNames.get(i);
            String type = "text";
            Class<?> objectType = objectHashMap.get(newColumnName);
            if (objectType == Integer.class || objectType == int.class || objectType == Long.class || objectType == long.class) {
                type = "int";
            } else if (objectType == Float.class || objectType == float.class || objectType == Double.class || objectType == double.class) {
                type = "real";
            } else if (objectType == String.class) {
                type = "text";
            } else {
                throw new IllegalArgumentException("unsupported column type " + (objectType == null ? null : objectType.getSimpleName()) + " :" + newColumnNames.get(i));
            }
            Column column = columnHashMap.get(newColumnName);
            String defaultValue = column.defaultValue();
            defaultValue = ColumnFilter.getColumn(newColumnName, defaultValue, column);
            database.execSQL("alter table " + modelInfo.getTableName() + " add column " + newColumnName + " " + type);
            if (defaultValue != null && defaultValue.length() > 0) {
                database.execSQL("update " + modelInfo.getTableName() + " set " + newColumnName +
                        " = '" + ColumnFilter.safeColumn(defaultValue) + "'");
            }
        }
        database.setTransactionSuccessful();
        database.endTransaction();
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 获取条件处理model
     */
    public <T> Model<T> model(Class<T> table) {
        return model(table, true);
    }

    /**
     * 获取条件处理model
     */
    public <T> Model<T> model(Class<T> table, boolean checkTable) {
        if (checkTable) {
            createTableIfNotExists(table);
            try {
                changeTableIfColumnAdd(table);
            } catch (Exception e) {
                Log.e("SQLiteUtil", "changeTable failed:", e);
            }
        }
        return new Model<>(table, this, getModel(table));
    }

    /**
     * native查询
     */
    public NativeQuery nativeQuery() {
        return new NativeQuery(database);
    }

    public boolean transaction(Runnable runnable) {
        if (runnable == null) {
            return false;
        }

        if (database.inTransaction()) {
            runnable.run();
            return true;
        }

        database.beginTransaction();
        try {
            runnable.run();
            database.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
        }
        return false;
    }

    /**
     * 插入数据
     */
    public void insert(Object entity) {
        if (entity == null) {
            return;
        }
        database.execSQL(TableFinder.getInsertSql(entity, getModel(entity.getClass())));
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 获取最后插入数据id
     */
    public long lastInsertId(Class<?> table) {
        Cursor cursor = database.rawQuery(TableFinder.getLastIdSql(getModel(table)), null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(0);
        }
        cursor.close();
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return id;
    }

    /**
     * 删除数据
     */
    public void delete(Object entity) {
        if (entity == null) {
            return;
        }
        database.execSQL(TableFinder.getDeleteSql(entity, getModel(entity.getClass())));
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 删除数据
     */
    public void deleteById(String id, Class<?> table) {
        database.execSQL("delete from " + getModel(table).getTableName() + " where " + getModel(table).getId().getName() + " = '" + id + "'");
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 删除所有数据
     */
    public void deleteAll(Class<?> table) {
        database.execSQL("delete from " + getModel(table).getTableName());
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 根据id更新数据
     */
    public void updateById(Object entity) {
        if (entity == null) {
            return;
        }
        database.execSQL(TableFinder.getUpdateSql(entity, getModel(entity.getClass())));
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 获取所有数据
     */
    public <T> List<T> getAll(Class<T> table) {
        List<T> entitys = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from " + getModel(table).getTableName(), null);
        while (cursor.moveToNext()) {
            T entity = null;
            try {
                entity = (T) getModel(table).getTableObject();
                entitys.add(entity);
                TableFinder.find(entity, cursor);
            } catch (Exception e) {
                e.printStackTrace();
                if (entity == null) {
                    throw new IllegalArgumentException(table.getSimpleName() + " have no non parametric constructor");
                }
            }
        }
        cursor.close();
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return entitys;
    }

    /**
     * 获取条目
     */
    public int count(Class<?> table) {
        int count = 0;
        Cursor cursor = database.rawQuery("select count(1) from " + getModel(table).getTableName(), null);
        if (cursor.moveToNext()) {
            try {
                count = cursor.getInt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        if (mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return count;
    }

    /**
     * 重新开启
     */
    public void reStart() {
        if (this.database == null) {
            init(null);
        }
    }

    /**
     * 关闭
     */
    public void close() {
        if (this.onClose != null) {
            this.onClose.close(this);
        } else {
            if (this.database != null) {
                this.database.close();
                this.database = null;
            }
        }
    }

    /**
     * 是否关闭
     */
    public boolean isClosed() {
        return this.database == null;
    }
}
