package com.loror.lororUtil.http.api;

import android.support.annotation.Nullable;

import com.loror.lororUtil.annotation.BaseUrl;
import com.loror.lororUtil.annotation.DELETE;
import com.loror.lororUtil.annotation.GET;
import com.loror.lororUtil.annotation.KeepStream;
import com.loror.lororUtil.annotation.MOCK;
import com.loror.lororUtil.annotation.POST;
import com.loror.lororUtil.annotation.PUT;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ApiTask {

    private final ApiClient apiClient;
    private final ApiRequest apiRequest;
    private final Type returnType;
    private Observable observable;

    public ApiTask(ApiClient apiClient, Class<?> service, Method method, @Nullable Object[] args) {
        this.apiClient = apiClient;
        BaseUrl baseUrl = service.getAnnotation(BaseUrl.class);
        final String anoBaseUrl;
        if (baseUrl != null) {
            anoBaseUrl = baseUrl.value();
        } else {
            anoBaseUrl = null;
        }
        Class<?> declaringClass = method.getDeclaringClass();
        returnType = method.getGenericReturnType();
        apiRequest = getApiRequest(method);
        if (apiRequest.getType() != 0) {
            apiRequest.setAnoBaseUrl(anoBaseUrl);
            apiRequest.generateParams(method, args);
            apiRequest.apiName = declaringClass.getName() + "." + method.getName();
        }
    }

    /**
     * 获取请求参数
     */
    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    /**
     * task执行
     * 返回 正常情况为解析对象结果 出现异常时为Throwable 无效请求返回null
     */
    public Object execute() {
        if (apiRequest.getType() != 0) {
            Responce responce = request();
            return toResult(responce);
        } else {
            return null;
        }
    }

    /**
     * 请求重试
     */
    protected void requestAgain(ApiResult apiResult) {
        if (observable != null) {
            apiResult.accept = true;
            observable.subscribe(observable.getObserver());
        } else if (ApiClient.returnAdapters.size() == 0) {
            apiResult.accept = true;
            Responce responce = request();
            apiResult.responceObject = toResult(responce);
        }
    }

    /**
     * 获取ApiRequest
     */
    private ApiRequest getApiRequest(Method method) {
        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setBaseUrl(apiClient.baseUrl);
        GET get = method.getAnnotation(GET.class);
        if (get != null) {
            apiRequest.setType(1);
            apiRequest.setUrl(get.value());
        } else {
            POST post = method.getAnnotation(POST.class);
            if (post != null) {
                apiRequest.setType(2);
                apiRequest.setUrl(post.value());
            } else {
                DELETE delete = method.getAnnotation(DELETE.class);
                if (delete != null) {
                    apiRequest.setType(3);
                    apiRequest.setUrl(delete.value());
                } else {
                    PUT put = method.getAnnotation(PUT.class);
                    if (put != null) {
                        apiRequest.setType(4);
                        apiRequest.setUrl(put.value());
                    }
                }
            }
        }
        KeepStream stream = method.getAnnotation(KeepStream.class);
        if (stream != null) {
            apiRequest.setKeepStream(true);
        }
        MOCK mock = method.getAnnotation(MOCK.class);
        if (mock != null && mock.enable()) {
            apiRequest.mockType = mock.type();
            apiRequest.mockData = mock.value();
        }
        return apiRequest;
    }

    /**
     * 同步请求
     */
    public Responce request() {
        //使用mock数据
        if (apiClient.mockEnable && apiRequest.mockData != null) {
            MockData mockData = new MockData(apiRequest, this);
            String result = mockData.getResult();
            Responce responce = mockData.getResponce();
            if (responce == null) {
                responce = new Responce();
                try {
                    Field field = Responce.class.getDeclaredField("code");
                    field.setAccessible(true);
                    field.set(responce, 200);
                } catch (Exception ignore) {
                }
                responce.result = result == null ? null : result.getBytes();
            }
            return responce;
        }
        return connect();
    }

    /**
     * 连接网络
     */
    protected Responce connect() {
        final TypeInfo typeInfo = new TypeInfo(returnType);
        ++apiRequest.useTimes;
        final HttpClient client = new HttpClient();
        if (apiClient.onRequestListener != null) {
            apiClient.onRequestListener.onRequestBegin(client, apiRequest);
        }
        final RequestParams params = apiRequest.getParams();
        final String url = apiRequest.getUrl();
        if (apiRequest.isKeepStream() && typeInfo.getType() == Responce.class) {
            client.setKeepStream(true);
        }
        apiRequest.client = client;
        client.setProgressListener(apiRequest.progressListener);
        int type = apiRequest.getType();
        Responce responce;
        if (type == 1) {
            responce = client.get(url, params);
        } else if (type == 2) {
            responce = client.post(url, params);
        } else if (type == 3) {
            responce = client.delete(url, params);
        } else if (type == 4) {
            responce = client.put(url, params);
        } else {
            return null;
        }
        if (apiClient.onRequestListener != null) {
            ApiResult apiResult = new ApiResult();
            apiResult.url = url;
            apiResult.apiRequest = apiRequest;
            apiResult.responce = responce;
            apiResult.apiTask = ApiTask.this;
            apiClient.onRequestListener.onRequestEnd(client, apiResult);
            //事务拦截
            if (apiResult.accept) {
                //responce被重写
                if (apiResult.responce != responce) {
                    responce = apiResult.responce;
                } else {
                    Object acceptObject = apiResult.responceObject;
                    //设置了拦截返回数据则返回
                    if (acceptObject != null) {
                        String result = ApiClient.objectToString(acceptObject);
                        responce = new Responce();
                        try {
                            Field field = Responce.class.getDeclaredField("code");
                            field.setAccessible(true);
                            field.set(responce, 200);
                        } catch (Exception ignore) {
                        }
                        responce.result = result == null ? null : result.getBytes();
                        return responce;
                    }
                    return null;
                }
            }
        }
        return responce;
    }

    /**
     * responce转对象
     */
    public Object toResult(Responce responce) {
        if (responce == null) {
            return null;
        }
        final TypeInfo typeInfo = new TypeInfo(returnType);
        return result(responce, typeInfo);
    }

    /**
     * 处理返回结果
     */
    private Object result(Responce responce, TypeInfo typeInfo) {
        Type classType = typeInfo.getType();
        //Responce类型无需判断
        if (classType == Responce.class) {
            return responce;
        }
        //包含异常抛出异常
        if (responce.getThrowable() != null) {
            return new ResultException(responce);
        }
        //优先外部筛选器通过尝试解析，否则200系列解析
        if (apiClient.codeFilter != null ? apiClient.codeFilter.isSuccessCode(responce.getCode()) : responce.getCode() / 100 == 2) {
             //String类型优先拦截
            if (classType == String.class) {
                return apiClient.charset == null ? responce.toString() : new String(responce.result, apiClient.charset);
            }
            try {
                return ApiClient.jsonToObject((apiClient.charset == null ? responce.toString() : new String(responce.result, apiClient.charset)), typeInfo);
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            }
        } else {
            return new ResultException(responce);
        }
    }

    protected void setObservable(Observable<?> observable) {
        this.observable = observable;
    }

    protected Observable<?> getObservable() {
        return observable;
    }

    public Type getReturnType() {
        return returnType;
    }
}
