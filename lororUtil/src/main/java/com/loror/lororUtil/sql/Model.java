package com.loror.lororUtil.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model<T> {

    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_APPEND = 1;

    private Class<T> table;
    private SQLiteUtil sqLiteUtil;
    private ModelInfo modelInfo;
    private int type = TYPE_NORMAL;
    private ConditionBuilder conditionBuilder = ConditionBuilder.create();

    public interface onWhen<T> {
        void when(Model<T> model);
    }

    public Model(Class<T> table, SQLiteUtil sqLiteUtil, ModelInfo modelInfo) {
        this.table = table;
        this.sqLiteUtil = sqLiteUtil;
        this.modelInfo = modelInfo;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    public Model<T> type(int type) {
        this.type = type;
        return this;
    }

    public Model<T> where(String key, Object var) {
        return where(key, var == null ? "is" : "=", var);
    }

    public Model<T> where(String key, String operation, Object var) {
        if (type == TYPE_NORMAL) {
            conditionBuilder.addCondition(key, operation, var);
        } else {
            conditionBuilder.withCondition(key, operation, var);
        }
        return this;
    }

    public Model<T> whereOr(String key, Object var) {
        return whereOr(key, var == null ? "is" : "=", var);
    }

    public Model<T> whereOr(String key, String operation, Object var) {
        if (type == TYPE_NORMAL) {
            conditionBuilder.addOrCondition(key, operation, var);
        } else {
            conditionBuilder.withOrCondition(key, operation, var);
        }
        return this;
    }

    public Model<T> whereIn(String key, String... vars) {
        return whereIn(key, "in", vars);
    }

    public Model<T> whereIn(String key, String operation, String... vars) {
        if (vars == null || vars.length == 0) {
            return this;
        }
        return whereIn(key, operation, Arrays.asList(vars));
    }

    public Model<T> whereIn(String key, List<?> vars) {
        return whereIn(key, "in", vars);
    }

    public Model<T> whereIn(String key, String operation, List<?> vars) {
        if (vars == null || vars.size() == 0) {
            throw new IllegalArgumentException("in condition can not be empty");
        }
        if (type == TYPE_NORMAL) {
            conditionBuilder.addInCondition(key, operation, vars);
        } else {
            conditionBuilder.withInCondition(key, operation, vars);
        }
        return this;
    }

    public Model<T> whereOrIn(String key, String... vars) {
        return whereOrIn(key, "in", vars);
    }

    public Model<T> whereOrIn(String key, String operation, String... vars) {
        if (vars == null || vars.length == 0) {
            return this;
        }
        return whereOrIn(key, operation, Arrays.asList(vars));
    }

    public Model<T> whereOrIn(String key, List<?> vars) {
        return whereOrIn(key, "in", vars);
    }

    public Model<T> whereOrIn(String key, String operation, List<?> vars) {
        if (vars == null || vars.size() == 0) {
            throw new IllegalArgumentException("in condition can not be empty");
        }
        if (type == TYPE_NORMAL) {
            conditionBuilder.addOrInCondition(key, operation, vars);
        } else {
            conditionBuilder.withOrInCondition(key, operation, vars);
        }
        return this;
    }

    public Model<T> when(boolean satisfy, onWhen<T> onWhen) {
        if (satisfy && onWhen != null) {
            onWhen.when(this);
        }
        return this;
    }

    public Model<T> orderBy(String key, int order) {
        conditionBuilder.withOrder(key, order);
        return this;
    }

    public Model<T> page(int page, int size) {
        conditionBuilder.withPagination(page, size);
        return this;
    }

    /**
     * 保存
     */
    public void save(T entity) {
        if (entity != null) {
            ModelInfo.ColumnInfo idColumn = modelInfo.getId();
            if (idColumn == null) {
                sqLiteUtil.insert(entity);
            } else {
                long id = 0;
                Field field = idColumn.getField();
                field.setAccessible(true);
                try {
                    Object var = field.get(entity);
                    id = var == null ? 0 : Long.parseLong(String.valueOf(var));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if (id == 0) {
                    sqLiteUtil.insert(entity);
                } else {
                    sqLiteUtil.updateById(entity);
                }
            }
        }
    }

    /**
     * 保存
     */
    public void save(List<T> entities) {
        sqLiteUtil.getDatabase().beginTransaction();
        try {
            for (T t : entities) {
                save(t);
            }
            sqLiteUtil.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqLiteUtil.getDatabase().endTransaction();
        }
    }

    /**
     * 条件删除
     */
    public void delete() {
        if (conditionBuilder.getConditionCount() > 0) {
            sqLiteUtil.getDatabase().execSQL("delete from " + modelInfo.getTableName() + conditionBuilder.getConditions());
            if (sqLiteUtil.mitiProgress) {
                SQLiteDatabase.releaseMemory();
            }
        }
    }

    /**
     * 清空表
     */
    public void clear() {
        sqLiteUtil.deleteAll(table);
    }

    /**
     * 截断表
     */
    public void truncate() {
        sqLiteUtil.deleteAll(table);
        sqLiteUtil.getDatabase().execSQL("delete from sqlite_sequence WHERE name = " + modelInfo.getTableName());
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 修改
     */
    public void update(T entity, boolean ignoreNull) {
        if (entity == null) {
            return;
        }
        sqLiteUtil.getDatabase().execSQL(TableFinder.getUpdateSqlNoWhere(entity, modelInfo, ignoreNull)
                + conditionBuilder.getConditionsWithoutPage());
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
    }

    /**
     * 条件计数
     */
    public int count() {
        int count = 0;
        Cursor cursor = null;
        if (conditionBuilder.getConditionCount() == 0) {
            cursor = sqLiteUtil.getDatabase().rawQuery("select count(1) from " + modelInfo.getTableName(), null);
        } else if (conditionBuilder.isHasNull()) {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select count(1) from " + modelInfo.getTableName() + conditionBuilder.getConditions(),
                    null);
        } else {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select count(1) from " + modelInfo.getTableName() + conditionBuilder.getNoColumnConditions(),
                    conditionBuilder.getColumnArray());
        }
        if (cursor.moveToNext()) {
            try {
                count = cursor.getInt(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return count;
    }

    /**
     * 条件查询
     */
    public List<T> get() {
        List<T> entitys = new ArrayList<>();
        Cursor cursor = null;
        if (conditionBuilder.isHasNull()) {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select * from " + modelInfo.getTableName() + conditionBuilder.getConditions(),
                    null);
        } else {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select * from " + modelInfo.getTableName() + conditionBuilder.getNoColumnConditions(),
                    conditionBuilder.getColumnArray());
        }
        while (cursor.moveToNext()) {
            T entity = null;
            try {
                entity = (T) modelInfo.getTableObject();
                TableFinder.find(entity, cursor);
                entitys.add(entity);
            } catch (Exception e) {
                e.printStackTrace();
                if (entity == null) {
                    throw new IllegalArgumentException(table.getSimpleName() + " have no non parametric constructor");
                }
            }
        }
        cursor.close();
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return entitys;
    }

    /**
     * 条件查询首条
     */
    public T first() {
        T entity = null;
        Cursor cursor = null;
        if (conditionBuilder.isHasNull()) {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select * from " + modelInfo.getTableName()
                            + conditionBuilder.getConditionsWithoutPage() + " limit 0,2",
                    null);
        } else {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select * from " + modelInfo.getTableName()
                            + conditionBuilder.getNoColumnConditionsWithoutPage() + " limit 0,2",
                    conditionBuilder.getColumnArray());
        }
        if (cursor.moveToNext()) {
            try {
                entity = (T) modelInfo.getTableObject();
                TableFinder.find(entity, cursor);
            } catch (Exception e) {
                e.printStackTrace();
                if (entity == null) {
                    throw new IllegalArgumentException(table.getSimpleName() + " have no non parametric constructor");
                }
            }
        }
        cursor.close();
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return entity;
    }
}
