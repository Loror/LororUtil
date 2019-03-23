package com.loror.lororUtil.http;

import android.annotation.SuppressLint;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.loror.lororUtil.text.TextUtil;

public abstract class BaseClient<T extends HttpURLConnection> extends Prepare implements Client {
    protected int timeOut = 10000;
    protected int readTimeOut;
    protected boolean followRedirects = true;
    protected T conn;
    private ProgressListener progressListener;

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
    protected byte[] getResponce(T conn) throws Exception {
        InputStream inputStream = conn.getInputStream();
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
        return result;

    }

    @Override
    public synchronized Responce get(String urlStr, RequestParams parmas) {
        Responce responce = new Responce();
        try {
            if (parmas != null) {
                String StrParmas = parmas.packetOutParams("GET");
                if (!TextUtil.isEmpty(StrParmas)) {
                    if (urlStr.indexOf("?") != -1) {
                        urlStr += "&" + StrParmas;
                    } else {
                        urlStr += "?" + StrParmas;
                    }
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareGet(conn, timeOut, readTimeOut, parmas);
            responce.code = conn.getResponseCode();
            if (responce.code == HttpURLConnection.HTTP_OK) {
                initHeaders(conn, responce);
            }
            responce.url = conn.getURL();
            responce.result = getResponce(conn);
            conn.disconnect();
        } catch (Throwable e) {
            responce.setThrowable(e);
        }
        conn = null;
        return responce;
    }

    @Override
    public synchronized Responce post(String urlStr, RequestParams parmas) {
        if (parmas == null || parmas.getFiles().size() == 0) {
            Responce responce = new Responce();
            try {
                if (parmas != null && parmas.isAsJson()) {
                    String StrParmas = parmas.packetOutParams("GET");
                    if (!TextUtil.isEmpty(StrParmas)) {
                        if (urlStr.indexOf("?") != -1) {
                            urlStr += "&" + StrParmas;
                        } else {
                            urlStr += "?" + StrParmas;
                        }
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePost(conn, timeOut, readTimeOut, parmas);
                if (parmas != null) {
                    String StrParmas = parmas.packetOutParams("POST");
                    if (!TextUtil.isEmpty(StrParmas)) {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
                        pw.print(StrParmas);
                        pw.close();
                    }
                }
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                responce.result = getResponce(conn);
                conn.disconnect();
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
            }
            return responce;
        } else {
            List<FileBody> files = parmas.getFiles();
            final Responce responce = new Responce();
            try {
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePostFile(conn, timeOut, readTimeOut, parmas);
                OutputStream out = new DataOutputStream(conn.getOutputStream());
                String StrParmas = parmas.packetOutParams("POST_FORM");
                if (!TextUtil.isEmpty(StrParmas)) {
                    out.write(StrParmas.getBytes());
                }// 提交参数
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index, out);
                        index++;
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (Config.PREFIX + Config.BOUNDARY + Config.PREFIX + Config.LINEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                responce.result = getResponce(conn);
                conn.disconnect();
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (responce.result == null) {
                    if (progressListener != null) {
                        postRunnable(new Runnable() {

                            @Override
                            public void run() {
                                progressListener.failed();
                            }
                        });
                    }
                } else {
                    if (progressListener != null) {
                        postRunnable(new Runnable() {

                            @Override
                            public void run() {
                                progressListener.finish(responce.toString());
                            }
                        });
                    }
                }
            }
            return responce;
        }
    }

    @Override
    public synchronized Responce put(String urlStr, RequestParams parmas) {
        if (parmas == null || parmas.getFiles().size() == 0) {
            Responce responce = new Responce();
            try {
                if (parmas != null && parmas.isAsJson()) {
                    String StrParmas = parmas.packetOutParams("GET");
                    if (!TextUtil.isEmpty(StrParmas)) {
                        if (urlStr.indexOf("?") != -1) {
                            urlStr += "&" + StrParmas;
                        } else {
                            urlStr += "?" + StrParmas;
                        }
                    }
                }
                URL url = new URL(urlStr);
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePut(conn, timeOut, readTimeOut, parmas);
                if (parmas != null) {
                    String StrParmas = parmas.packetOutParams("POST");
                    if (!TextUtil.isEmpty(StrParmas)) {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(conn.getOutputStream()));
                        pw.print(StrParmas);
                        pw.close();
                    }
                }
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                responce.result = getResponce(conn);
                conn.disconnect();
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
            }
            return responce;
        } else {
            List<FileBody> files = parmas.getFiles();
            final Responce responce = new Responce();
            try {
                URL url = new URL(urlStr);// 服务器的域名
                conn = (T) url.openConnection();
                if (followRedirects) {
                    conn.setInstanceFollowRedirects(true);
                }
                preparePutFile(conn, timeOut, readTimeOut, parmas);
                OutputStream out = new DataOutputStream(conn.getOutputStream());
                String StrParmas = parmas.packetOutParams("POST_FORM");
                if (!TextUtil.isEmpty(StrParmas)) {
                    out.write(StrParmas.getBytes());
                } // 提交参数
                if (files != null) {
                    int index = 0;
                    for (FileBody file : files) {
                        upLoadFile(file, index, out);
                        index++;
                    }
                } // 上传文件
                // 定义最后数据分隔线，即--加上BOUNDARY再加上--，写上结尾标识
                byte[] end_data = (Config.PREFIX + Config.BOUNDARY + Config.PREFIX + Config.LINEND).getBytes();
                out.write(end_data);
                out.flush();
                out.close();
                responce.url = conn.getURL();
                responce.code = conn.getResponseCode();
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
                responce.result = getResponce(conn);
                conn.disconnect();
            } catch (Throwable e) {
                responce.setThrowable(e);
            } finally {
                conn = null;
                if (responce.result == null) {
                    if (progressListener != null) {
                        postRunnable(new Runnable() {

                            @Override
                            public void run() {
                                progressListener.failed();
                            }
                        });
                    }
                } else {
                    if (progressListener != null) {
                        postRunnable(new Runnable() {

                            @Override
                            public void run() {
                                progressListener.finish(responce.toString());
                            }
                        });
                    }
                }
            }
            return responce;
        }
    }

    @Override
    public synchronized Responce delete(String urlStr, RequestParams parmas) {
        Responce responce = new Responce();
        try {
            if (parmas != null) {
                String StrParmas = parmas.packetOutParams("GET");
                if (!TextUtil.isEmpty(StrParmas)) {
                    if (urlStr.indexOf("?") != -1) {
                        urlStr += "&" + StrParmas;
                    } else {
                        urlStr += "?" + StrParmas;
                    }
                }
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareDelete(conn, timeOut, readTimeOut, parmas);
            responce.code = conn.getResponseCode();
            if (responce.code == HttpURLConnection.HTTP_OK) {
                initHeaders(conn, responce);
            }
            responce.url = conn.getURL();
            responce.result = getResponce(conn);
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
    private void upLoadFile(FileBody file, final int index, OutputStream out) throws Throwable {
        if (file == null || file.getFile() == null) {
            return;
        }
        if (progressListener instanceof DetailProgressListener) {
            postRunnable(new Runnable() {

                @Override
                public void run() {
                    ((DetailProgressListener) progressListener).fileIndex(index);
                }
            });
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

        sendFile(file.getFile(), out);
        out.write(Config.LINEND.getBytes());
    }

    /**
     * 通过流发送文件
     */
    private void sendFile(File file, OutputStream os) throws Throwable {
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
            long now = System.currentTimeMillis();
            final long timeGo = now - lastTime;
            if (timeGo > 30) {
                final int progress = (int) (transed * 100 / length);
                final int finalSpeed = speed;
                if (progressListener != null) {
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.transing(progress, (int) (finalSpeed * 1000L / timeGo), length);
                        }
                    });
                }
                speed = 0;
                lastTime = now;
            }
        }
        if (progressListener != null) {
            final long timeGo = (System.currentTimeMillis() - lastTime);
            final int finalSpeed = speed;
            postRunnable(new Runnable() {

                @Override
                public void run() {
                    progressListener.transing(100, timeGo == 0 ? 0 : (int) (finalSpeed * 1000L / timeGo), length);
                }
            });
        }
        fis.close();
    }

    /**
     * 通过流接受文件
     */
    private void downloadFile(File file, final long length, InputStream is) throws Throwable {
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
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.transing(progress, (int) (finalSpeed * 1000L / timeGo), length);
                        }
                    });
                }
                speed = 0;
                last = now;
            }
        }
        if (progressListener != null) {
            final long timeGo = (System.currentTimeMillis() - last);
            final int finalSpeed = speed;
            postRunnable(new Runnable() {

                @Override
                public void run() {
                    progressListener.transing(100, timeGo == 0 ? 0 : (int) (finalSpeed * 1000L / timeGo), length);
                }
            });
        }
        is.close();
        fos.close();
    }

    /**
     * 下载文件
     */
    public synchronized Responce download(String urlStr, String path, boolean cover) {
        final Responce responce = new Responce();
        try {
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            if (followRedirects) {
                conn.setInstanceFollowRedirects(true);
            }
            prepareGet(conn, timeOut, readTimeOut, null);
            conn.setRequestProperty("Accept-Encoding", "identity");
            long length = length(conn);
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            if (file.exists() && !cover && file.length() == length) {
                conn.disconnect();
            } else {
                downloadFile(file, length, conn.getInputStream());
                if (responce.code == HttpURLConnection.HTTP_OK) {
                    initHeaders(conn, responce);
                }
            }
            responce.result = file.getAbsolutePath().getBytes();
        } catch (Throwable e) {
            responce.setThrowable(e);
        } finally {
            conn = null;
            if (responce.result == null) {
                if (progressListener != null) {
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.failed();
                        }
                    });
                }
            } else {
                if (progressListener != null) {
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.toString());
                        }
                    });
                }
            }
        }
        return responce;
    }

    /**
     * 断点续传下载
     */
    public synchronized Responce downloadInPeice(String urlStr, String path, long start, long end) {
        final Responce responce = new Responce();
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
            downloadFile(file, length, conn.getInputStream());
            if (responce.code == HttpURLConnection.HTTP_PARTIAL) {
                initHeaders(conn, responce);
            }
            conn.disconnect();
            responce.result = file.getAbsolutePath().getBytes();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            conn = null;
            if (responce.result == null) {
                if (progressListener != null) {
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.failed();
                        }
                    });
                }
            } else {
                if (progressListener != null) {
                    postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            progressListener.finish(responce.toString());
                        }
                    });
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

    /**
     * 提交runable
     */
    protected void postRunnable(Runnable runnable) {
        runnable.run();
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
