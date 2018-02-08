package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Loror on 2018/2/8.
 */

public class ConditionBuilder {

    private List<Condition> conditions = new ArrayList<>();
    private Order order;

    private ConditionBuilder() {

    }

    public static ConditionBuilder builder() {
        return new ConditionBuilder();
    }

    /**
     * 获取条件数量
     */
    public int getConditionCount() {
        return conditions.size();
    }

    public List<Condition> getConditionList() {
        return conditions;
    }

    public Order getOrder() {
        return order;
    }

    /**
     * 排序条件条件
     */
    public ConditionBuilder withOrder(String key, int orderType) {
        order = new Order(key, orderType);
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addIdCondition(Object column) {
        conditions.add(new Condition("id", "=", String.valueOf(column)));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addIdCondition(String operator, Object column) {
        conditions.add(new Condition("id", operator, String.valueOf(column)));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, Object column) {
        conditions.add(new Condition(key, "=", String.valueOf(column)));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, String operator, Object column) {
        conditions.add(new Condition(key, operator, String.valueOf(column)));
        return this;
    }

    /**
     * 获取条件语句
     */
    public String getConditions() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(" and ");
            }
            builder.append(conditions.get(i).toString());
        }
        if (order != null) {
            builder.append(" ");
            builder.append(order.toString());
        }
        return builder.toString();
    }

    /**
     * 获取条件语句
     */
    public String getNoColumnConditions() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(" and ");
            }
            builder.append(conditions.get(i).getKey());
            builder.append(" ");
            builder.append(conditions.get(i).getOperator());
            builder.append(" ?");
        }
        if (order != null) {
            builder.append(" ");
            builder.append(order.toString());
        }
        return builder.toString();
    }

    /**
     * 获取条件值数组
     */
    public String[] getColumnArray() {
        String[] array = null;
        if (conditions.size() > 0) {
            array = new String[conditions.size()];
            for (int i = 0; i < conditions.size(); i++) {
                array[i] = conditions.get(i).getColumn();
            }
        }
        return array;
    }
}
