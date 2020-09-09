package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

import java.util.HashMap;
import java.util.Set;

public class Cookies {

    protected HashMap<String, String> cookies = new HashMap<>();

    public Cookies addCookie(String key, String value) {
        cookies.put(key, value);
        return this;
    }

    public String getCookie(String key) {
        return cookies.get(key);
    }

    public Set<String> keys() {
        return cookies.keySet();
    }

    public Cookies parse(String cookieStr) {
        if (TextUtil.isEmpty(cookieStr)) {
            return this;
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
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        boolean top = true;
        for (String key : cookies.keySet()) {
            String value = cookies.get(key);
            if (top) {
                top = false;
            } else {
                builder.append(" ");
            }
            builder.append(key)
                    .append("=")
                    .append(value == null ? "" : value)
                    .append(";");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
