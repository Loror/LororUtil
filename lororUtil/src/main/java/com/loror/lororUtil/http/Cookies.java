package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

import java.util.HashMap;

public class Cookies {

    protected HashMap<String, String> cookies = new HashMap<>();

    public void putCookie(String key, String value) {
        cookies.put(key, value);
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    public void parse(String cookieStr) {
        if (TextUtil.isEmpty(cookieStr)) {
            return;
        }
        String[] cookies = cookieStr.split(";");
        for (int i = 0; i < cookies.length; i++) {
            String cookie = cookies[i];
            if (cookie == null) {
                continue;
            }
            try {
                cookie = cookie.trim();
                String[] keyValue = cookie.split("\\=");
                if (keyValue.length == 1) {
                    this.cookies.put(keyValue[0], null);
                } else {
                    this.cookies.put(keyValue[0], cookie.substring(keyValue[0].length() + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String key : cookies.keySet()) {
            builder.append(key)
                    .append("=")
                    .append(cookies.get(key))
                    .append(";");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
