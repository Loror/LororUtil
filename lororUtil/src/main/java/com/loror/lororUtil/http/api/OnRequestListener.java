package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.HttpClient;

public interface OnRequestListener {
    void onRequestBegin(HttpClient client, ApiRequest request);

    void onRequestEnd(HttpClient client, ApiResult result);
}
