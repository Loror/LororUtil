package com.loror.lororUtil.http.api.helper;

import androidx.annotation.NonNull;

import com.loror.lororUtil.http.Primitive;

public class PrimitiveObject<T> implements Primitive {

    private final T data;

    public PrimitiveObject(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        if (data == null) {
            return "";
        }
        return data.toString();
    }
}
