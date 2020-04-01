package com.loror.lororUtil.http;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URLDecoder;

import com.loror.lororUtil.convert.MD5Util;
import com.loror.lororUtil.text.TextUtil;

import android.os.Environment;

public class Prepare {

    /**
     * post文件准备
     */
    public void preparePostFile(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("POST");// 设置为POST情
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        conn.setDoOutput(true);// 发送POST请求必须设置如下两行
        conn.setDoInput(true);
        conn.setUseCaches(false);

        conn.setRequestProperty("connection", "keep-alive");// 设置请求头参数
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MultipartConfig.BOUNDARY);

        if (parmas != null && parmas.getHeaders().size() > 0) {
            for (String name : parmas.getHeaders().keySet()) {
                conn.setRequestProperty(name, parmas.getHeader(name));
            }
        }
    }

    /**
     * post参数准备
     */
    public void preparePost(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);

        conn.setRequestProperty("Content-Language", "zh-cn");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Charset", "UTF-8");
//        conn.setRequestProperty("Cache-Control", "no-cache");
        if (parmas != null) {
            if (parmas.isAsJson()) {
                conn.setRequestProperty("Content-Type", "application/json");
            } else if (parmas.isUseDefaultConverterInPost()) {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            if (parmas.getHeaders().size() > 0) {
                for (String name : parmas.getHeaders().keySet()) {
                    conn.setRequestProperty(name, parmas.getHeader(name));
                }
            }
        }

    }

    /**
     * get参数准备
     */
    public void prepareGet(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        if (parmas != null && parmas.getHeaders().size() > 0) {
            for (String name : parmas.getHeaders().keySet()) {
                conn.setRequestProperty(name, parmas.getHeader(name));
            }
        }
        conn.setDoInput(true);
    }

    /**
     * put文件准备
     */
    public void preparePutFile(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("PUT");// 设置为PUT情
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        conn.setDoOutput(true);// 发送POST请求必须设置如下两行
        conn.setDoInput(true);

        conn.setRequestProperty("connection", "keep-alive");// 设置请求头参数
        conn.setRequestProperty("Charset", "UTF-8");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + MultipartConfig.BOUNDARY);
        if (parmas != null && parmas.getHeaders().size() > 0) {
            for (String name : parmas.getHeaders().keySet()) {
                conn.setRequestProperty(name, parmas.getHeader(name));
            }
        }
    }

    /**
     * put参数准备
     */
    public void preparePut(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("PUT");
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setRequestProperty("Content-Language", "zh-cn");
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Charset", "UTF-8");

        if (parmas != null) {
            if (parmas.isAsJson()) {
                conn.setRequestProperty("Content-Type", "application/json");
            } else if (parmas.isUseDefaultConverterInPost()) {
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            }
            if (parmas.getHeaders().size() > 0) {
                for (String name : parmas.getHeaders().keySet()) {
                    conn.setRequestProperty(name, parmas.getHeader(name));
                }
            }
        }
    }

    /**
     * delete参数准备
     */
    public void prepareDelete(HttpURLConnection conn, int timeOut, int readTimeOut, RequestParams parmas)
            throws Exception {
        conn.setRequestMethod("DELETE");
        conn.setConnectTimeout(timeOut);
        if (readTimeOut != 0) {
            conn.setReadTimeout(readTimeOut);
        }
        if (parmas != null && parmas.getHeaders().size() > 0) {
            for (String name : parmas.getHeaders().keySet()) {
                conn.setRequestProperty(name, parmas.getHeader(name));
            }
        }
        conn.setDoInput(true);
    }

    /**
     * 获取文件
     */
    protected File getFile(HttpURLConnection conn, String path, String url) throws Throwable {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            String name = null;
            try {
                String fn = URLDecoder.decode(conn.getURL().toString(), "UTF-8");
                name = fn.substring(fn.lastIndexOf("/") + 1);
            } catch (Exception e) {
                System.out.println("cannot get file name");
            }
            if (TextUtil.isEmpty(name)) {
                file = new File(file, MD5Util.md5(url));
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    /**
     * 检查SD卡状态
     */
    public boolean checkState() {
        try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
        } catch (Throwable e) {
            if (e instanceof NoClassDefFoundError) {
                System.out.println("may be not an android device");
            }
        }
        return true;
    }

}
