package com.loror.lororUtil.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model<T> implements Where {

    private final Class<T> table;
    private final SQLiteUtil sqLiteUtil;
    private final ModelInfo modelInfo;
    private final ConditionBuilder conditionBuilder = ConditionBuilder.create();
    private boolean returnInfluence;

    public Model(Class<T> table, SQLiteUtil sqLiteUtil, ModelInfo modelInfo) {
        this.table = table;
        this.sqLiteUtil = sqLiteUtil;
        this.modelInfo = modelInfo;
    }

    public ModelInfo getModelInfo() {
        return modelInfo;
    }

    /**
     * 更新、删除操作是否返回影响行数
     */
    public Model<T> returnInfluence(boolean returnInfluence) {
        this.returnInfluence = returnInfluence;
        return this;
    }

    @Override
    public Model<T> where(String key, Object var) {
        return where(key, var == null ? "is" : "=", var);
    }

    @Override
    public Model<T> where(String key, String operation, Object var) {
        conditionBuilder.addCondition(key, operation, var);
        return this;
    }

    @Override
    public Model<T> whereOr(String key, Object var) {
        return whereOr(key, var == null ? "is" : "=", var);
    }

    @Override
    public Model<T> whereOr(String key, String operation, Object var) {
        conditionBuilder.addOrCondition(key, operation, var);
        return this;
    }

    @Override
    public Model<T> whereIn(String key, Object[] vars) {
        return whereIn(key, "in", vars);
    }

    @Override
    public Model<T> whereIn(String key, String operation, Object[] vars) {
        if (vars == null || vars.length == 0) {
            return this;
        }
        return whereIn(key, operation, Arrays.asList(vars));
    }

    @Override
    public Model<T> whereIn(String key, List<?> vars) {
        return whereIn(key, "in", vars);
    }

    @Override
    public Model<T> whereIn(String key, String operation, List<?> vars) {
        if (vars == null || vars.size() == 0) {
            throw new IllegalArgumentException("in condition can not be empty");
        }
        conditionBuilder.addInCondition(key, operation, vars);
        return this;
    }

    @Override
    public Model<T> whereOrIn(String key, Object[] vars) {
        return whereOrIn(key, "in", vars);
    }

    @Override
    public Model<T> whereOrIn(String key, String operation, Object[] vars) {
        if (vars == null || vars.length == 0) {
            return this;
        }
        return whereOrIn(key, operation, Arrays.asList(vars));
    }

    @Override
    public Model<T> whereOrIn(String key, List<?> vars) {
        return whereOrIn(key, "in", vars);
    }

    @Override
    public Model<T> whereOrIn(String key, String operation, List<?> vars) {
        if (vars == null || vars.size() == 0) {
            throw new IllegalArgumentException("in condition can not be empty");
        }
        conditionBuilder.addOrInCondition(key, operation, vars);
        return this;
    }

    @Override
    public Model<T> where(OnWhere onWhere) {
        return where(onWhere, 0);
    }

    @Override
    public Model<T> whereOr(OnWhere onWhere) {
        return where(onWhere, 1);
    }

    private Model<T> where(OnWhere onWhere, int type) {
        if (onWhere != null) {
            Model<T> model = new Model<T>(table, sqLiteUtil, modelInfo);
            onWhere.where(model);
            List<Condition> conditions = model.conditionBuilder.getConditionList();
            if (conditions.size() > 0) {
                Condition top = conditions.get(0);
                top.setType(type);
                for (Condition condition : conditions) {
                    if (condition == top) {
                        continue;
                    }
                    top.addCondition(condition);
                }
                conditionBuilder.addCondition(top, model.conditionBuilder.isHasNull());
            }
        }
        return this;
    }

    @Override
    public Model<T> when(boolean satisfy, OnWhere onWhere) {
        if (satisfy && onWhere != null) {
            onWhere.where(this);
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
    public boolean save(List<T> entities) {
        return save(entities, true);
    }

    /**
     * 保存
     */
    public boolean save(List<T> entities, boolean transaction) {
        if (transaction && !sqLiteUtil.getDatabase().inTransaction()) {
            sqLiteUtil.getDatabase().beginTransaction();
            try {
                for (T t : entities) {
                    save(t);
                }
                sqLiteUtil.getDatabase().setTransactionSuccessful();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sqLiteUtil.getDatabase().endTransaction();
            }
            return false;
        } else {
            for (T t : entities) {
                save(t);
            }
            return true;
        }
    }

    /**
     * 条件删除
     */
    public int delete() {
        int influence = 0;
        if (conditionBuilder.getConditionCount() > 0) {
            String sql = "delete from " + modelInfo.getTableName() + conditionBuilder.getConditions(true);
            if (returnInfluence) {
                influence = sqLiteUtil.nativeQuery().executeUpdateStatement(sql);
            } else {
                sqLiteUtil.getDatabase().execSQL(sql);
            }
            if (sqLiteUtil.mitiProgress) {
                SQLiteDatabase.releaseMemory();
            }
        }
        return influence;
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
     * 更新
     */
    public int update(ModelData values, boolean ignoreNull) {
        int influence = 0;
        if (values == null) {
            return influence;
        }
        String sql = TableFinder.getUpdateSqlNoWhere(values, modelInfo, ignoreNull)
                + conditionBuilder.getConditionsWithoutPage(true);
        if (returnInfluence) {
            influence = sqLiteUtil.nativeQuery().executeUpdateStatement(sql);
        } else {
            sqLiteUtil.getDatabase().execSQL(sql);
        }
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return influence;
    }

    /**
     * 更新
     */
    public int update(T entity, boolean ignoreNull) {
        int influence = 0;
        if (entity == null) {
            return influence;
        }
        String sql = TableFinder.getUpdateSqlNoWhere(entity, modelInfo, ignoreNull)
                + conditionBuilder.getConditionsWithoutPage(true);
        if (returnInfluence) {
            influence = sqLiteUtil.nativeQuery().executeUpdateStatement(sql);
        } else {
            sqLiteUtil.getDatabase().execSQL(sql);
        }
        if (sqLiteUtil.mitiProgress) {
            SQLiteDatabase.releaseMemory();
        }
        return influence;
    }

    /**
     * 条件计数
     */
    public int count() {
        int count = 0;
        Cursor cursor = null;
        if (conditionBuilder.getConditionCount() == 0) {
            cursor = sqLiteUtil.getDatabase().rawQuery("select count(1) from " + modelInfo.getTableName(), null);
        } else {
            cursor = sqLiteUtil.getDatabase().rawQuery(
                    "select count(1) from " + modelInfo.getTableName() + conditionBuilder.getConditionsWithoutPage(true),
                    null);
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
        Cursor cursor = sqLiteUtil.getDatabase().rawQuery(
                "select * from " + modelInfo.getTableName() + conditionBuilder.getConditions(true),
                null);
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
        Cursor cursor = sqLiteUtil.getDatabase().rawQuery(
                "select * from " + modelInfo.getTableName()
                        + conditionBuilder.getConditionsWithoutPage(true) + " limit 0,2",
                null);
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

    @Override
    public String toString() {
        return conditionBuilder.toString();
    }
}
