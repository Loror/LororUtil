package com.loror.lororUtil.http;

import com.loror.lororUtil.convert.UrlUtf8Util;
import com.loror.lororUtil.text.TextUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * post提交参数类
 */
public class RequestParams {

    private final Map<String, String> headers = new HashMap<String, String>();
    private Map<String, Object> params = new HashMap<String, Object>();
    private List<StreamBody> files;
    private String json;

    private RequestConverter getConverter, postConverter;
    private PacketConverter packetConverter;
    private BodyConverter bodyConverter;
    private SpliceConverter spliceConverter;

    private boolean userMultiForPost;
    private boolean useDefaultConverterInPost;
    private boolean asJson;
    private boolean gzip;
    private boolean useQueryForPost;
    private String contentTransferEncoding;

    private static boolean defaultUseDefaultConverterInPost = false;
    private static boolean defaultNullToEmpty = true;
    private static boolean defaultUseMultiForPost = false;

    private static final RequestConverter defaultConverter = new RequestConverter() {
        @Override
        public String convert(String key, String value) {
            return value == null ? null : UrlUtf8Util.toUrlString(value);
        }
    };

    /**
     * 设置contentTransferEncoding
     */
    public void setContentTransferEncoding(String contentTransferEncoding) {
        this.contentTransferEncoding = contentTransferEncoding;
    }

    /**
     * 设置是否post参数添加到url中提交
     */
    public void setUseQueryForPost(boolean useQueryForPost) {
        this.useQueryForPost = useQueryForPost;
    }

    /**
     * 是否post参数添加到url中提交
     */
    public boolean isUseQueryForPost() {
        return useQueryForPost;
    }

    /**
     * 是否使用表单提交post
     */
    public boolean isUseMultiForPost() {
        return defaultUseMultiForPost || userMultiForPost;
    }

    /**
     * 设置是否使用表单提交post
     */
    public void setUserMultiForPost(boolean userMultiForPost) {
        this.userMultiForPost = userMultiForPost;
    }

    /**
     * 设置提交方式为json
     */
    public void setAsJson(boolean asJson) {
        this.asJson = asJson;
    }

    /**
     * 是否已设置提交方式为json
     */
    public boolean isAsJson() {
        return asJson;
    }

    /**
     * 设置是否使用gzip压缩上传
     */
    public void setGzip(boolean gzip) {
        this.gzip = gzip;
        if (gzip) {
            headers.put("Content-Encoding", "gzip");
        } else if ("gzip".equals(headers.get("Content-Encoding"))) {
            headers.remove("Content-Encoding");
        }
    }

    /**
     * 是否使用gzip压缩上传
     */
    public boolean isGzip() {
        return gzip;
    }

    /**
     * 设置是否默认使用表单提交post
     */
    public static void setDefaultUseMultiForPost(boolean defaultUseMultiForPost) {
        RequestParams.defaultUseMultiForPost = defaultUseMultiForPost;
    }

    /**
     * 设置是否对post请求参数url编码
     */
    public static void setDefaultUseDefaultConverterInPost(boolean defaultUseDefaultConverterInPost) {
        RequestParams.defaultUseDefaultConverterInPost = defaultUseDefaultConverterInPost;
    }

    /**
     * 设置是否默认对post请求参数url编码
     */
    public void setUseDefaultConverterInPost(boolean useDefaultConverterInPost) {
        this.useDefaultConverterInPost = useDefaultConverterInPost;
    }

