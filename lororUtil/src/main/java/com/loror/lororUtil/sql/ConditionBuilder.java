package com.loror.lororUtil.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Loror on 2018/2/8.
 */

public class ConditionBuilder {

    private List<Condition> conditions = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private Page page;
    private boolean hasNull;

    private ConditionBuilder() {

    }

    public static ConditionBuilder create() {
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

    public List<Order> getOrderList() {
        return orders;
    }

    public boolean isHasNull() {
        return hasNull;
    }

    /**
     * 分页
     */
    public ConditionBuilder withPagination(int page, int number) {
        this.page = new Page(page, number);
        return this;
    }

    /**
     * 排序条件条件
     */
    public ConditionBuilder withOrder(String key, int orderType) {
        orders.add(new Order(key, orderType));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, String operator, Object column) {
        return addCondition(key, operator, column, true);
    }

    /**
     * list条件拼接
     */
    private String getListCondition(List<?> columns) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < columns.size(); i++) {
            Object column = columns.get(i);
            if (column != null) {
                builder.append("'")
                        .append(ColumnFilter.safeColumn(columns.get(i)))
                        .append("'");
                if (i != columns.size() - 1) {
                    builder.append(",");
                }
            }
        }
        builder.append(")");
        return builder.toString();
    }

    /**
     * 增加in条件
     */
    public ConditionBuilder addInCondition(String key, String operator, List<?> columns) {
        if (columns.size() == 1) {
            return addCondition(key, "not in".equalsIgnoreCase(operator) ? "<>" : "=", columns.get(0), true);
        } else {
            return addCondition(key, operator, getListCondition(columns), false);
        }
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, String operator, Object column, boolean quotation) {
        if (column == null) {
            hasNull = true;
        }
        conditions.add(new Condition(key, operator, column, 0, quotation));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addOrCondition(String key, String operator, Object column) {
        return addOrCondition(key, operator, column, true);
    }

    /**
     * 增加in条件
     */
    public ConditionBuilder addOrInCondition(String key, String operator, List<?> columns) {
        if (columns.size() == 1) {
            return addOrCondition(key, "not in".equalsIgnoreCase(operator) ? "<>" : "=", columns.get(0), true);
        } else {
            return addOrCondition(key, operator, getListCondition(columns), false);
        }
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addOrCondition(String key, String operator, Object column, boolean quotation) {
        if (column == null) {
            hasNull = true;
        }
        conditions.add(new Condition(key, operator, column, 1, quotation));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(Condition condition, boolean hasNull) {
        if (hasNull) {
            this.hasNull = true;
        }
        conditions.add(condition);
        return this;
    }

    /**
     * 获取排序语句
     */
    public String getOrders() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < orders.size(); i++) {
            if (i == 0) {
                builder.append(orders.get(i).toString());
            } else {
                builder.append(orders.get(i).toString().replace("order by ", ","));
            }
        }
        return builder.toString();
    }

    /**
     * 获取条件语句
     */
    public String getConditions(boolean withColumn) {
        return getConditionsWithoutPage(withColumn) + (page == null ? "" : " " + page.toString());
    }

    /**
     * 获取条件语句
     */
    public String getConditionsWithoutPage(boolean withColumn) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(conditions.get(i).getType() == 0 ? " and " : " or ");
            } else {
                builder.append(" where ");
            }
            builder.append(conditions.get(i).toString(withColumn));
        }
        String order = getOrders();
        if (order.length() > 0) {
            builder.append(" ");
            builder.append(order);
        }
        return builder.toString();
    }

    /**
     * 获取条件值数组
     */
    public String[] getColumnArray() {
        List<String> array = new ArrayList<>();
        if (conditions.size() > 0) {
            add(array, conditions);
        }
        return array.toArray(new String[0]);
    }

    private void add(List<String> array, List<Condition> conditions) {
        for (int i = 0; i < conditions.size(); i++) {
            Condition condition = conditions.get(i);
            array.add(condition.getColumn());
            if (condition.getConditions() != null) {
                add(array, condition.getConditions());
            }
        }
    }

    @Override
    public String toString() {
        return getConditions(true);
    }
}
