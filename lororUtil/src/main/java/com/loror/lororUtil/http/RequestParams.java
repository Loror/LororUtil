package com.loror.lororUtil.http;

import com.loror.lororUtil.convert.UrlUtf8Util;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * post提交参数类
 */
public class RequestParams {

    private final String JSONKEY = "RequestParamsAsJson";
    private HashMap<String, String> params = new HashMap<String, String>();
    private HashMap<String, Object[]> arrayParams;
    private List<FileBody> files;
    protected HashMap<String, String> head = new HashMap<String, String>();
    private RequestConverter getConverter, postConverter, bodyConverter;
    private SplicingConverter splicingConverter;
    private static boolean useDefaultConverterInPost = false;
    private static boolean defaultNullToEmpty = true;
    private static boolean defaultUserFormForPost = false;
    private boolean userFormForPost = false;
    private static RequestConverter defaultConverter = new RequestConverter() {
        @Override
        public String convert(String key, String value) {
            return value == null ? null : UrlUtf8Util.toUrlString(value);
        }
    };

    /**
     * 设置是否使用表单提交post
     */
    public void setUserFormForPost(boolean userFormForPost) {
        this.userFormForPost = userFormForPost;
    }

    /**
     * 设置是否默认使用表单提交post
     */
    public static void setDefaultUserFormForPost(boolean defaultUserFormForPost) {
        RequestParams.defaultUserFormForPost = defaultUserFormForPost;
    }

    /**
     * 设置是否默认对post请求参数url编码
     */
    public static void setUseDefaultConverterInPost(boolean useDefaultConverterInPost) {
        RequestParams.useDefaultConverterInPost = useDefaultConverterInPost;
    }

    /**
     * 设置是否默认将null转化成“”
     */
    public static void setDefaultNullToEmpty(boolean defaultNullToEmpty) {
        RequestParams.defaultNullToEmpty = defaultNullToEmpty;
    }

    /**
     * 获取所有文件
     */
    public List<FileBody> getFiles() {
        return files == null ? new ArrayList<FileBody>() : files;
    }

    /**
     * 添加请求头
     */
    public RequestParams addHeader(String name, String value) {
        head.put(name, value);
        return this;
    }

    /**
     * 获取所有header
     */
    public HashMap<String, String> getHeader() {
        return head;
    }

    /**
     * 添加参数
     */
    public void asJson(String json) {
        params.put(JSONKEY, json);
    }

    /**
     * 是否已设置提交方式为json
     */
    public boolean isAsJson() {
        return params.containsKey(JSONKEY);
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
            } else {
                addParams(keyValue[0], "");
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
            //transient|static修饰(10001000)，放弃
            if ((field.getModifiers() & 136) != 0) {
                continue;
            }
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
        if (value != null) {
            if (arrayParams == null) {
                arrayParams = new HashMap<String, Object[]>();
            }
            arrayParams.put(key, value);
            return addParams(key, "array{@arrayParams}");
        } else {
            return this;
        }
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
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, FileBody file) {
        file.setKey(key);
        if (files == null) {
            files = new ArrayList<FileBody>();
        }
        files.add(file);
        return this;
    }

