package com.loror.lororUtil.example.net;

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
    @GET("/s?wd=1")
    String test1();

    /**
     * 自定义访问方式
     */
    @GET("/s?wd=2")
    rx.Observable<String> test2();
}
