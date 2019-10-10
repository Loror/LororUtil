package com.loror.lororUtil.http;

import android.annotation.SuppressLint;

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

    /**
     * 设置超时时间
     */
    public void setTimeOut(int timeOut) {
        if (timeOut > 1000 && timeOut < 60000) {
            this.timeOut = timeOut;
        }
    }

    /**
     * 设置读取超时时间
     */
    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    /**
     * 设置是否自动处理重定向
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * 读取http头
     */
    protected void initHeaders(HttpURLConnection connection, Responce responce) {
        String cookieskey = "Set-Cookie";
        responce.headers = connection.getHeaderFields();
        responce.cookielist = responce.headers.get(cookieskey);
        if (responce.cookielist == null) {
            responce.cookielist = new ArrayList<>();
        }
        for (String cookielist : responce.cookielist) {
            if (cookielist == null) {
                continue;
            }
            String[] cookies = cookielist.split(";");
            for (int i = 0; i < cookies.length; i++) {
                String cookie = cookies[i];
                if (cookie == null) {
                    continue;
                }
                try {
                    cookie = cookie.trim();
                    String[] keyValue = cookie.split("\\=");
                    if (keyValue.length == 1) {
                        responce.cookies.put(keyValue[0], null);
                    } else {
                        responce.cookies.put(keyValue[0], cookie.substring(keyValue[0].length() + 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取返回数据
     */
    protected void readResponce(HttpURLConnection conn, Responce responce) throws Exception {
        InputStream inputStream = responce.getCode() / 100 > 3 ? conn.getErrorStream() : conn.getInputStream();
        if ("gzip".equals(responce.getContentEncoding())) {
            inputStream = new GZIPInputStream(inputStream);
        }
        List<byte[]> bytesList = new ArrayList<>();
        byte[] bytes = new byte[1024];
        int total = 0;
        int length = 0;
        while ((total = inputStream.read(bytes)) != -1) {
            byte[] temp = new byte[total];
            System.arraycopy(bytes, 0, temp, 0, total);
            bytesList.add(temp);
            length += total;
        }
        byte[] result = new byte[length];
        int position = 0;
        for (int i = 0; i < bytesList.size(); i++) {
            byte[] temp = bytesList.get(i);
            System.arraycopy(temp, 0, result, position, temp.length);
            position += temp.length;
        }
        responce.result = result;
    }

    @Override
    public Responce get(String urlStr, RequestParams params) {
        Responce responce = new Responce();
        try {
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareGet(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentEncoding = conn.getContentEncoding();
            if (responce.code == HttpURLConnection.HTTP_OK) {
                initHeaders(conn, responce);
            }
            responce.url = conn.getURL();
            readResponce(conn, responce);
            conn.disconnect();
        } catch (Throwable e) {
            responce.setThrowable(e);
        }
        conn = null;
        return responce;
    }

    @Override
    public Responce post(String urlStr, RequestParams params) {
        if (params == null || (params.getFiles().size() == 0 && !params.isUseFormForPost())) {
            Responce responce = new Responce();
            try {
                if (params != null && params.isAsJson() && params.getJson() != null) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr) + strParams;
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePost(conn, timeOut, readTimeOut, params);
                if (params != null) {
                    String strParams = params.packetOutParams("POST");
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
                responce.contentEncoding = conn.getContentEncoding();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                readResponce(conn, responce);
                conn.disconnect();
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
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                }
                preparePostFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                String StrParmas = params.packetOutParams("POST_FORM");
                if (!TextUtil.isEmpty(StrParmas)) {
                    out.write(StrParmas.getBytes());
                }// 提交参数
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index++, out, progressListener, callbackActuator);
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (Config.PREFIX + Config.BOUNDARY + Config.PREFIX + Config.LINEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentEncoding = conn.getContentEncoding();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                readResponce(conn, responce);
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
    }

    @Override
    public Responce put(String urlStr, RequestParams params) {
        if (params == null || (params.getFiles().size() == 0 && !params.isUseFormForPost())) {
            Responce responce = new Responce();
            try {
                if (params != null && params.isAsJson() && params.getJson() != null) {
                    String strParams = params.packetOutParams("GET");
                    if (!TextUtil.isEmpty(strParams)) {
                        urlStr += params.getSplicing(urlStr) + strParams;
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePut(conn, timeOut, readTimeOut, params);
                if (params != null) {
                    String strParams = params.packetOutParams("POST");
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
                responce.contentEncoding = conn.getContentEncoding();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                readResponce(conn, responce);
                conn.disconnect();
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
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                if (progressListener != null) {
                    conn.setUseCaches(false);
                }
                preparePutFile(conn, timeOut, readTimeOut, params);
                OutputStream out = conn.getOutputStream();
                if (params.isGzip()) {
                    out = new GZIPOutputStream(out);
                }
                String strParams = params.packetOutParams("POST_FORM");
                if (!TextUtil.isEmpty(strParams)) {
                    out.write(strParams.getBytes());
                } // 提交参数
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index++, out, progressListener, callbackActuator);
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (Config.PREFIX + Config.BOUNDARY + Config.PREFIX + Config.LINEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                responce.contentEncoding = conn.getContentEncoding();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                readResponce(conn, responce);
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
    }

    @Override
    public Responce delete(String urlStr, RequestParams params) {
        Responce responce = new Responce();
        try {
            if (params != null) {
                String strParams = params.packetOutParams("GET");
                if (!TextUtil.isEmpty(strParams)) {
                    urlStr += params.getSplicing(urlStr) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareDelete(conn, timeOut, readTimeOut, params);
            responce.code = conn.getResponseCode();
            responce.contentEncoding = conn.getContentEncoding();
            if (responce.code == HttpURLConnection.HTTP_OK) {
                initHeaders(conn, responce);
            }
            responce.url = conn.getURL();
            readResponce(conn, responce);
            conn.disconnect();
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
        sb.append(Config.PREFIX);
        sb.append(Config.BOUNDARY);
        sb.append(Config.LINEND);
        // name是post中传参的键 filename是文件的名称
        sb.append("Content-Disposition: form-data; name=\"").append(file.getKey() == null ? "file" : file.getKey())
                .append("\"; filename=\"").append(file.getName()).append("\"" + Config.LINEND);
        sb.append("Content-Type: ").append(file.getContentType()).append("; charset=UTF-8" + Config.LINEND);
        sb.append(Config.LINEND);
        out.write(sb.toString().getBytes());

        sendFile(file.getFile(), out, progressListener, actuator);
        out.write(Config.LINEND.getBytes());
    }

    /**
     * 通过流发送文件
     */
    private void sendFile(File file, OutputStream os, final ProgressListener progressListener, Actuator actuator) throws Throwable {
        FileInputStream fis = new FileInputStream(file);
        final long length = file.length();
        long lastTime = System.currentTimeMillis(), transed = 0;
        byte[] temp = new byte[1024 * 100];
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
                final int progress = (int) (transed * 1.0 / length * 100);
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
                final int progress = (int) (transed * 1.0 / length * 100);
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
                    urlStr += params.getSplicing(urlStr) + strParams;
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareGet(conn, timeOut, readTimeOut, params);
            conn.setRequestProperty("Accept-Encoding", "identity");
            long length = length(conn);
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            if (file.exists() && !cover && file.length() == length) {
                conn.disconnect();
            } else {
                downloadFile(file, length, conn.getInputStream(), progressListener, callbackActuator);
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
            }
            responce.result = file.getAbsolutePath().getBytes();
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
            long length = length(conn);
            downloadFile(file, length, conn.getInputStream(), progressListener, callbackActuator);
            if (responce.code == HttpURLConnection.HTTP_PARTIAL) {
                initHeaders(conn, responce);
            }
            conn.disconnect();
            responce.result = file.getAbsolutePath().getBytes();
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

    @SuppressLint("NewApi")
    private long length(T conn) {
        long length = 0;
        try {
            length = conn.getContentLengthLong();
        } catch (Throwable e) {
            length = conn.getContentLength();
        }
        return length;
    }

}
