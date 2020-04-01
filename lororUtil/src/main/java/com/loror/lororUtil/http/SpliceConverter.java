package com.loror.lororUtil.http;

public interface SpliceConverter {

    //type 0，首次拼接，1‘=’替换位,2‘&’替换位
    String convert(String baseUrl, int type);
}
