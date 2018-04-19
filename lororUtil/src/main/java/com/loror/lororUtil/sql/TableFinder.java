package com.loror.lororUtil.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import android.database.Cursor;

public class TableFinder {
    /**
     * 获得表名
     */
    public static String getTableName(Class<?> entityType) {
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
    public static String getCreateSql(Class<?> entityType) {
        String tableName = getTableName(entityType);
        ArrayList<String> columns = new ArrayList<>();
        Field[] fields = entityType.getDeclaredFields();
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
                    if (!(object instanceof Integer) && !(object instanceof Long)) {
                        throw new IllegalStateException("PRIMARY KEY must be Integer or Long");
                    }
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
        StringBuilder builder = new StringBuilder();
        for (String o : columns) {
            if (o.startsWith("1")) {
                builder.append(o.substring(1))
                        .append(" text,");
            } else if (o.startsWith("0")) {
                builder.append(o.substring(1))
                        .append(" int,");
            } else if (o.startsWith("2")) {
                builder.append("id integer PRIMARY KEY AUTOINCREMENT,");
            } else if (o.startsWith("3")) {
                builder.append(o.substring(1))
                        .append(" real,");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        return "create table if not exists " + tableName + "(" + builder.toString() + ")";
    }

    /**
     * 获得删表语句
     */
    public static String getDropTableSql(Class<?> entityType) {
        return "drop table if exists " + getTableName(entityType);
    }

    /**
     * 获得更新语句
     */
    public static String getUpdateSql(Object entity) {
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
                    if (object != null) {
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = column.encryption().newInstance().encrypt(value);
                        }
                        columns.put(columnName, value);
                    } else {
                        columns.put(columnName, null);
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
        StringBuilder keys = new StringBuilder();
        for (String o : columns.keySet()) {
            if (columns.get(o) == null) {
                keys.append(o)
                        .append("= null ,");
            } else {
                keys.append(o)
                        .append("='")
                        .append(columns.get(o).replace("'", "''"))
                        .append("',");
            }
        }
        keys.deleteCharAt(keys.length() - 1);
        sql = "update " + tableName + " set " + keys.toString() + "where id = " + id_pry;
        return sql;
    }

    /**
     * 获得插入语句
     */
    public static String getInsertSql(Object entity) {
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
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = column.encryption().newInstance().encrypt(value);
                        }
                        columns.put(columnName, value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        StringBuilder keys = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (String o : columns.keySet()) {
            keys.append(o)
                    .append(",");
            values.append("'")
                    .append(columns.get(o).replace("'", "''"))
                    .append("',");
        }
        keys.deleteCharAt(keys.length() - 1);
        values.deleteCharAt(values.length() - 1);
        return "insert into " + tableName + "(" + keys.toString() + ")" + " values " + "(" + values.toString() + ")";
    }

    /**
     * 获得删除语句
     */
    public static String getdeleteSql(Object entity) {
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
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = column.encryption().newInstance().encrypt(value);
                        }
                        columns.put(columnName, value);
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
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        StringBuilder keys = new StringBuilder();
        for (String o : columns.keySet()) {
            keys.append(o)
                    .append("='")
                    .append(columns.get(o).replace("'", "''"))
                    .append("' and ");
        }
        sql = "delete from " + tableName + " where " + keys.toString().substring(0, keys.toString().length() - 5);
        return sql;
    }

    /**
     * 查询sql数据到对象中
     */
    public static void find(Object entity, Cursor cursor) {
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
                    Object type = field.get(entity);
                    String result = cursor.getString(cursor.getColumnIndex(columnName));
                    if (result != null && column.encryption() != Encryption.class) {
                        result = column.encryption().newInstance().decrypt(result);
                    }
                    if (type instanceof Integer) {
                        field.set(entity, Integer.parseInt(result));
                    } else if (type instanceof Long) {
                        field.set(entity, Long.parseLong(result));
                    } else if (type instanceof Float) {
                        field.set(entity, Float.parseFloat(result));
                    } else if (type instanceof Double) {
                        field.set(entity, Double.parseDouble(result));
                    } else {
                        field.set(entity, result);
                    }
                }
                Id id = (Id) field.getAnnotation(Id.class);
                if (id != null) {
                    String result = cursor.getString(cursor.getColumnIndex("id"));
                    Object type = field.get(entity);
                    if (type instanceof Integer) {
                        field.set(entity, Integer.parseInt(result));
                    } else {
                        field.set(entity, Long.parseLong(result));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
