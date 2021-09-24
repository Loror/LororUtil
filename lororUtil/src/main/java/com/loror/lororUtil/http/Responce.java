package com.loror.lororUtil.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Responce {

    protected URL url;
    protected int code;
    protected long contentLength;
    public byte[] result;
    protected String contentEncoding;
    protected String contentType;
    protected Map<String, List<String>> headers;
    protected List<String> cookieList;
    protected List<SetCookie> cookies;
    protected InputStream inputStream;
    protected HttpURLConnection connection;
    protected Throwable throwable;

    public Responce() {

    }

    public Responce(int code) {
        this.code = code;
    }

    /**
     * 获取最终url路径
     */
    public URL getUrl() {
        return url;
    }

    /**
     * 获取返回码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取编码
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * 获取contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 获取http头
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * 获取http头
     */
    public List<String> getHeaderList(String key) {
        if (headers == null) {
            return null;
        }
        for (Map.Entry<String, List<String>> item : headers.entrySet()) {
            if (key == null) {
                if (item.getKey() == null) {
                    return item.getValue();
                }
            } else {
                if (item.getKey() != null && key.equalsIgnoreCase(item.getKey())) {
                    return item.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 获取http头
     */
    public String getHeader(String key) {
        List<String> list = getHeaderList(key);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取cookie
     */
    public List<SetCookie> getCookies() {
        return cookies;
    }

    /**
     * 获取cookie列表
     */
    public List<String> getCookieList() {
        return cookieList;
    }

    /**
     * 获取流，设置client属性keepStream为true时可以获取，若使用了流，需手动调用close关闭连接
     * 建议在io线程处理流相关代码
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 获取内容长度
     */
    public long getContentLength() {
        if (contentLength == 0 && result != null) {
            contentLength = result.length;
        }
        return contentLength;
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = null;
        }
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    /**
     * 设置异常
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
        //可能读取超时或用户取消连接，此时已获取code，重置为0
        if (throwable instanceof SocketTimeoutException ||
                throwable instanceof SocketException) {
            code = 0;
        }
    }

    /**
     * 获取异常
     */
    public Throwable getThrowable() {
        return throwable;
    }

    /**
     * 读取流中内容
     */
    protected void readStream() throws IOException {
        if (inputStream != null) {
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
            inputStream.close();
            inputStream = null;
            close();
            byte[] result = new byte[length];
            int position = 0;
            for (int i = 0; i < bytesList.size(); i++) {
                byte[] temp = bytesList.get(i);
                System.arraycopy(temp, 0, result, position, temp.length);
                position += temp.length;
            }
            this.result = result;
        }
    }

    @Override
    public String toString() {
        return toString(null);
    }

    //带编码
    public String toString(Charset charset) {
        if (this.throwable != null) {
            return "an exception happen : " + this.throwable.getClass().getName();
        } else if (this.result != null) {
            return charset == null ? new String(this.result) : new String(this.result, charset);
        } else if (this.connection != null) {
            try {
                readStream();
                return charset == null ? new String(this.result) : new String(this.result, charset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
