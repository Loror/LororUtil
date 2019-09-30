package com.loror.lororUtil.http;

import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Responce {
    protected URL url;
    protected int code;
    public byte[] result;
    protected HashMap<String, String> cookies = new HashMap<>();
    protected Map<String, List<String>> headers;
    protected List<String> cookielist;
    protected Throwable throwable;

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
     * 获取http头
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * 获取cookie
     */
    public HashMap<String, String> getCookies() {
        return cookies;
    }

    /**
     * 获取cookie列表
     */
    public List<String> getCookieList() {
        return cookielist;
    }

    /**
     * 设置异常
     */
    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 获取异常
     */
    public Throwable getThrowable() {
        return throwable;
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
        }
        return null;
    }
}