    /**
     * 获取参数
     */
    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * 获取所有参数
     */
    public HashMap<String, String> getParams() {
        return params;
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
     * 设置拼接符
     */
    public void setSplicingConverter(SplicingConverter splicingConverter) {
        this.splicingConverter = splicingConverter;
    }

    /**
     * 获取拼接符
     */
    protected String getSplicing(String url) {
        return splicingConverter != null ? splicingConverter.convert(url) : (url.contains("?") ? "&" : "?");
    }

    /**
     * 获取数组参数
     */
    public HashMap<String, Object[]> getArrayParams() {
        return arrayParams == null ? new HashMap<String, Object[]>() : arrayParams;
    }

    /**
     * 打包参数
     */
    protected String packetOutParams(String method) {
        if ("POST".equals(method) && params.containsKey(JSONKEY)) {
            return params.get(JSONKEY);
        }
        String str = "";
        StringBuilder sb = new StringBuilder();
        for (String o : params.keySet()) {
            if (JSONKEY.equals(o)) {
                continue;
            }//不拼接json
            String value = params.get(o);
            if ("array{@arrayParams}".equals(value) && arrayParams != null) {
                Object[] values = arrayParams.get(o);
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        append(method, sb, o, values[i] == null ? null : String.valueOf(values[i]));
                    }
                } else {
                    append(method, sb, o, value);
                }
            } else {
                append(method, sb, o, value);
            }
        }
        if (sb.length() > 0) {
            if (!"POST_FORM".equals(method)) {
                str = sb.deleteCharAt(sb.length() - 1).toString();
            } else {
                str = sb.toString();
            }
        }
        return bodyConverter == null ? str : bodyConverter.convert(method, str);
    }

    /**
     * 最终拼接
     */
    private final void append(String method, StringBuilder sb, String key, String value) {
        if (defaultNullToEmpty && value == null) {
            value = "";
        }
        switch (method) {
            case "GET":
                sb.append(key)
                        .append("=")
                        .append(getConverter == null ? defaultConverter.convert(key, value) : getConverter.convert(key, value))
                        .append("&");
                break;
            case "POST":
                if (defaultUserFormForPost || userFormForPost) {
                    sb.append(Config.PREFIX);
                    sb.append(Config.BOUNDARY);
                    sb.append(Config.LINEND);
                    sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"" + Config.LINEND);
                    sb.append("Content-Type: text/plain; charset=UTF-8" + Config.LINEND);
                    sb.append("Content-Transfer-Encoding: 8bit" + Config.LINEND);
                    sb.append(Config.LINEND);
                    sb.append(postConverter == null ? (useDefaultConverterInPost ? defaultConverter.convert(key, value) : value) : postConverter.convert(key, value));
                    sb.append(Config.LINEND);
                } else {
                    sb.append(key)
                            .append("=")
                            .append(postConverter == null ? (useDefaultConverterInPost ? defaultConverter.convert(key, value) : value) : postConverter.convert(key, value))
                            .append("&");
                }
                break;
            case "POST_FORM":
                sb.append(Config.PREFIX);
                sb.append(Config.BOUNDARY);
                sb.append(Config.LINEND);
                sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"" + Config.LINEND);
                sb.append("Content-Type: text/plain; charset=UTF-8" + Config.LINEND);
                sb.append("Content-Transfer-Encoding: 8bit" + Config.LINEND);
                sb.append(Config.LINEND);
                sb.append(postConverter == null ? (useDefaultConverterInPost ? defaultConverter.convert(key, value) : value) : postConverter.convert(key, value));
                sb.append(Config.LINEND);
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        buffer.append("[headers:");
        if (head.size() > 0) {
            for (String o : head.keySet()) {
                buffer.append(o)
                        .append("=")
                        .append(head.get(o))
                        .append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            buffer.append("empty");
        }
        buffer.append("],");
        buffer.append("[params:");
        if (params.size() > 0) {
            for (String o : params.keySet()) {
                String value = params.get(o);
                if ("array{@arrayParams}".equals(value) && arrayParams != null) {
                    Object[] values = arrayParams.get(o);
                    if (values != null) {
                        for (int i = 0; i < values.length; i++) {
                            buffer.append(o)
                                    .append("=")
                                    .append(values[i] == null ? null : String.valueOf(values[i]))
                                    .append(",");
                        }
                    } else {
                        buffer.append(o)
                                .append("=")
                                .append(params.get(o))
                                .append(",");
                    }
                } else {
                    buffer.append(o)
                            .append("=")
                            .append(params.get(o))
                            .append(",");
                }
            }
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            buffer.append("empty");
        }
        buffer.append("],");
        buffer.append("[files:");
        if (files != null && files.size() > 0) {
            for (FileBody fileBody : files) {
                buffer.append(fileBody.getKey())
                        .append("=")
                        .append(fileBody.getName())
                        .append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            buffer.append("empty");
        }
        buffer.append("]");
        buffer.append("}");
        return buffer.toString();
    }
}
