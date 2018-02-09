package com.loror.lororUtil.sql;

/**
 * Created by Loror on 2018/2/9.
 */

public class Page {
    private int page;
    private int number;

    public Page(int page, int number) {
        this.page = page;
        this.number = number;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "limit " + ((page - 1) * number) + "," + number;
    }
}
