package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.Responce;

public class ResultException extends RuntimeException {

    private Responce responce;

    public ResultException(Responce responce) {
        this.responce = responce;
    }

    public Responce getResponce() {
        return responce;
    }
}
