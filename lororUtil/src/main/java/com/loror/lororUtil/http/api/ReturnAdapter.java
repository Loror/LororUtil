package com.loror.lororUtil.http.api;

import java.lang.reflect.Type;

public interface ReturnAdapter {

    boolean filterType(Type type, Class<?> rawType);

    Object returnAdapter(ApiTask apiTask);
}
