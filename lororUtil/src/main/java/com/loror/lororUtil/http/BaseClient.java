package com.loror.lororUtil.http;

import android.os.Build;

import com.loror.lororUtil.text.TextUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public abstract class BaseClient<T extends HttpURLConnection> extends Prepare implements Client {

    private int timeOut = 10000;
    private int readTimeOut;
    private boolean followRedirects = true;
    private int fileReadLength = 1024 * 100;
    private boolean keepStream;
    private T conn;
    private ProgressListener progressListener;
    protected Actuator callbackActuator;

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
        if (timeOut > 1000 && timeOut < 60000) {
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
        this.readTimeOut = readTimeOut;
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
        try {
            String cookieskey = "Set-Cookie";
            responce.headers = connection.getHeaderFields();
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

    @Override
    public Responce get(String urlStr, RequestParams params) {
        Responce responce = new Responce();
        try {
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr, 0) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            HttpsClient.Config.httpsConfig(conn);
            prepareGet(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentType = conn.getContentType();
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
        if (params == null || (params.getFiles().size() == 0 && !params.isUseMultiForPost())) {
            Responce responce = new Responce();
            try {
                boolean queryParam = false;
                if (params != null) {
                    if ((params.isAsJson() && params.getJson() != null)
                            || params.isUseQueryForPost()) {
                        String strParams = params.packetOutParams("GET");
                        if (!TextUtil.isEmpty(strParams)) {
                            urlStr += params.getSplicing(urlStr, 0) + strParams;
                        }
                        queryParam = true;
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                HttpsClient.Config.httpsConfig(conn);
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
            List<FileBody> files = params.getFiles();
            final Responce responce = new Responce();
            try {
                if (params.isUseQueryForPost()) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr, 0) + strParams;
                    }
                }
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                    conn.setChunkedStreamingMode(fileReadLength);
                }
                HttpsClient.Config.httpsConfig(conn);
                preparePostFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                if (!params.isUseQueryForPost()) {
                    String strParams = params.packetOutParams("POST_MULTI");
                    if (!TextUtil.isEmpty(strParams)) {
                        out.write(strParams.getBytes());
                    }// 提交参数
                }
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index++, out, progressListener, callbackActuator);
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
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (progressListener != null) {
                    if (responce.result == null) {
                        Runnable runnable = new Runnable() {

                            @Override
                            public void run() {
                                progressListener.failed();
                            }
                        };
                        if (callbackActuator != null) {
                            callbackActuator.run(runnable);
                        } else {
                            runnable.run();
                        }
                    } else {
                        Runnable runnable = new Runnable() {

                            @Override
                            public void run() {
                                progressListener.finish(responce.toString());
                            }
                        };
                        if (callbackActuator != null) {
                            callbackActuator.run(runnable);
                        } else {
                            runnable.run();
                        }
                    }
                }
            }
            return responce;
        }
    }

    @Override
    public Responce put(String urlStr, RequestParams params) {
        if (params == null || (params.getFiles().size() == 0 && !params.isUseMultiForPost())) {
            Responce responce = new Responce();
            try {
                boolean queryParam = false;
                if (params != null) {
                    if ((params.isAsJson() && params.getJson() != null)
                            || params.isUseQueryForPost()) {
                        String strParams = params.packetOutParams("GET");
                        if (!TextUtil.isEmpty(strParams)) {
                            urlStr += params.getSplicing(urlStr, 0) + strParams;
                        }
                        queryParam = true;
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                HttpsClient.Config.httpsConfig(conn);
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
            List<FileBody> files = params.getFiles();
            final Responce responce = new Responce();
            try {
                if (params.isUseQueryForPost()) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr, 0) + strParams;
                    }
                }
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                    conn.setChunkedStreamingMode(fileReadLength);
                }
                HttpsClient.Config.httpsConfig(conn);
                preparePutFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                if (!params.isUseQueryForPost()) {
                    String strParams = params.packetOutParams("POST_MULTI");
                    if (!TextUtil.isEmpty(strParams)) {
                        out.write(strParams.getBytes());
                    } // 提交参数
                }
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index++, out, progressListener, callbackActuator);
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
                responce.contentEncoding = conn.getContentEncoding();
                initHeaders(conn, responce);
                readResponce(conn, responce);
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (progressListener != null) {
                    if (responce.result == null) {
                        Runnable runnable = new Runnable() {

                            @Override
                            public void run() {
                                progressListener.failed();
                            }
                        };
                        if (callbackActuator != null) {
                            callbackActuator.run(runnable);
                        } else {
                            runnable.run();
                        }
                    } else {
                        Runnable runnable = new Runnable() {

                            @Override
                            public void run() {
                                progressListener.finish(responce.toString());
                            }
                        };
                        if (callbackActuator != null) {
                            callbackActuator.run(runnable);
                        } else {
                            runnable.run();
                        }
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
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            HttpsClient.Config.httpsConfig(conn);
            prepareDelete(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentType = conn.getContentType();
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
    private void upLoadFile(FileBody file, final int index, OutputStream out, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        if (file == null || file.getFile() == null) {
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
        sb.append("Content-Disposition: form-data; name=\"").append(file.getKey() == null ? "file" : file.getKey())
                .append("\"; filename=\"").append(file.getName()).append("\"" + MultipartConfig.LINEEND);
        sb.append("Content-Type: ").append(file.getContentType()).append(MultipartConfig.LINEEND);
        sb.append(MultipartConfig.LINEEND);
        out.write(sb.toString().getBytes());

        sendFile(file.getFile(), out, progressListener, actuator);
        out.write(MultipartConfig.LINEEND.getBytes());
    }

    /**
     * 通过流发送文件
     */
    private void sendFile(File file, OutputStream os, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        FileInputStream fis = new FileInputStream(file);
        final long length = file.length();
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
        fis.close();
    }

    /**
     * 通过流接受文件
     */
    private void downloadFile(File file, final long length, InputStream is, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        long last = System.currentTimeMillis(), transed = 0;
        FileOutputStream fos = new FileOutputStream(file);
        byte[] out = new byte[1024 * 100];
        int total = 0;
        int speed = 0;
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
                last = now;
            }
        }
        if (progressListener != null) {
            final long timeGo = (System.currentTimeMillis() - last);
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
        is.close();
        fos.close();
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
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareGet(conn, timeOut, readTimeOut, params);
            conn.setRequestProperty("Accept-Encoding", "identity");
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            responce.contentEncoding = conn.getContentEncoding();
            initHeaders(conn, responce);
            if (responce.code == HttpURLConnection.HTTP_OK) {
                long length = length(conn);
                if (file.exists() && !cover && file.length() == length) {
                    responce.result = "success".getBytes();
                } else {
                    InputStream inputStream = conn.getInputStream();
                    if ("gzip".equals(responce.getContentEncoding())) {
                        inputStream = new GZIPInputStream(inputStream);
                    }
                    downloadFile(file, length, inputStream, progressListener, callbackActuator);
                    responce.result = "success".getBytes();
                }
            }
            conn.disconnect();
        } catch (Throwable e) {
            responce.setThrowable(e);
        } finally {
            conn = null;
            if (progressListener != null) {
                if (responce.result == null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.failed();
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                } else {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.toString());
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
        }
        return responce;
    }

    /**
     * 断点续传下载
     */
    public Responce downloadInPiece(String urlStr, String path, long start, long end) {
        final Responce responce = new Responce();
        final ProgressListener progressListener = this.progressListener;
        final Actuator callbackActuator = this.callbackActuator;
        try {
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
            URL url = new URL(urlStr);// 服务器的域名
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            conn.setConnectTimeout(timeOut);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setDoInput(true);
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            responce.contentEncoding = conn.getContentEncoding();
            initHeaders(conn, responce);
            if (responce.code == HttpURLConnection.HTTP_OK || responce.code == HttpURLConnection.HTTP_PARTIAL) {
                long length = length(conn);
                InputStream inputStream = conn.getInputStream();
                if ("gzip".equals(responce.getContentEncoding())) {
                    inputStream = new GZIPInputStream(inputStream);
                }
                downloadFile(file, length, inputStream, progressListener, callbackActuator);
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    responce.result = "not support".getBytes();
                } else {
                    responce.result = "success".getBytes();
                }
            } else {
                responce.result = null;
            }
            conn.disconnect();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            conn = null;
            if (progressListener != null) {
                if (responce.result == null) {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.failed();
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                } else {
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.toString());
                        }
                    };
                    if (callbackActuator != null) {
                        callbackActuator.run(runnable);
                    } else {
                        runnable.run();
                    }
                }
            }
        }
        return responce;
    }

    /**
     * 获取服务器文件大小
     */
    public long getContentLength(String urlStr) {
        long length = 0;
        try {
            URL url = new URL(urlStr);
            T conn = (T) url.openConnection();
            prepareGet(conn, timeOut, readTimeOut, null);
            conn.setRequestProperty("Accept-Encoding", "identity");
            length = length(conn);
            conn.disconnect();
        } catch (Throwable e) {
            System.out.println("cannot get contentlength");
        }
        return length;
    }

    private long length(T conn) {
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
