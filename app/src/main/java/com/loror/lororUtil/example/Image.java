package com.loror.lororUtil.example;

import com.loror.lororUtil.sql.Column;
import com.loror.lororUtil.sql.Id;
import com.loror.lororUtil.sql.Table;

@Table
public class Image {
    @Id
    public long id;
    @Column(notNull = true, encryption = Code.class)
    public String path;
    public String flag;

    @Override
    public String toString() {
        return "{id=" + id + ",path=" + path + ",flag=" + flag + "}";
    }
}
