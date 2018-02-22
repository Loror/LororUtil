package com.loror.lororUtil.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.database.Cursor;

class TableFinder {
    /**
     * 获得表名
     */
    protected static String getTableName(Class<?> entityType) {
        String tableName = "";
        Table table = (Table) entityType.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalStateException("this object does not define table");
        }
        tableName = table.name();
        if ("".equals(tableName)) {
            tableName = entityType.getSimpleName();
        }
        return tableName;
    }

    /**
     * 获得创建语句
     */
    protected static String getCreateSql(Class<?> entityType) {
        String sql = "";
        String tableName = getTableName(entityType);
        ArrayList<String> columns = new ArrayList<>();
        Class<?> handlerType = entityType;
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        Object entity = null;
        try {
            entity = entityType.newInstance();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        int mainCount = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.column();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object == null) {
                        columns.add(1 + columnName);
                    } else if (object instanceof Integer || object instanceof Long) {
                        columns.add(0 + columnName);
                    } else if (object instanceof String) {
                        columns.add(1 + columnName);
                    } else if (object instanceof Float || object instanceof Double) {
                        columns.add(3 + columnName);
                    } else {
                        columns.add(1 + columnName);
                    }
                }
                Id id = (Id) field.getAnnotation(Id.class);
                if (id != null) {
                    Object object = field.get(entity);
                    if (!(object instanceof Integer) && !(object instanceof Long))
                        throw new IllegalStateException("PRIMARY KEY must be Integer or Long");
                    columns.add(2 + "id");
                    mainCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        if (mainCount > 1) {
            throw new IllegalStateException("cannot contain more than 1 primary key");
        }
        for (String o : columns) {
            if (o.startsWith("1")) {
                sql += o.substring(1) + " text,";
            } else if (o.startsWith("0")) {
                sql += o.substring(1) + " int,";
            } else if (o.startsWith("2")) {
                sql += "id integer PRIMARY KEY AUTOINCREMENT,";
            } else if (o.startsWith("3")) {
                sql += o.substring(1) + " real,";
            }
        }
        sql = sql.substring(0, sql.length() - 1);
        sql = "create table if not exists " + tableName + "(" + sql + ")";
        return sql;
    }

    /**
     * 获得删表语句
     */
    protected static String getDropTableSql(Class<?> entityType) {
        return "drop table if exists " + getTableName(entityType);
    }

    /**
     * 获得更新语句
     */
    protected static String getUpdateSql(Object entity) {
        String sql = "";
        String tableName = getTableName(entity.getClass());
        HashMap<String, String> columns = new HashMap<>();
        String id_pry = "0";
        Class<?> handlerType = entity.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.column();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object == null) {
                        columns.put(columnName, "");
                    } else {
                        columns.put(columnName, object.toString());
                    }
                }
                Id id = (Id) field.getAnnotation(Id.class);
                if (id != null) {
                    Object object = field.get(entity);
                    id_pry = object.toString();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        String keys = "";
        for (String o : columns.keySet()) {
            keys += o + "='" + (columns.get(o) == null ? null : columns.get(o).replace("'", "''")) + "',";
        }
        keys = keys.substring(0, keys.length() - 1);
        sql = "update " + tableName + " set " + keys + "where id = " + id_pry;
        return sql;
    }

    /**
     * 获得插入语句
     */
    protected static String getInsertSql(Object entity) {
        String sql = "";
        String tableName = getTableName(entity.getClass());
        HashMap<String, String> columns = new HashMap<>();
        Class<?> handlerType = entity.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.column();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object != null) {
                        columns.put(columnName, object.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        String keys = "";
        String values = "";
        for (String o : columns.keySet()) {
            keys += o + ",";
            values += "'" + (columns.get(o) == null ? null : columns.get(o).replace("'", "''")) + "',";
        }
        keys = "(" + keys.substring(0, keys.length() - 1) + ")";
        values = "(" + values.substring(0, values.length() - 1) + ")";
        sql = "insert into " + tableName + keys + " values " + values;
        return sql;
    }

    /**
     * 获得删除语句
     */
    protected static String getdeleteSql(Object entity) {
        String sql = "";
        String tableName = getTableName(entity.getClass());
        HashMap<String, String> columns = new HashMap<>();
        Class<?> handlerType = entity.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.column();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object == null) {
                        columns.put(columnName, "");
                    } else {
                        columns.put(columnName, object.toString());
                    }
                }
                Id id = (Id) field.getAnnotation(Id.class);
                if (id != null) {
                    Object object = field.get(entity);
                    columns.put("id", object.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0)
            throw new IllegalStateException("this object does not contains any colume");
        String keys = "";
        for (String o : columns.keySet()) {
            keys += o + "='" + (columns.get(o) == null ? null : columns.get(o).replace("'", "''")) + "' and ";
        }
        keys = keys.substring(0, keys.length() - 5);
        sql = "delete from " + tableName + " where " + keys;
        return sql;
    }

    /**
     * 查询sql数据到对象中
     */
    protected static void find(Object entity, Cursor cursor) {
        Class<?> handlerType = entity.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            return;
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.column();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    String result = cursor.getString(cursor.getColumnIndex(columnName));
                    if (object instanceof Integer) {
                        field.set(entity, Integer.parseInt(result));
                    } else if (object instanceof Long) {
                        field.set(entity, Long.parseLong(result));
                    } else if (object instanceof Float) {
                        field.set(entity, Float.parseFloat(result));
                    } else if (object instanceof Double) {
                        field.set(entity, Double.parseDouble(result));
                    } else {
                        field.set(entity, result);
                    }
                }
                Id id = (Id) field.getAnnotation(Id.class);
                if (id != null) {
                    String result = cursor.getString(cursor.getColumnIndex("id"));
                    field.set(entity, Integer.parseInt(result));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
