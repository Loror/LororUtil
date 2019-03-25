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
    public ConditionBuilder addIdCondition(Object column) {
        return addIdCondition(column == null ? "is" : "=", column);
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addIdCondition(String operator, Object column) {
        if (column == null) {
            hasNull = true;
        }
        conditions.add(new Condition("id", operator, column == null ? null : String.valueOf(column)));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, Object column) {
        return addCondition(key, column == null ? "is" : "=", column);
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addCondition(String key, String operator, Object column) {
        if (column == null) {
            hasNull = true;
        }
        conditions.add(new Condition(key, operator, column == null ? null : String.valueOf(column)));
        return this;
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addOrCondition(String key, Object column) {
        return addOrCondition(key, column == null ? "is" : "=", column);
    }

    /**
     * 增加条件
     */
    public ConditionBuilder addOrCondition(String key, String operator, Object column) {
        if (column == null) {
            hasNull = true;
        }
        conditions.add(new Condition(key, operator, column == null ? null : String.valueOf(column), 1));
        return this;
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withCondition(String key, Object column) {
        return withCondition(key, column == null ? "is" : "=", column);
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withCondition(String key, String operator, Object column) {
        if (conditions.size() > 0) {
            if (column == null) {
                hasNull = true;
            }
            Condition condition = conditions.get(conditions.size() - 1);
            condition.addCondition(new Condition(key, operator, column == null ? null : String.valueOf(column)));
        }
        return this;
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withOrCondition(String key, Object column) {
        return withOrCondition(key, column == null ? "is" : "=", column);
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withOrCondition(String key, String operator, Object column) {
        if (conditions.size() > 0) {
            if (column == null) {
                hasNull = true;
            }
            Condition condition = conditions.get(conditions.size() - 1);
            condition.addCondition(new Condition(key, operator, column == null ? null : String.valueOf(column), 1));
        }
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
    public String getConditions() {
        return getConditionsWithoutPage() + (page == null ? "" : " " + page.toString());
    }

    /**
     * 获取条件语句
     */
    public String getConditionsWithoutPage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(conditions.get(i).getType() == 0 ? " and " : " or ");
            } else {
                builder.append(" where ");
            }
            builder.append(conditions.get(i).toString());
        }
        String order = getOrders();
        if (order.length() > 0) {
            builder.append(" ");
            builder.append(order);
        }
        return builder.toString();
    }

    /**
     * 获取条件语句
     */
    public String getNoColumnConditions() {
        return getNoColumnConditionsWithoutPage() + (page == null ? "" : " " + page.toString());
    }

    /**
     * 获取条件语句
     */
    public String getNoColumnConditionsWithoutPage() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < conditions.size(); i++) {
            if (i > 0) {
                builder.append(conditions.get(i).getType() == 0 ? " and " : " or ");
            } else {
                builder.append(" where ");
            }
            Condition that = conditions.get(i).getCondition();
            if (that != null) {
                builder.append("(");
            }
            builder.append(conditions.get(i).getKey());
            builder.append(" ");
            builder.append(conditions.get(i).getOperator());
            builder.append(" ?");
            if (that != null) {
                do {
                    builder.append(that.getType() == 0 ? " and " : " or ");
                    builder.append(that.getKey());
                    builder.append(" ");
                    builder.append(that.getOperator());
                    builder.append(" ?");
                } while ((that = that.getCondition()) != null);
                builder.append(")");
            }
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
            for (int i = 0; i < conditions.size(); i++) {
                Condition condition = conditions.get(i);
                array.add(condition.getColumn());
                while ((condition = condition.getCondition()) != null) {
                    array.add(condition.getColumn());
                }
            }
        }
        return array.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return getConditions();
    }
}
