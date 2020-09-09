package com.loror.lororUtil.http;

import com.loror.lororUtil.text.TextUtil;

public class SetCookie {

    private String name;
    private String value;
    private String expires;
    private String maxAge;
    private String path;
    private String domain;
    private boolean secure;
    private boolean httpOnly;
    private String origin;

    public SetCookie(String origin) {
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public String getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(String maxAge) {
        this.maxAge = maxAge;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean getSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean getHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getOrigin() {
        return origin;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", expires='" + expires + '\'' +
                ", maxAge='" + maxAge + '\'' +
                ", path='" + path + '\'' +
                ", domain='" + domain + '\'' +
                ", secure=" + secure +
                ", httpOnly=" + httpOnly +
                ", origin='" + origin + '\'';
    }

    public static SetCookie parse(String cookie) {
        if (TextUtil.isEmpty(cookie)) {
            return null;
        }

        SetCookie setCookie = new SetCookie(cookie);
        String[] cookies = cookie.split(";");
        for (int i = 0; i < cookies.length; i++) {
            String item = cookies[i];
            if (item == null) {
                continue;
            }
            try {
                item = item.trim();
                String[] keyValue = item.split("\\=");
                String key = keyValue[0];
                String value = null;
                if (keyValue.length > 1) {
                    value = item.substring(keyValue[0].length() + 1);
                }
                switch (key.toLowerCase()) {
                    case "expires":
                        setCookie.expires = value;
                        break;
                    case "max-age":
                        setCookie.maxAge = value;
                        break;
                    case "path":
                        setCookie.path = value;
                        break;
                    case "domain":
                        setCookie.domain = value;
                        break;
                    case "secure":
                        setCookie.secure = true;
                        break;
                    case "httponly":
                        setCookie.httpOnly = true;
                        break;
                    default: {
                        if (setCookie.name == null) {
                            setCookie.name = key;
                            setCookie.value = value;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return setCookie;
    }
}
