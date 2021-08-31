package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.Responce;

public class Call<T> {

    protected T data;
    protected Responce responce;

    public T getData() {
        return data;
    }

    public Responce getResponce() {
        return responce;
    }
}
