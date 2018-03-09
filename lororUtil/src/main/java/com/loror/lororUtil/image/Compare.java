package com.loror.lororUtil.image;

import com.loror.lororUtil.sql.Column;
import com.loror.lororUtil.sql.Id;
import com.loror.lororUtil.sql.Table;

@Table(name = "imageCompare")
public class Compare {
    @Id
    int id;
    @Column
    String url;
    @Column
    String path;
    @Column
    long length;
}
