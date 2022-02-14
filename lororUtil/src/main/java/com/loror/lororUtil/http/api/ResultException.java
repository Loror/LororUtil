package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.Responce;

public class ResultException extends RuntimeException {

    private final Responce responce;

    public ResultException(Responce responce) {
        super(responce.getThrowable() != null ? responce.getThrowable().getMessage() : null);
        this.responce = responce;
    }

    public Responce getResponce() {
        return responce;
    }
}
