package com.loror.lororUtil.http;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * post提交参数类
 */
public class RequestParams {
    protected HashMap<String, String> parmas = new HashMap<String, String>();
    protected List<FileBody> files = new ArrayList<>();
    protected String[] head = new String[2];
    protected RequestConverter getConverter, postConverter, bodyConverter;

    /**
     * 设置请求头
     */
    public void setHeader(String name, String value) {
        head[0] = name;
        head[1] = value;
    }

    /**
     * 添加参数
     */
    public RequestParams fromKeyValue(String params) {
        String[] keyValues = params.split("\\&");
        for (int i = 0; i < keyValues.length; i++) {
            String[] keyValue = keyValues[i].split("\\=");
            if (keyValue.length > 1) {
                addParams(keyValue[0], keyValues[i].substring(keyValues[i].indexOf("=") + 1));
            }
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams fromObject(Object object) {
        Class<?> handlerType = object.getClass();
        Field[] fields = handlerType.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                String key = field.getName();
                Object value = field.get(object);
                Class<?> type = field.getDeclaringClass();
                if (type == File.class) {
                    addParams(key, value == null ? new FileBody(null, key) : new FileBody(((File) object).getAbsolutePath(), ((File) object).getName()));
                } else if (type == FileBody.class) {
                    addParams(key, value == null ? new FileBody(null, key) : (FileBody) value);
                } else {
                    addParams(key, value == null ? "" : String.valueOf(value));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, Object[] value) {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("array{");
        for (int i = 0; i < value.length; i++) {
            stringBuffer.append(String.valueOf(value[i]))
                    .append(",");
        }
        stringBuffer.append("}");
        return addParams(key, stringBuffer.toString());
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, boolean value) {
        return addParams(key, String.valueOf(value));
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, long value) {
        return addParams(key, String.valueOf(value));
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, double value) {
        return addParams(key, String.valueOf(value));
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, String value) {
        if (key != null) {
            parmas.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, FileBody file) {
        file.setKey(key);
        files.add(file);
        return this;
    }

    /**
     * 获取参数
     */
    public String getParma(String key) {
        return parmas.get(key);
    }

    /**
     * 获取所有参数
     */
    public HashMap<String, String> getParmas() {
        return parmas;
    }

    /**
     * 全局转换
     */
    public void setGetConverter(RequestConverter getConverter) {
        this.getConverter = getConverter;
    }

    /**
     * 全局转换
     */
    public void setPostConverter(RequestConverter postConverter) {
        this.postConverter = postConverter;
    }

    /**
     * 全局转换
     */
    public void setBodyConverter(RequestConverter bodyConverter) {
        this.bodyConverter = bodyConverter;
    }

    /**
     * 打包参数
     */
    protected String packetOutParams(String method) {
        String str = "";
        StringBuilder sb = new StringBuilder();
        for (String o : parmas.keySet()) {
            String value = parmas.get(o);
            if (value != null && value.startsWith("array{")) {
                value = value.substring(6, value.length() - 1);
                String[] values = value.split(",");
                if (values.length > 0) {
                    for (int i = 0; i < values.length; i++) {
                        append(method, sb, o, values[i]);
                    }
                }
            } else {
                append(method, sb, o, value);
            }
        }
        if (sb.length() > 0) {
            if (!"POST_FILE".equals(method)) {
                str = sb.substring(0, sb.length() - 1);
            } else {
                str = sb.toString();
            }
        }
        return bodyConverter == null ? str : bodyConverter.convert(method, str);
    }

    private final void append(String method, StringBuilder sb, String key, String value) {
        switch (method) {
            case "GET":
                sb.append(key)
                        .append("=")
                        .append(getConverter == null ? value : getConverter.convert(key, value))
                        .append("&");
                break;
            case "POST":
                sb.append(key)
                        .append("=")
                        .append(postConverter == null ? value : postConverter.convert(key, value))
                        .append("&");
                break;
            case "POST_FILE":
                sb.append(Config.PREFIX);
                sb.append(Config.BOUNDARY);
                sb.append(Config.LINEND);
                sb.append("Content-Disposition: form-data; name=\"" + key + "\"" + Config.LINEND);
                sb.append("Content-Type: text/plain; charset=UTF-8" + Config.LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + Config.LINEND);
                sb.append(Config.LINEND);
                sb.append(postConverter == null ? value : postConverter.convert(key, value));
                sb.append(Config.LINEND);
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[");
        for (String o : parmas.keySet()) {
            buffer.append(o)
                    .append("=")
                    .append(parmas.get(o))
                    .append(",");
        }
        for (FileBody fileBody : files) {
            buffer.append(fileBody.getKey())
                    .append("=")
                    .append(fileBody.getName())
                    .append(",");
        }
        if (buffer.length() > 1) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        buffer.append("]");
        return buffer.toString();
    }
}
