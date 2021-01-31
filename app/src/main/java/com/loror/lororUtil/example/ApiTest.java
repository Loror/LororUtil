package com.loror.lororUtil.example;

import com.loror.lororUtil.annotation.BaseUrl;
import com.loror.lororUtil.annotation.GET;
import com.loror.lororUtil.http.api.Observable;

@BaseUrl("http://www.baidu.com")
public interface ApiTest {

    /**
     * 内置异步访问方式
     */
    @GET("/")
    Observable<String> test();

    /**
     * 同步访问方式
     */
    @GET("/")
    String test1();

    /**
     * 自定义访问方式
     */
    @GET("/")
    rx.Observable<String> test2();
}
