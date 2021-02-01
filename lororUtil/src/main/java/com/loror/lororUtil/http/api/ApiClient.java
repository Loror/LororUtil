package com.loror.lororUtil.http.api;

import android.support.annotation.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ApiClient {

    protected static JsonParser jsonParser;
    protected static List<ReturnAdapter> returnAdapters = new ArrayList<>();

    protected OnRequestListener onRequestListener;
    protected CodeFilter codeFilter;
    protected Charset charset;
    protected boolean mockEnable = true;
    protected String baseUrl;

    /**
     * 设置是否开启mock功能
     */
    public ApiClient setMockEnable(boolean mockEnable) {
        this.mockEnable = mockEnable;
        return this;
    }

    /**
     * 设置baseUrl
     */
    public ApiClient setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public ApiClient setOnRequestListener(OnRequestListener onRequestListener) {
        this.onRequestListener = onRequestListener;
        return this;
    }

    public ApiClient setCodeFilter(CodeFilter codeFilter) {
        this.codeFilter = codeFilter;
        return this;
    }

    public ApiClient setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * 自定义json处理器
     */
    public static void setJsonParser(JsonParser jsonParser) {
        ApiClient.jsonParser = jsonParser;
    }

    public static JsonParser getJsonParser() {
        return jsonParser;
    }

    /**
     * 自定义请求筛选处理器
     */
    public static void addReturnAdapter(ReturnAdapter returnAdapter) {
        ApiClient.returnAdapters.add(returnAdapter);
    }

    public static List<ReturnAdapter> getReturnAdapters() {
        return returnAdapters;
    }

    /**
     * 创建Api对象
     */
    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, @Nullable Object[] args)
                            throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        Class<?> declaringClass = method.getDeclaringClass();
                        if (declaringClass == Object.class) {
                            return method.invoke(this, args);
                        }
                        return makeReturnData(service, method, args);
                    }
                });
    }

    /**
     * 获取方法返回对象
     */
    private Object makeReturnData(Class<?> service, Method method, @Nullable Object[] args) {
        ApiTask apiTask = new ApiTask(ApiClient.this, service, method, args);
        if (method.getReturnType() == Observable.class) {
            Observable<?> observable = new Observable<>();
            observable.setApiTask(apiTask);
            observable.setApiRequest(apiTask.getApiRequest());
            apiTask.setObservable(observable);
            return observable;
        }
        if (returnAdapters.size() > 0) {
            for (ReturnAdapter returnAdapter : returnAdapters) {
                if (returnAdapter.filterType(method.getGenericReturnType(), method.getReturnType())) {
                    return returnAdapter.returnAdapter(apiTask);
                }
            }
        }
        return apiTask.execute();
    }

    /**
     * 对象转字符串
     */
    protected static String objectToString(Object object) {
        if (jsonParser != null) {
            return jsonParser.objectToJson(object);
        } else {
            return String.valueOf(object);
        }
    }

    /**
     * json转对象
     */
    protected static Object jsonToObject(String json, TypeInfo typeInfo) {
        return ApiClient.jsonParser == null ? null : ApiClient.jsonParser.jsonToObject(json, typeInfo);
    }

}
