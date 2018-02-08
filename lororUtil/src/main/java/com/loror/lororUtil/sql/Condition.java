package com.loror.lororUtil.sql;

/**
 * Created by Loror on 2018/2/8.
 */

public class Condition {
    private String key;
    private String operator;
    private String column;

    public Condition(String key, String operator, String column) {
        this.key = key;
        this.operator = operator;
        this.column = column;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    @Override
    public String toString() {
        return key + " " + operator + " '" + column + "'";
    }
}
