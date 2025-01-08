package com.loror.lororUtil.http;

import android.os.Build;

import com.loror.lororUtil.http.okhttp.ProgressRequestBody;
import com.loror.lororUtil.text.TextUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

public abstract class BaseClient extends Prepare implements Client {

    private int timeOut = 10000;
    private int readTimeOut;
    private boolean followRedirects = true;
    private int fileReadLength = 1024 * 100;
    private boolean keepStream;
    private HttpURLConnection conn;
    private ProgressListener progressListener;
    protected Actuator callbackActuator;
    private int core = CORE_URL_CONNECTION;

    @Override
    public boolean setCore(int core) {
        if (core == CORE_OKHTTP3) {
            try {
                Class<?> type = okhttp3.OkHttpClient.class;
                System.out.println("update core:" + type.getSimpleName());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        this.core = core;
        return true;
    }

    /**
     * 设置回调执行器
     */
    public void setCallbackActuator(Actuator callbackActuator) {
        this.callbackActuator = callbackActuator;
    }

    /**
     * 进度监听
     */
    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    /**
     * 设置超时时间
     */
    public void setTimeOut(int timeOut) {
        if (timeOut > 1000 && timeOut <= 180000) {
            this.timeOut = timeOut;
        }
    }

    public int getTimeOut() {
        return timeOut;
    }

    /**
     * 设置读取超时时间
     */
    public void setReadTimeOut(int readTimeOut) {
        if (readTimeOut > 0) {
            this.readTimeOut = readTimeOut;
        }
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    /**
     * 设置是否自动处理重定向
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * 设置上传文件时每次读入大小
     */
    public void setFileReadLength(int fileReadLength) {
        this.fileReadLength = fileReadLength;
    }

    public int getFileReadLength() {
        return fileReadLength;
    }

    /**
     * 设置是否保留原始流到responce不进行读取
     */
    public void setKeepStream(boolean keepStream) {
        this.keepStream = keepStream;
    }

    public boolean isKeepStream() {
        return keepStream;
    }

    /**
     * 读取http头
     */
    protected void initHeaders(HttpURLConnection connection, Responce responce) {
        initHeaders(connection.getHeaderFields(), responce);
//        try {
//            String cookieskey = "Set-Cookie";
//            responce.headers = connection.getHeaderFields();
//            responce.cookieList = responce.headers.get(cookieskey);
//            if (responce.cookieList == null) {
//                responce.cookieList = new ArrayList<>();
//            }
//            responce.cookies = new ArrayList<>();
//            for (String cookie : responce.cookieList) {
//                SetCookie setCookie = SetCookie.parse(cookie);
//                if (setCookie != null) {
//                    responce.cookies.add(setCookie);
//                }
//            }
//        } catch (Throwable e) {
//            System.err.println("lost headers");
//        }
    }

    protected void initHeaders(Map<String, List<String>> headers, Responce responce) {
        try {
            String cookieskey = "Set-Cookie";
            responce.headers = headers;
            responce.cookieList = responce.headers.get(cookieskey);
            if (responce.cookieList == null) {
                responce.cookieList = new ArrayList<>();
            }
            responce.cookies = new ArrayList<>();
            for (String cookie : responce.cookieList) {
                SetCookie setCookie = SetCookie.parse(cookie);
                if (setCookie != null) {
                    responce.cookies.add(setCookie);
                }
            }
        } catch (Throwable e) {
            System.err.println("lost headers");
        }
    }

    /**
     * 获取返回数据
     */
    protected void readResponce(HttpURLConnection conn, Responce responce) throws Exception {
        InputStream inputStream = responce.getCode() / 100 > 3 ? conn.getErrorStream() : conn.getInputStream();
        if ("gzip".equalsIgnoreCase(responce.getContentEncoding())) {
            inputStream = new GZIPInputStream(inputStream);
        }
        responce.inputStream = inputStream;
        responce.connection = conn;
        if (!keepStream) {
            responce.readStream();
            responce.close();
        }
    }

    /**
     * https配置
     */
    protected void httpsConfig(HttpURLConnection conn) throws Exception {
        HttpsClient.Config.httpsConfig(conn);
    }

    @Override
    public Responce get(String urlStr, RequestParams params) {
        Responce responce = new Responce();
        if (params != null) {
            String strParams = params.packetOutParams("GET");
            if (!TextUtil.isEmpty(strParams)) {
                urlStr += params.getSplicing(urlStr, 0) + strParams;
            }
        }
        if (core == CORE_OKHTTP3) {
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
            return responce;
        }
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            httpsConfig(conn);
            prepareGet(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentType = conn.getContentType();
            responce.contentLength = length(conn);
            responce.contentEncoding = conn.getContentEncoding();
            responce.url = conn.getURL();
            initHeaders(conn, responce);
            readResponce(conn, responce);
        } catch (Throwable e) {
            responce.setThrowable(e);
        }
        conn = null;
        return responce;
    }

    @Override
    public Responce post(String urlStr, RequestParams params) {
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
        if (core == CORE_OKHTTP3) {
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
            return responce;
        }
        if (!isMultipart) {
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                httpsConfig(conn);
                preparePost(conn, timeOut, readTimeOut, params);
                if (params != null) {
                    String strParams = params.packetOutParams("POST");
                    if (queryParam && !params.isAsJson()) {
                        strParams = "";
                    }
                    if (!TextUtil.isEmpty(strParams)) {
                        OutputStream out = conn.getOutputStream();
                        if (params.isGzip()) {
                            out = new GZIPOutputStream(out);
                        }
                        out.write(strParams.getBytes());
                        out.flush();
                        out.close();
                    }
                }
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentType = conn.getContentType();
                responce.contentLength = length(conn);
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
            }
            return responce;
        } else {
            final ProgressListener progressListener = this.progressListener;
            final Actuator callbackActuator = this.callbackActuator;
            List<StreamBody> files = params.getFiles();
            try {
                URL url = new URL(urlStr);// 服务器的域名
                conn = (HttpURLConnection) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                    conn.setChunkedStreamingMode(fileReadLength);
                }
                httpsConfig(conn);
                preparePostFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                if (!params.isForceParamAsQueryForPostOrPut()) {
                    String strParams = params.packetOutParams("POST_MULTI");
                    if (!TextUtil.isEmpty(strParams)) {
                        out.write(strParams.getBytes());
                    }// 提交参数
                }
                if (files != null) {
                    int index = 0;
                    for (StreamBody body : files) {
                        upLoadFile(body, index++, out, progressListener, callbackActuator);
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (MultipartConfig.PREFIX + MultipartConfig.BOUNDARY + MultipartConfig.PREFIX + MultipartConfig.LINEEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentType = conn.getContentType();
                responce.contentLength = length(conn);
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.result != null);
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
            return responce;
        }
    }

    @Override
    public Responce put(String urlStr, RequestParams params) {
        if (params == null || params.getFiles().size() == 0) {
            Responce responce = new Responce();
            try {
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
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                httpsConfig(conn);
                preparePut(conn, timeOut, readTimeOut, params);
                if (params != null) {
                    String strParams = params.packetOutParams("POST");
                    if (queryParam && !params.isAsJson()) {
                        strParams = "";
                    }
                    if (!TextUtil.isEmpty(strParams)) {
                        OutputStream out = conn.getOutputStream();
                        if (params.isGzip()) {
                            out = new GZIPOutputStream(out);
                        }
                        out.write(strParams.getBytes());
                        out.flush();
                        out.close();
                    }
                }
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentType = conn.getContentType();
                responce.contentLength = length(conn);
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
            }
            return responce;
        } else if (params.isForceMultiparty()) {
            final ProgressListener progressListener = this.progressListener;
            final Actuator callbackActuator = this.callbackActuator;
            List<StreamBody> files = params.getFiles();
            final Responce responce = new Responce();
            try {
                if (params.isForceParamAsQueryForPostOrPut()) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr, 0) + strParams;
                    }
                }
                URL url = new URL(urlStr);// 服务器的域名
                conn = (HttpURLConnection) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                    conn.setChunkedStreamingMode(fileReadLength);
                }
                httpsConfig(conn);
                preparePutFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                if (!params.isForceParamAsQueryForPostOrPut()) {
                    String strParams = params.packetOutParams("POST_MULTI");
                    if (!TextUtil.isEmpty(strParams)) {
                        out.write(strParams.getBytes());
                    } // 提交参数
                }
                if (files != null) {
                    int index = 0;
                    for (StreamBody body : files) {
                        upLoadFile(body, index++, out, progressListener, callbackActuator);
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (MultipartConfig.PREFIX + MultipartConfig.BOUNDARY + MultipartConfig.PREFIX + MultipartConfig.LINEEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentType = conn.getContentType();
                responce.contentLength = length(conn);
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.result != null);
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
            return responce;
        } else {
            //这种情况只会上传第一个文件，其余参数全部打包到url
            final ProgressListener progressListener = this.progressListener;
            final Actuator callbackActuator = this.callbackActuator;
            List<StreamBody> files = params.getFiles();
            final Responce responce = new Responce();
            try {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
                URL url = new URL(urlStr);// 服务器的域名
                conn = (HttpURLConnection) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                    conn.setChunkedStreamingMode(fileReadLength);
                }
                httpsConfig(conn);
                preparePutSingleFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                //上传第一个文件
                StreamBody body = files.get(0);
                sendFile(body, out, progressListener, callbackActuator);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentType = conn.getContentType();
                responce.contentLength = length(conn);
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.result != null);
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
            return responce;
        }
    }

    @Override
    public Responce delete(String urlStr, RequestParams params) {
        Responce responce = new Responce();
        try {
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            httpsConfig(conn);
            prepareDelete(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentType = conn.getContentType();
            responce.contentLength = length(conn);
            responce.contentEncoding = conn.getContentEncoding();
            responce.url = conn.getURL();
            initHeaders(conn, responce);
            readResponce(conn, responce);
        } catch (Throwable e) {
            responce.setThrowable(e);
        }
        conn = null;
        return responce;
    }

    @Override
    public boolean cancel() {
        if (conn != null) {
            try {
                conn.disconnect();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 上传一个文件
     */
    private void upLoadFile(StreamBody body, final int index, OutputStream out, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        if (body == null || body.getInputStream() == null) {
            return;
        }
        if (progressListener instanceof DetailProgressListener) {
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    ((DetailProgressListener) progressListener).fileIndex(index);
                }
            };
            if (actuator != null) {
                actuator.run(runnable);
            } else {
                runnable.run();
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(MultipartConfig.PREFIX);
        sb.append(MultipartConfig.BOUNDARY);
        sb.append(MultipartConfig.LINEEND);
        // name是post中传参的键 filename是文件的名称
        sb.append("Content-Disposition: form-data; name=\"").append(body.getKey() == null ? "file" : body.getKey())
                .append("\"; filename=\"").append(body.getName()).append("\"" + MultipartConfig.LINEEND);
        sb.append("Content-Type: ").append(body.getContentType()).append(MultipartConfig.LINEEND);
        sb.append(MultipartConfig.LINEEND);
        out.write(sb.toString().getBytes());

        sendFile(body, out, progressListener, actuator);
        out.write(MultipartConfig.LINEEND.getBytes());
    }

    /**
     * 通过流发送文件
     */
    private void sendFile(StreamBody body, OutputStream os, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        InputStream fis = body.getInputStream();
        final long length = body.length();
        long lastTime = System.currentTimeMillis(), transed = 0;
        byte[] temp = new byte[fileReadLength > 0 ? fileReadLength : (1024 * 100)];
        int total = 0;
        int speed = 0;
        while ((total = fis.read(temp)) != -1) {
            transed += total;
            speed += total;
            os.write(temp, 0, total);
            os.flush();
            long now = System.currentTimeMillis();
            final long timeGo = now - lastTime;
            if (timeGo > 30) {
                final float progress = (float) (transed * 1.0 / length * 100);
                final int finalSpeed = speed;
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.transing(progress, (int) (finalSpeed * 1000L / timeGo), length);
                        }
                    };
                    if (actuator != null) {
                        actuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
                speed = 0;
                lastTime = now;
            }
        }
        if (progressListener != null) {
            final long timeGo = (System.currentTimeMillis() - lastTime);
            final int finalSpeed = speed;
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    progressListener.transing(100, timeGo == 0 ? 0 : (int) (finalSpeed * 1000L / timeGo), length);
                }
            };
            if (actuator != null) {
                actuator.run(runnable);
            } else {
                runnable.run();
            }
        }
        body.close();
    }

    /**
     * 下载文件
     */
    public Responce download(String urlStr, RequestParams params, String path, boolean cover) {
        final Responce responce = new Responce();
        final ProgressListener progressListener = this.progressListener;
        final Actuator callbackActuator = this.callbackActuator;
        try {
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            httpsConfig(conn);
            prepareGet(conn, timeOut, readTimeOut, params);
            conn.setRequestProperty("Accept-Encoding", "identity");
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            responce.contentEncoding = conn.getContentEncoding();
            initHeaders(conn, responce);
            if (responce.code == HttpURLConnection.HTTP_OK || responce.code == HttpURLConnection.HTTP_PARTIAL) {
                long length = length(conn);
                responce.contentLength = length;
                if (!cover && responce.code == HttpURLConnection.HTTP_OK && (file.exists() && file.length() == length)) {
                    responce.result = "success".getBytes();
                } else {
                    InputStream inputStream = conn.getInputStream();
                    if ("gzip".equals(responce.getContentEncoding())) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    downloadFile(params, responce, file, length, inputStream, cover, progressListener, callbackActuator);
                    responce.result = "success".getBytes();
                }
            }
            conn.disconnect();
        } catch (Throwable e) {
            responce.setThrowable(e);
        } finally {
            conn = null;
            if (progressListener != null) {
                Runnable runnable = new Runnable() {

                    @Override
                    public void run() {
                        progressListener.finish(responce.result != null);
                    }
                };
                if (callbackActuator != null) {
                    callbackActuator.run(runnable);
                } else {
                    runnable.run();
                }
            }
        }
        return responce;
    }

    /**
     * 通过流接受文件
     */
    private void downloadFile(RequestParams params, Responce responce, File file, long length, InputStream is, boolean cover,
                              final ProgressListener progressListener, Actuator actuator) throws Throwable {
        long last = System.currentTimeMillis(), transed = 0;
        FileOutputStream fos = null;
        byte[] out = new byte[1024 * 100];
        int total = 0;
        int speed = 0;
        long fileLength = length == 0 ? 1 : length;
        if (params != null) {
            if (responce.getCode() == HttpURLConnection.HTTP_PARTIAL) {
                String range = responce.getHeader("Content-Range");
                if (!cover) {
                    fos = new FileOutputStream(file, true);
                }
                if (!TextUtil.isEmpty(range)) {
                    int index = range.lastIndexOf("/");
                    if (index != -1) {
                        String fileLengthString = range.substring(index + 1);
                        if (TextUtil.isNumber(fileLengthString)) {
                            fileLength = Integer.parseInt(fileLengthString);
                        }
                    }
                }
            }
        }
        if (fos == null) {
            fos = new FileOutputStream(file);
        }
        final long finalFileLength = fileLength;
        while ((total = is.read(out)) != -1) {
            fos.write(out, 0, total);
            fos.flush();
            transed += total;
            speed += total;
            long now = System.currentTimeMillis();
            final long timeGo = now - last;
            if (timeGo > 30) {
                final float progress = (float) (transed * 1.0 / length * 100);
                final int finalSpeed = speed;
                if (progressListener != null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.transing(progress, (int) (finalSpeed * 1000L / timeGo), finalFileLength);
                        }
                    };
                    if (actuator != null) {
                        actuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
                speed = 0;
                last = now;
            }
        }
        if (progressListener != null) {
            final long timeGo = (System.currentTimeMillis() - last);
            final int finalSpeed = speed;
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    progressListener.transing(100, timeGo == 0 ? 0 : (int) (finalSpeed * 1000L / timeGo), finalFileLength);
                }
            };
            if (actuator != null) {
                actuator.run(runnable);
            } else {
                runnable.run();
            }
        }
        is.close();
        fos.close();
    }

    /**
     * 获取服务器文件大小
     */
    public long getContentLength(String urlStr) {
        long length = 0;
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            prepareGet(conn, timeOut, readTimeOut, null);
            conn.setRequestProperty("Accept-Encoding", "identity");
            length = length(conn);
            conn.disconnect();
        } catch (Throwable e) {
            System.out.println("cannot get contentlength");
        }
        return length;
    }

    private long length(HttpURLConnection conn) {
        long length = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                length = conn.getContentLengthLong();
            } else {
                length = conn.getContentLength();
            }
        } catch (Throwable e) {
            length = conn.getContentLength();
        }
        return length;
    }

}
