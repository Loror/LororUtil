package com.loror.lororUtil.http.api;

public interface Observer<T> {
    void success(T data);

    void failed(int code, Throwable e);
}
