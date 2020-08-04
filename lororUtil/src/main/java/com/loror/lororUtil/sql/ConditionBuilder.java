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
    private String getListCondition(List columns) {
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
    public ConditionBuilder addInCondition(String key, String operator, List columns) {
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
        conditions.add(new Condition(key, operator, column == null ? null : String.valueOf(column), 0, quotation));
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
    public ConditionBuilder addOrInCondition(String key, String operator, List columns) {
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
        conditions.add(new Condition(key, operator, column == null ? null : String.valueOf(column), 1, quotation));
        return this;
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withCondition(String key, String operator, Object column) {
        return withCondition(key, operator, column, true);
    }

    /**
     * 追加增加in条件
     */
    public ConditionBuilder withInCondition(String key, String operator, List columns) {
        if (columns.size() == 1) {
            return withCondition(key, "not in".equalsIgnoreCase(operator) ? "<>" : "=", columns.get(0), true);
        } else {
            return withCondition(key, operator, getListCondition(columns), false);
        }
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withCondition(String key, String operator, Object column, boolean quotation) {
        if (conditions.size() > 0) {
            if (column == null) {
                hasNull = true;
            }
            Condition condition = conditions.get(conditions.size() - 1);
            condition.addCondition(new Condition(key, operator, column == null ? null : String.valueOf(column), 0, quotation));
        }
        return this;
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withOrCondition(String key, String operator, Object column) {
        return withOrCondition(key, operator, column, true);
    }

    /**
     * 追加增加in条件
     */
    public ConditionBuilder withOrInCondition(String key, String operator, List columns) {
        if (columns.size() == 1) {
            return withOrCondition(key, "not in".equalsIgnoreCase(operator) ? "<>" : "=", columns.get(0), true);
        } else {
            return withOrCondition(key, operator, getListCondition(columns), false);
        }
    }

    /**
     * 追加条件
     */
    public ConditionBuilder withOrCondition(String key, String operator, Object column, boolean quotation) {
        if (conditions.size() > 0) {
            if (column == null) {
                hasNull = true;
            }
            Condition condition = conditions.get(conditions.size() - 1);
            condition.addCondition(new Condition(key, operator, column == null ? null : String.valueOf(column), 1, quotation));
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
