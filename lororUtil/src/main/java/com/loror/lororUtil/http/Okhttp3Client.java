package com.loror.lororUtil.http;

import com.loror.lororUtil.http.okhttp.ProgressRequestBody;
import com.loror.lororUtil.text.TextUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Okhttp3Client extends BaseClient {

    private okhttp3.Call call;
    private int core = CORE_URL_CONNECTION;

    @Override
    public boolean setCore(int core) {
        if (core == CORE_OKHTTP3) {
            try {
                Class<?> type = okhttp3.OkHttpClient.class;
                System.out.println("update core:" + type.getSimpleName());
            } catch (Throwable e) {
                e.printStackTrace();
                return false;
            }
        }
        this.core = core;
        return true;
    }

    @Override
    public Responce get(String urlStr, RequestParams params) {
        if (core == CORE_OKHTTP3) {
            Responce responce = new Responce();
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
            }

            Request.Builder builder = new Request.Builder().url(urlStr);
            builder.method("GET", null);
            if (params != null) {
                Map<String, String> paramsHeaders = params.getHeaders();
                for (Map.Entry<String, String> kv : paramsHeaders.entrySet()) {
                    builder.header(kv.getKey(), kv.getValue());
                }
            }
            Request request = builder.build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(timeOut / 1000, TimeUnit.SECONDS)
                    .build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                Headers responseHeaders = response.headers();
                if (responseHeaders != null) {
                    Map<String, List<String>> headers = new HashMap<>();
                    for (String name : responseHeaders.names()) {
                        List<String> value = responseHeaders.values(name);
                        headers.put(name, value);
                    }
                    initHeaders(headers, responce);
                }
                ResponseBody body = response.body();
                if (body != null) {
                    responce.result = body.bytes();
                }
                response.close();
            } catch (IOException e) {
                responce.setThrowable(e);
            }
            this.call = null;
            return responce;
        }
        return super.get(urlStr, params);
    }

    @Override
    public Responce post(String urlStr, RequestParams params) {
        if (core == CORE_OKHTTP3) {
            Responce responce = new Responce();
            boolean isMultipart = !(params == null || (params.getFiles().size() == 0 && !params.isForceMultiparty()));
            boolean queryParam = false;
            if (!isMultipart) {
                if (params != null) {
                    if ((params.isAsJson() && params.getJson() != null)
                            || params.isForceParamAsQueryForPostOrPut()) {
                        String strParams = params.packetOutParams("GET");
                        if (!TextUtil.isEmpty(strParams)) {
                            urlStr += params.getSplicing(urlStr, 0) + strParams;
                        }
                        queryParam = true;
                    }
                }
            } else {
                if (params.isForceParamAsQueryForPostOrPut()) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr, 0) + strParams;
                    }
                }
            }

            Request.Builder builder = new Request.Builder().url(urlStr);
            if (params != null) {
                Map<String, String> paramsHeaders = params.getHeaders();
                for (Map.Entry<String, String> kv : paramsHeaders.entrySet()) {
                    builder.header(kv.getKey(), kv.getValue());
                }
            }
            if (isMultipart) {
                MultipartBody.Builder body = new okhttp3.MultipartBody.Builder();
                for (Map.Entry<String, Object> kv : params.getParams().entrySet()) {
                    Object value = kv.getValue();
                    if (value instanceof StreamBody) {
                        StreamBody streamBody = (StreamBody) value;
                        if (value instanceof FileBody) {
                            FileBody fileBody = (FileBody) value;
                            body.addFormDataPart("attachments", streamBody.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), fileBody.getFile()));
                        } else {
                            body.addFormDataPart("attachments", streamBody.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), streamBody.getBytes()));
                        }
                    } else if (!params.isForceParamAsQueryForPostOrPut()) {
                        body.addFormDataPart(kv.getKey(), String.valueOf(kv.getValue()));
                    }
                }
                if (progressListener != null) {
                    builder.post(new ProgressRequestBody(body.build(), progressListener, callbackActuator));
                } else {
                    builder.post(body.build());
                }
            } else {
                if (params != null) {
                    if (params.getJson() != null || params.isAsJson()) {
                        String json = params.packetOutParams("POST");
                        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
                        builder.post(requestBody);
                    } else {
                        if (!queryParam) {
                            FormBody.Builder body = new FormBody.Builder();
                            for (Map.Entry<String, Object> kv : params.getParams().entrySet()) {
                                body.add(kv.getKey(), String.valueOf(kv.getValue()));
                            }
                            builder.post(body.build());
                        } else {
                            builder.method("POST", null);
                        }
                    }
                } else {
                    builder.method("POST", null);
                }
            }
            Request request = builder.build();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(timeOut / 1000, TimeUnit.SECONDS)
                    .build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                Headers responseHeaders = response.headers();
                if (responseHeaders != null) {
                    Map<String, List<String>> headers = new HashMap<>();
                    for (String name : responseHeaders.names()) {
                        List<String> value = responseHeaders.values(name);
                        headers.put(name, value);
                    }
                    initHeaders(headers, responce);
                }
                ResponseBody body = response.body();
                if (body != null) {
                    responce.result = body.bytes();
                }
                response.close();
            } catch (IOException e) {
                responce.setThrowable(e);
            }
            this.call = null;
            return responce;
        }
        return super.post(urlStr, params);
    }

    @Override
    public boolean cancel() {
        try {
            if (call != null) {
                call.cancel();
                call = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.cancel();
    }
}
