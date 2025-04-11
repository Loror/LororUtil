package com.loror.lororUtil.http;

import com.loror.lororUtil.http.okhttp.ProgressRequestBody;
import com.loror.lororUtil.text.TextUtil;

import java.io.File;
import java.io.InputStream;
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

    private OkHttpClient.Builder configOkHttp() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeOut / 1000, TimeUnit.SECONDS);
        if (readTimeOut != 0) {
            builder.readTimeout(readTimeOut / 1000, TimeUnit.SECONDS);
        }
        try {
            httpsConfig(builder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder;
    }

    /**
     * https配置
     */
    protected void httpsConfig(OkHttpClient.Builder builder) throws Exception {
        HttpsClient.Config.httpsConfig(builder);
    }

    /**
     * 读取http头
     */
    protected void initHeaders(Response response, Responce responce) {
        Headers responseHeaders = response.headers();
        if (responseHeaders != null) {
            Map<String, List<String>> headers = new HashMap<>();
            for (String name : responseHeaders.names()) {
                List<String> value = responseHeaders.values(name);
                headers.put(name, value);
            }
            initHeaders(headers, responce);
        }
        responce.contentType = response.header("Content-Type");
        String contentLengthHeader = response.header("Content-Length");
        if (contentLengthHeader != null) {
            responce.contentLength = Long.parseLong(contentLengthHeader);
        }
    }

    /**
     * 获取返回数据
     */
    protected void readResponce(Response response, Responce responce) throws Exception {
        ResponseBody body = response.body();
        if (body != null) {
            responce.result = body.bytes();
        }
        response.close();
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
            OkHttpClient okHttpClient = configOkHttp().build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                initHeaders(response, responce);
                readResponce(response, responce);
            } catch (Exception e) {
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
            boolean isMultipart = !(params == null || (params.getFiles().isEmpty() && !params.isForceMultiparty()));
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
                    if (!params.isForceParamAsQueryForPostOrPut()) {
                        body.addFormDataPart(kv.getKey(), String.valueOf(kv.getValue()));
                    }
                }
                for (StreamBody streamBody : params.getFiles()) {
                    if (streamBody instanceof FileBody) {
                        FileBody fileBody = (FileBody) streamBody;
                        body.addFormDataPart(streamBody.getKey(), streamBody.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), fileBody.getFile()));
                    } else {
                        body.addFormDataPart(streamBody.getKey(), streamBody.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), streamBody.getBytes()));
                    }
                }
                if (progressListener != null) {
                    builder.post(new ProgressRequestBody(body.build(), progressListener) {
                        @Override
                        protected void execute(Runnable runnable) {
//                            super.execute(runnable);
                            executeCallBack(runnable);
                        }
                    });
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
            OkHttpClient okHttpClient = configOkHttp().build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                initHeaders(response, responce);
                readResponce(response, responce);
            } catch (Exception e) {
                responce.setThrowable(e);
            }
            this.call = null;
            return responce;
        }
        return super.post(urlStr, params);
    }

    @Override
    public Responce put(String urlStr, RequestParams params) {
        if (core == CORE_OKHTTP3) {
            Responce responce = new Responce();
            Request request;
            if (params == null || params.getFiles().isEmpty()) {
                boolean queryParam = false;
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
                Request.Builder builder = new Request.Builder().url(urlStr);
                if (!queryParam && params != null && !params.getParams().isEmpty()) {
                    FormBody.Builder body = new FormBody.Builder();
                    for (Map.Entry<String, Object> kv : params.getParams().entrySet()) {
                        body.add(kv.getKey(), String.valueOf(kv.getValue()));
                    }
                    builder.put(body.build());
                } else {
                    builder.method("PUT", null);
                }
                if (params != null) {
                    Map<String, String> paramsHeaders = params.getHeaders();
                    for (Map.Entry<String, String> kv : paramsHeaders.entrySet()) {
                        builder.header(kv.getKey(), kv.getValue());
                    }
                }
                request = builder.build();
            } else {
                //这种情况只会上传第一个文件，其余参数全部打包到url
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
                final ProgressListener progressListener = this.progressListener;
                StreamBody file = params.getFiles().get(0);
                RequestBody body = RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType()));
                Request.Builder builder = new Request.Builder()
                        .url(urlStr);
                if (progressListener != null) {
                    builder.put(new ProgressRequestBody(body, progressListener) {
                        @Override
                        protected void execute(Runnable runnable) {
//                            super.execute(runnable);
                            executeCallBack(runnable);
                        }
                    });
                } else {
                    builder.put(body);
                }
                request = builder.build();
            }
            OkHttpClient okHttpClient = configOkHttp().build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                initHeaders(response, responce);
                readResponce(response, responce);
            } catch (Exception e) {
                responce.setThrowable(e);
            }
            this.call = null;
            return responce;
        }
        return super.put(urlStr, params);
    }

    @Override
    public Responce delete(String urlStr, RequestParams params) {
        if (core == CORE_OKHTTP3) {
            Responce responce = new Responce();
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
            }

            Request.Builder builder = new Request.Builder().url(urlStr);
            builder.method("DELETE", null);
            if (params != null) {
                Map<String, String> paramsHeaders = params.getHeaders();
                for (Map.Entry<String, String> kv : paramsHeaders.entrySet()) {
                    builder.header(kv.getKey(), kv.getValue());
                }
            }
            Request request = builder.build();
            OkHttpClient okHttpClient = configOkHttp().build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                initHeaders(response, responce);
                readResponce(response, responce);
            } catch (Exception e) {
                responce.setThrowable(e);
            }
            this.call = null;
            return responce;
        }
        return super.delete(urlStr, params);
    }

    @Override
    public Responce download(String urlStr, RequestParams params, String path, boolean cover) {
        if (core == CORE_OKHTTP3) {
            final Responce responce = new Responce();
            final ProgressListener progressListener = this.progressListener;
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
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
            OkHttpClient okHttpClient = configOkHttp().build();
            Call call = okHttpClient.newCall(request);
            this.call = call;
            try {
                Response response = call.execute();
                responce.code = response.code();
                initHeaders(response, responce);
                if (responce.code / 100 == 2) {
                    File file = getFile(responce.getUrl(), path, urlStr);
                    InputStream inputStream = response.body().byteStream();
                    downloadFile(params, responce, file, responce.contentLength, inputStream, cover, progressListener);
                    responce.result = "success".getBytes();
                }
                response.close();
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.result != null);
                        }
                    };
                    executeCallBack(runnable);
                }
            }
            this.call = null;
            return responce;
        }
        return super.download(urlStr, params, path, cover);
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
