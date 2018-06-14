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
        String tableName = null;
        Table table = (Table) entityType.getAnnotation(Table.class);
        if (table == null) {
            throw new IllegalStateException("this object does not define table");
        }
        tableName = table.name();
        if (tableName.length() == 0) {
            tableName = entityType.getSimpleName();
        }
        return tableName;
    }

    /**
     * 获得创建语句
     */
    public static String getCreateSql(Class<?> entityType) {
        String tableName = getTableName(entityType);
        Field[] fields = entityType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any field");
        }
        int mainCount = 0;
        int columnCount = 0;
        StringBuilder builder = new StringBuilder();
        builder.append("create table if not exists ")
                .append(tableName)
                .append("(");
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.name();
                    if (columnName.length() == 0) {
                        columnName = field.getName();
                    }
                    Class<?> type = field.getType();
                    if (type == int.class || type == long.class || type == Integer.class || type == Long.class) {
                        builder.append(columnName)
                                .append(" int,");
                    } else if (type == float.class || type == double.class || type == Float.class || type == Double.class) {
                        builder.append(columnName)
                                .append(" real,");
                    } else if (type == String.class) {
                        builder.append(columnName)
                                .append(" text,");
                    }
                    columnCount++;
                } else {
                    Id id = (Id) field.getAnnotation(Id.class);
                    if (id != null) {
                        Class<?> type = field.getType();
                        if (type == int.class || type == long.class || type != Integer.class && type != Long.class) {
                            throw new IllegalStateException("primary key must be Integer or Long");
                        }
                        String idName = id.name();
                        builder.append(idName.length() == 0 ? "id" : idName)
                                .append(" integer PRIMARY KEY AUTOINCREMENT,");
                        mainCount++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columnCount == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        if (mainCount > 1) {
            throw new IllegalStateException("cannot contain more than 1 primary key");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(")");
        return builder.toString();
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
        String tableName = getTableName(entity.getClass());
        HashMap<String, String> columns = new HashMap<>();
        String idName = "id";
        String idVolume = "0";
        Class<?> handlerType = entity.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        if (fields == null) {
            throw new IllegalStateException("this object does not contains any field");
        }
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                Column column = (Column) field.getAnnotation(Column.class);
                if (column != null) {
                    String columnName = column.name();
                    if (columnName.length() == 0) {
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
                        String name = id.name();
                        if (name.length() > 0) {
                            idName = name;
                        }
                        idVolume = object.toString();
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
        StringBuilder builder = new StringBuilder();
        builder.append("update ")
                .append(tableName)
                .append(" set ");
        for (String o : columns.keySet()) {
            if (columns.get(o) == null) {
                builder.append(o)
                        .append("= null ,");
            } else {
                builder.append(o)
                        .append("='")
                        .append(columns.get(o).replace("'", "''"))
                        .append("',");
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append(" where ")
                .append(idName)
                .append(" = ")
                .append(idVolume);
        return builder.toString();
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
                    if (columnName.length() == 0) {
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
                            if (column.defaultValue().length() == 0) {
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
                                String name = id.name();
                                columns.put(name.length() == 0 ? "id" : name, object.toString());
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
                    if (columnName.length() == 0) {
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
                        String name = id.name();
                        columns.put(name.length() == 0 ? "id" : name, String.valueOf(object));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (columns.size() == 0) {
            throw new IllegalStateException("this object does not contains any colume");
        }
        StringBuilder builder = new StringBuilder();
        builder.append("delete from ")
                .append(tableName)
                .append(" where ");
        for (String o : columns.keySet()) {
            builder.append(o)
                    .append("='")
                    .append(columns.get(o).replace("'", "''"))
                    .append("' and ");
        }
        return builder.toString().substring(0, builder.toString().length() - 5);
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
                    if (columnName.length() == 0) {
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
                        String name = id.name();
                        if (name.length() == 0) {
                            name = "id";
                        }
                        String result = cursor.getString(cursor.getColumnIndex(name));
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
