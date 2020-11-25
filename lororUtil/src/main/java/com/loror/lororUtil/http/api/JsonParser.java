package com.loror.lororUtil.http.api;

public interface JsonParser {
    Object jsonToObject(String json, TypeInfo typeInfo);

    String objectToJson(Object object);
}
