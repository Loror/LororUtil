package com.loror.lororUtil.example;

import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.sql.Column;
import com.loror.lororUtil.sql.Id;
import com.loror.lororUtil.sql.Table;

@Table
public class Test {
    @Id
    public long id;
    @Column(defaultValue = "xiaoming")
    public String name;
    @Column(defaultValue = "test")
    public String test;

    @Override
    public String toString() {
        return new RequestParams().fromObject(this).toString();
    }
}
