package com.loror.lororUtil.sql;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        int mainCount = 0;
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.name();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Class<?> type = field.getType();
                    if (type == int.class || type == long.class || type == Integer.class || type == Long.class) {
                        columns.add(0 + columnName);
                    } else if (type == float.class || type == double.class || type == Float.class || type == Double.class) {
                        columns.add(3 + columnName);
                    } else if (type == String.class) {
                        columns.add(1 + columnName);
                    }
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        Class<?> type = field.getType();
                        if (type == int.class || type == long.class || type != Integer.class && type != Long.class) {
                            throw new IllegalStateException("PRIMARY KEY must be Integer or Long");
                        }
                        columns.add(2 + "id");
                        mainCount++;
                    }
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
                    String columnName = column.name();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object != null) {
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = getEncryption(column.encryption()).encrypt(value);
                        }
                        columns.put(columnName, value);
                    } else {
                        if (column.notNull() && "".equals(column.defaultValue())) {
                            throw new NullPointerException("column " + columnName + " can not be null");
                        }
                        columns.put(columnName, "".equals(column.defaultValue()) ? null : column.defaultValue());
                    }
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        Object object = field.get(entity);
                        id_pry = object.toString();
                    }
                }
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    throw ((NullPointerException) e);
                }
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
                    String columnName = column.name();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object != null) {
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = getEncryption(column.encryption()).encrypt(value);
                        }
                        columns.put(columnName, value);
                    } else {
                        if (column.notNull()) {
                            if ("".equals(column.defaultValue())) {
                                throw new NullPointerException("column " + columnName + " can not be null");
                            } else {
                                columns.put(columnName, column.defaultValue());
                            }
                        }

                    }
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        Object object = field.get(entity);
                        if (object != null) {
                            long idValue = Long.parseLong(object.toString());
                            if (idValue > 0) {
                                columns.put("id", object.toString());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (e instanceof NullPointerException) {
                    throw ((NullPointerException) e);
                }
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
                    String columnName = column.name();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Object object = field.get(entity);
                    if (object != null) {
                        String value = String.valueOf(object);
                        if (column.encryption() != Encryption.class) {
                            value = getEncryption(column.encryption()).encrypt(value);
                        }
                        columns.put(columnName, value);
                    }
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        Object object = field.get(entity);
                        columns.put("id", object.toString());
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
                    String columnName = column.name();
                    if ("".equals(columnName)) {
                        columnName = field.getName();
                    }
                    Class<?> type = field.getType();
                    String result = cursor.getString(cursor.getColumnIndex(columnName));
                    if (result != null && column.encryption() != Encryption.class) {
                        result = getEncryption(column.encryption()).decrypt(result);
                    }
                    if (type == int.class || type == Integer.class) {
                        field.set(entity, Integer.parseInt(result));
                    } else if (type == long.class || type == Long.class) {
                        field.set(entity, Long.parseLong(result));
                    } else if (type == float.class || type == Float.class) {
                        field.set(entity, Float.parseFloat(result));
                    } else if (type == double.class || type == Double.class) {
                        field.set(entity, Double.parseDouble(result));
                    } else if (type == String.class) {
                        field.set(entity, result);
                    }
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        String result = cursor.getString(cursor.getColumnIndex("id"));
                        Class<?> type = field.getType();
                        if (type == int.class || type == Integer.class) {
                            field.set(entity, Integer.parseInt(result));
                        } else {
                            field.set(entity, Long.parseLong(result));
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<Encryption> encryptions = new ArrayList<>();

    private static Encryption getEncryption(Class<? extends Encryption> encryptionType) throws Exception {
        Encryption encryption = null;
        for (int i = 0; i < encryptions.size(); i++) {
            if (encryptions.get(i).getClass() == encryptionType) {
                encryption = encryptions.get(i);
                break;
            }
        }
        if (encryption == null) {
            encryption = encryptionType.newInstance();
        }
        if (encryptions.size() > 10) {
            encryptions.remove(0);
        }
        encryptions.add(encryption);
        return encryption;
    }
}
