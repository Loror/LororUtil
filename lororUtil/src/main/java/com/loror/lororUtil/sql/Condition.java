package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Loror on 2018/2/8.
 */

public class Condition {
    private String key;
    private String operator;
    private String column;
    private int type;//0,and.1,or
    private boolean quotation;
    private List<Condition> conditions;

    public Condition(String key, String operator, Object column) {
        this(key, operator, column, 0);
    }

    public Condition(String key, String operator, Object column, int type) {
        this(key, operator, column, type, true);
    }

    public Condition(String key, String operator, Object column, int type, boolean quotation) {
        this.key = key;
        this.operator = operator;
        if (quotation) {
            this.column = ColumnFilter.safeColumn(column);
        } else {
            this.column = column == null ? null : String.valueOf(column);
        }
        this.type = type;
        this.quotation = quotation;
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
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean withColumn) {
        StringBuilder builder = new StringBuilder();
        if (conditions != null) {
            builder.append("(");
        }
        builder.append(key);
        builder.append(" ");
        builder.append(operator);
        if (withColumn) {
            if (column == null) {
                builder.append(" null");
            } else {
                if (quotation) {
                    builder.append(" '");
                } else {
                    builder.append(" ");
                }
                builder.append(column);
                if (quotation) {
                    builder.append("'");
                }
            }
        } else {
            builder.append(" ?");
        }
        if (conditions != null) {
            int size = conditions.size();
            for (int i = 0; i < size; i++) {
                Condition condition = conditions.get(i);
                builder.append(condition.getType() == 0 ? " and " : " or ");
                builder.append(condition.toString(withColumn));
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
