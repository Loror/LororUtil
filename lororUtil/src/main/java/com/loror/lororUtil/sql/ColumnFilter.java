package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

public class ColumnFilter {

    private static List<Encryption> encryptions = new ArrayList<>();

    protected static Encryption getEncryption(Class<? extends Encryption> encryptionType) {
        Encryption encryption = null;
        for (int i = 0; i < encryptions.size(); i++) {
            if (encryptions.get(i).getClass() == encryptionType) {
                encryption = encryptions.get(i);
                break;
            }
        }
        if (encryption == null) {
            try {
                encryption = encryptionType.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException(encryptionType.getSimpleName() + " have no non parametric constructor");
            }
        }
        if (encryptions.size() > 10) {
            encryptions.remove(0);
        }
        encryptions.add(encryption);
        return encryption;
    }

    /**
     * 获取Column
     */
    public static String getColumn(String name, Object var, Column column) {
        if (var != null) {
            String value = String.valueOf(var);
            if (column.encryption() != Encryption.class) {
                value = getEncryption(column.encryption()).encrypt(value);
            }
            return value;
        } else {
            String defaultValue = column.defaultValue();
            if (column.notNull() && defaultValue.length() == 0) {
                throw new NullPointerException("column " + name + " can not be null");
            }
            if (defaultValue.length() != 0 && column.encryption() != Encryption.class) {
                defaultValue = getEncryption(column.encryption()).encrypt(defaultValue);
            }
            return defaultValue.length() == 0 ? null : defaultValue;
        }
    }

    /**
     * 获取Column
     */
    public static String decodeColumn(Object var, Column column) {
        if (var != null) {
            String value = String.valueOf(var);
            if (column.encryption() != Encryption.class) {
                value = getEncryption(column.encryption()).decrypt(value);
            }
            return value;
        }
        return null;
    }

    /**
     * 安全处理
     */
    public static String safeColumn(Object column) {
        if (column == null) {
            return null;
        }
        return column.toString().replace("'", "''");
    }
}
