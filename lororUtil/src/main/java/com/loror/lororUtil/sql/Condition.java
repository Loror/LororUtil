package com.loror.lororUtil.sql;

/**
 * Created by Loror on 2018/2/8.
 */

public class Condition {
    private String key;
    private String operator;
    private String column;
    private int type;//0,and.1,or
    private Condition condition;

    public Condition(String key, String operator, String column) {
        this.key = key;
        this.operator = operator;
        this.column = column;
    }

    public Condition(String key, String operator, String column, int type) {
        this.key = key;
        this.operator = operator;
        this.column = column == null ? null : column.replace("'", "''");
        this.type = type;
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
        this.column = column == null ? null : column.replace("'", "''");
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void addCondition(Condition condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            this.condition.addCondition(condition);
        }
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Condition that = condition;
        if (that == null) {
            builder.append(key);
            builder.append(" ");
            builder.append(operator);
            builder.append(" '");
            builder.append(column);
            builder.append("'");
        } else {
            builder.append("(");
            builder.append(key);
            builder.append(" ");
            builder.append(operator);
            builder.append(" '");
            builder.append(column);
            builder.append("'");
            do {
                builder.append(that.getType() == 0 ? " and " : " or ");
                builder.append(that.getKey());
                builder.append(" ");
                builder.append(that.getOperator());
                builder.append(" '");
                builder.append(that.getColumn());
                builder.append("'");
            } while ((that = that.getCondition()) != null);
            builder.append(")");
        }
        return builder.toString();
    }
}
