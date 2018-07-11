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
                cookie = cookie.trim();
                try {
                    String[] keyValue = cookie.split("\\=");
                    if (keyValue == null) {
                        System.err.println("error expected cookie");
                    } else if (keyValue.length == 1) {
                        responce.cookies.put(keyValue[0], null);
                    } else {
                        responce.cookies.put(keyValue[0], cookie.substring(keyValue[0].length() + 1, cookie.length()));
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
        ArrayList<Byte> list = new ArrayList<>();
        byte[] bytes = new byte[1024];
        int total = 0;
        while ((total = inputStream.read(bytes)) != -1) {
            for (int i = 0; i < total; i++) {
                list.add(bytes[i]);
            }
        }
        bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
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
            return postFrom(urlStr, parmas);
        } else {
            return postFile(urlStr, parmas, parmas.getFiles());
        }
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
     * post请求
     */
    protected Responce postFrom(String urlStr, RequestParams parmas) {
        Responce responce = new Responce();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection.setFollowRedirects(true);
            conn = (T) url.openConnection();
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
    }

    /**
     * 上传文件带参数
     */
    protected Responce postFile(final String urlStr, RequestParams parmas, List<FileBody> files) {
        final Responce responce = new Responce();
        try {
            URL url = new URL(urlStr);// 服务器的域名
            conn = (T) url.openConnection();
            preparePostFile(conn, timeOut, readTimeOut, parmas);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            if (parmas != null) {
                String StrParmas = parmas.packetOutParams("POST_FILE");
                if (!TextUtil.isEmpty(StrParmas)) {
                    out.write(StrParmas.getBytes());
                }
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

    /**
     * 上传一个文件
     */
    protected void upLoadFile(FileBody file, final int index, OutputStream out) throws Throwable {
        if (file.getFile() == null) {
            return;
        }
        if (progressListener != null && progressListener instanceof DetailProgressListener) {
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
        sb.append("Content-Disposition: form-data; name=\"" + (file.getKey() == null ? "file" : file.getKey())
                + "\"; filename=\"" + file.getName() + "\"" + Config.LINEND);
        sb.append("Content-Type: " + file.getContentType() + "; charset=UTF-8" + Config.LINEND);
        sb.append(Config.LINEND);
        out.write(sb.toString().getBytes());

        sendFile(file.getFile(), out);
        out.write(Config.LINEND.getBytes());
    }

    /**
     * 通过流发送文件
     */
    protected void sendFile(File file, OutputStream os) throws Throwable {
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
    protected void downloadFile(File file, final long length, InputStream is) throws Throwable {
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
    @SuppressLint("NewApi")
    public synchronized Responce download(String urlStr, String path, boolean cover) {
        final Responce responce = new Responce();
        try {
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
            URL url = new URL(urlStr);
            conn = (T) url.openConnection();
            prepareGet(conn, timeOut, readTimeOut, null);
            conn.setRequestProperty("Accept-Encoding", "identity");
            long length = 0;
            try {
                length = conn.getContentLengthLong();
            } catch (Throwable e) {
                length = conn.getContentLength();
            }
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
    @SuppressLint("NewApi")
    public synchronized Responce downloadInPeice(String urlStr, String path, long start, long end) {
        final Responce responce = new Responce();
        try {
            if (!checkState()) {
                throw new IllegalArgumentException("no permission to visit file");
            }
            URL url = new URL(urlStr);// 服务器的域名
            conn = (T) url.openConnection();
            conn.setConnectTimeout(timeOut);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
            conn.setRequestProperty("Accept-Encoding", "identity");
            conn.setDoInput(true);
            File file = getFile(conn, path, urlStr);
            responce.url = conn.getURL();
            responce.code = conn.getResponseCode();
            long length = 0;
            try {
                length = conn.getContentLengthLong();
            } catch (Throwable e) {
                length = conn.getContentLength();
            }
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
    @SuppressLint("NewApi")
    public long getContentLength(String urlStr) {
        long length = 0;
        try {
            URL url = new URL(urlStr);
            T conn = (T) url.openConnection();
            prepareGet(conn, timeOut, readTimeOut, null);
            conn.setRequestProperty("Accept-Encoding", "identity");
            try {
                length = conn.getContentLengthLong();
            } catch (Throwable e) {
                length = conn.getContentLength();
            }
            conn.disconnect();
        } catch (Throwable e) {
            System.out.println("cannot get contentlength");
        }
        return length;
    }

}
