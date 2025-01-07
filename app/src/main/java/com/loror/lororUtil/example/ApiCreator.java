package com.loror.lororUtil.example;

import com.alibaba.fastjson.JSON;
import com.loror.lororUtil.http.Client;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.api.ApiClient;
import com.loror.lororUtil.http.api.ApiRequest;
import com.loror.lororUtil.http.api.ApiResult;
import com.loror.lororUtil.http.api.JsonParser;
import com.loror.lororUtil.http.api.OnRequestListener;
import com.loror.lororUtil.http.api.TypeInfo;

public class ApiCreator {

    public static ApiClient getApiClient() {
        TypeInfo.setRawType(rx.Observable.class);
        ApiClient.setJsonParser(new JsonParser() {
            @Override
            public Object jsonToObject(String json, TypeInfo typeInfo) {
                return JSON.parseObject(json, typeInfo.getType());
            }

            @Override
            public String objectToJson(Object object) {
                return JSON.toJSONString(object);
            }
        });
        ApiClient.addReturnAdapter(new ObservableAdapter());
        return new ApiClient()
                .setOnRequestListener(new OnRequestListener() {
                    @Override
                    public void onRequestBegin(HttpClient client, ApiRequest request) {
                        client.setCore(Client.CORE_OKHTTP3);
                    }

                    @Override
                    public void onRequestEnd(HttpClient client, ApiResult result) {

                    }
                });
    }
}