    /**
     * 是否为post开启默认转换器
     */
    public boolean isUseDefaultConverterInPost() {
        return defaultUseDefaultConverterInPost || useDefaultConverterInPost;
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
    public List<StreamBody> getFiles() {
        return files == null ? new ArrayList<StreamBody>() : files;
    }

    /**
     * 添加请求头
     */
    public RequestParams addHeader(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * 添加Cookie
     */
    public RequestParams addCookies(Cookies cookies) {
        if (cookies != null) {
            String oldCookie = headers.get("Cookie");
            if (!TextUtil.isEmpty(oldCookie)) {
                Cookies old = new Cookies().parse(oldCookie);
                for (String key : cookies.keys()) {
                    old.addCookie(key, cookies.getCookie(key));
                }
                cookies = old;
            }
            headers.put("Cookie", cookies.toString());
        }
        return this;
    }

    /**
     * 获取header
     */
    public String getHeader(String key) {
        return headers.get(key);
    }

    /**
     * 获取所有header
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 添加参数
     */
    public RequestParams fromKeyValue(String params) {
        if (params != null) {
            String[] keyValues = params.split("\\&");
            for (int i = 0; i < keyValues.length; i++) {
                String[] keyValue = keyValues[i].split("\\=");
                if (keyValue.length > 1) {
                    addParams(keyValue[0], keyValues[i].substring(keyValues[i].indexOf("=") + 1));
                } else {
                    addParams(keyValue[0], "");
                }
            }
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams fromObject(Object object) {
        if (object instanceof Map) {
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                if (key == null) {
                    continue;
                }
                Object value = map.get(key);
                if (value instanceof File) {
                    addParams(key.toString(), new FileBody(((File) value).getAbsolutePath(), ((File) value).getName()));
                } else if (value instanceof StreamBody) {
                    addParams(key.toString(), (StreamBody) value);
                } else if (value instanceof Number || value instanceof Boolean ||
                        value instanceof Primitive) {
                    params.put(key.toString(), value);
                } else {
                    params.put(key.toString(), value == null ? null : String.valueOf(value));
                }
            }
        } else if (object != null) {
            Class<?> handlerType = object.getClass();
            Field[] fields = handlerType.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                //transient|static修饰(10001000)字段，放弃
                if ((field.getModifiers() & 136) != 0) {
                    continue;
                }
                try {
                    String key = field.getName();
                    Object value = field.get(object);
                    Class<?> type = field.getType();
                    if (type == File.class) {
                        addParams(key, value == null ? new FileBody(null, key) : new FileBody(((File) object).getAbsolutePath(), ((File) object).getName()));
                    } else if (type == StreamBody.class) {
                        addParams(key, value == null ? new FileBody(null, key) : (StreamBody) value);
                    } else if (type.isPrimitive() || type == String.class
                            || value instanceof Number || value instanceof Primitive) {
                        params.put(key, value);
                    } else {
                        params.put(key, value == null ? null : String.valueOf(value));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    /**
     * 设置提交json数据，设置后将使用json方式提交该json，param参数将失效
     */
    public void setJson(String json) {
        this.json = json;
        asJson = json != null;
    }

    /**
     * 提交的json数据
     */
    public String getJson() {
        return json;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, Object[] value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, boolean value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, int value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, long value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, float value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, double value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
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
    public RequestParams addParams(String key, Primitive value) {
        if (key != null) {
            params.put(key, value);
        }
        return this;
    }

    /**
     * 添加参数
     */
    public RequestParams addParams(String key, StreamBody body) {
        body.setKey(key);
        if (files == null) {
            files = new ArrayList<StreamBody>();
        }
        files.add(body);
        return this;
    }

    /**
     * 移除参数
     */
    public void removeParam(String key) {
        params.remove(key);
    }

    /**
     * 获取参数
     */
    public Object getParam(String key) {
        return params.get(key);
    }

    /**
     * 获取所有参数
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * 替换所有参数
     */
    public void setParams(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        this.params = params;
    }

    /**
     * 设置打包转换器，设置后get/post转换器将失效
     */
    public void setPacketConverter(PacketConverter packetConverter) {
        this.packetConverter = packetConverter;
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
    public void setBodyConverter(BodyConverter bodyConverter) {
        this.bodyConverter = bodyConverter;
    }

    /**
     * 设置拼接符
     */
    public void setSpliceConverter(SpliceConverter spliceConverter) {
        this.spliceConverter = spliceConverter;
    }

    /**
     * 获取拼接符
     * type 0,1,2
     */
    public String getSplicing(String url, int type) {
        switch (type) {
            case 0:
                return spliceConverter != null ? spliceConverter.convert(url, 0) : (url.contains("?") ? "&" : "?");
            case 1:
                return spliceConverter != null ? spliceConverter.convert(url, 1) : "=";
            case 2:
                return spliceConverter != null ? spliceConverter.convert(url, 2) : "&";
        }
        return "";
    }

    /**
     * 打包参数
     */
    protected String packetOutParams(String method) {
        String str = "";
        //以json形式提交
        if ("POST".equals(method) && asJson) {
            if (json != null) {
                str = json;
            } else {
                str = packetConverter != null ? packetConverter.convert(method, params) : getJson(params);
            }
        } else {
            //使用外部打包器
            if (packetConverter != null) {
                str = packetConverter.convert(method, params);
            } else {
                StringBuilder builder = new StringBuilder();
                for (String key : params.keySet()) {
                    Object value = params.get(key);
                    if (value != null && value.getClass().isArray()) {
                        Object[] values = (Object[]) value;
                        for (int i = 0; i < values.length; i++) {
                            appendValue(method, builder, key, values[i] == null ? null : String.valueOf(values[i]));
                        }
                    } else {
                        appendValue(method, builder, key, value == null ? null : String.valueOf(value));
                    }
                }
                if (builder.length() > 0) {
                    if (!"POST_MULTI".equals(method)) {
                        str = builder.deleteCharAt(builder.length() - 1).toString();
                    } else {
                        str = builder.toString();
                    }
                }
            }
        }
        return bodyConverter == null ? str : bodyConverter.convert(method, str);
    }

    /**
     * 创建json
     */
    public static String getJson(Map<String, Object> params) {
        StringBuilder builder = new StringBuilder("{");
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value == null) {
                builder.append("\"")
                        .append(key)
                        .append("\":null");
            } else if (value instanceof Number || value instanceof Boolean ||
                    value instanceof Primitive) {
                builder.append("\"")
                        .append(key)
                        .append("\":")
                        .append(value);
            } else if (value.getClass().isArray()) {
                Object[] values = (Object[]) value;
                builder.append("\"")
                        .append(key)
                        .append("\":[");
                for (int i = 0; i < values.length; i++) {
                    Object val = values[i];
                    if (val == null) {
                        builder.append("null");
                    } else if (val instanceof Number || val instanceof Boolean ||
                            val instanceof Primitive) {
                        builder.append(val);
                    } else {
                        builder.append("\"")
                                .append(val)
                                .append("\"");
                    }
                    if (i != values.length - 1) {
                        builder.append(",");
                    }
                }
                builder.append("]");
            } else {
                builder.append("\"")
                        .append(key)
                        .append("\":\"")
                        .append(value)
                        .append("\"");
            }
            builder.append(",");
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * 最终拼接
     */
    private final void appendValue(String method, StringBuilder sb, String key, String value) {
        if (defaultNullToEmpty && value == null) {
            value = "";
        }
        String contentValue;
        switch (method) {
            case "GET":
                contentValue = getConverter == null ? defaultConverter.convert(key, value) : getConverter.convert(key, value);
                sb.append(key)
                        .append(getSplicing(null, 1))
                        .append(contentValue)
                        .append(getSplicing(null, 2));
                break;
            case "POST":
                contentValue = postConverter == null ? (isUseDefaultConverterInPost() ? defaultConverter.convert(key, value) : value) : postConverter.convert(key, value);
                sb.append(key)
                        .append("=")
                        .append(contentValue)
                        .append("&");
                break;
            case "POST_MULTI":
                contentValue = postConverter == null ? (isUseDefaultConverterInPost() ? defaultConverter.convert(key, value) : value) : postConverter.convert(key, value);
                sb.append(MultipartConfig.PREFIX);
                sb.append(MultipartConfig.BOUNDARY);
                sb.append(MultipartConfig.LINEEND);
                sb.append("Content-Disposition: form-data; name=\"").append(key).append("\"" + MultipartConfig.LINEEND);
                sb.append("Content-Type: text/plain; charset=UTF-8" + MultipartConfig.LINEEND);
                if (TextUtil.isEmpty(contentTransferEncoding)) {
                    sb.append("Content-Transfer-Encoding: 8bit" + MultipartConfig.LINEEND);
                } else {
                    sb.append("Content-Transfer-Encoding: ").append(contentTransferEncoding).append(MultipartConfig.LINEEND);
                }
                sb.append(MultipartConfig.LINEEND);
                sb.append(contentValue);
                sb.append(MultipartConfig.LINEEND);
                break;
        }
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("{");
        buffer.append("[headers:");
        if (headers.size() > 0) {
            for (String o : headers.keySet()) {
                buffer.append(o)
                        .append("=")
                        .append(headers.get(o))
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
                Object value = params.get(o);
                if (value != null && value.getClass().isArray()) {
                    Object[] values = (Object[]) value;
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
            }
            buffer.deleteCharAt(buffer.length() - 1);
        } else {
            buffer.append("empty");
        }
        buffer.append("],");
        if (json != null) {
            buffer.append("[json:");
            buffer.append(json);
            buffer.append("],");
        }
        buffer.append("[files:");
        if (files != null && files.size() > 0) {
            for (StreamBody streamBody : files) {
                buffer.append(streamBody.getKey())
                        .append("=")
                        .append(streamBody.getName())
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
