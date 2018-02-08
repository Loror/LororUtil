package com.loror.lororUtil.sql;

/**
 * Created by Loror on 2018/2/8.
 */

public class Order {
    public static int ORDER_DESC = 0;
    public static int ORDER_ASC = 1;

    private String key;
    private int orderType;

    public Order(String key, int orderType) {
        this.key = key;
        this.orderType = orderType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    @Override
    public String toString() {
        return "order by " + key + (orderType == ORDER_DESC ? " desc" : " asc");
    }
}
