package com.loror.lororUtil.http.api;

import com.loror.lororUtil.annotation.HeaderObject;
import com.loror.lororUtil.http.Cookies;
import com.loror.lororUtil.http.FileBody;
import com.loror.lororUtil.http.StreamBody;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.Primitive;
import com.loror.lororUtil.http.ProgressListener;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.text.TextUtil;
import com.loror.lororUtil.annotation.AsJson;
import com.loror.lororUtil.annotation.Cookie;
import com.loror.lororUtil.annotation.DefaultHeaders;
import com.loror.lororUtil.annotation.DefaultParams;
import com.loror.lororUtil.annotation.Gzip;
import com.loror.lororUtil.annotation.Header;
import com.loror.lororUtil.annotation.Multipart;
import com.loror.lororUtil.annotation.Param;
import com.loror.lororUtil.annotation.ParamJson;
import com.loror.lororUtil.annotation.ParamKeyValue;
import com.loror.lororUtil.annotation.ParamObject;
import com.loror.lororUtil.annotation.Path;
import com.loror.lororUtil.annotation.Query;
import com.loror.lororUtil.annotation.Url;
import com.loror.lororUtil.annotation.UrlEnCode;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ApiRequest {

    private String baseUrl, anoBaseUrl;
    private RequestParams params;
    protected HttpClient client;
    private int type;//1,get;2,post;3,delete;4,put
    private String url;
    protected ProgressListener progressListener;
    private boolean keepStream;
    protected int useTimes;//计数Request使用次数
    protected String apiName;
    protected int mockType;
    protected String mockData;
    protected boolean intercept;
    private List<UrlPath> querys;//Query注解指定的参数
    private List<UrlPath> paths;//Path注解指定的参数
    private String anoUrl;//Url指定的url地址
    private boolean anoUseValueUrl;

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setAnoBaseUrl(String anoBaseUrl) {
        this.anoBaseUrl = anoBaseUrl;
    }

    public String getAnoBaseUrl() {
        return anoBaseUrl;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return url;
    }

    public void intercept() {
        this.intercept = true;
    }

    /**
     * 获取最终url
     */
    public String getUrl() {
        String finalBaseUrl = !TextUtil.isEmpty(anoBaseUrl) ? anoBaseUrl
                : !TextUtil.isEmpty(baseUrl) ? baseUrl :
                "";
        String finalUrl = finalBaseUrl + url;
        if (!TextUtil.isEmpty(anoUrl)) {
            if (anoUseValueUrl) {
                finalUrl += anoUrl;
            } else {
                finalUrl = anoUrl;
            }
        }
        if (paths != null) {
            for (UrlPath path : paths) {
                finalUrl = finalUrl.replace("{" + path.name + "}", path.value);
            }
        }
        if (querys != null) {
            StringBuilder builder = new StringBuilder();
            for (UrlPath query : querys) {
                builder.append(query.name)
                        .append(params != null ? params.getSplicing(null, 1) : "=")
                        .append(query.value)
                        .append(params != null ? params.getSplicing(null, 2) : "&");
            }
            if (builder.length() > 0) {
                builder.deleteCharAt(builder.length() - 1);
                finalUrl += params != null ? params.getSplicing(finalUrl, 0) : (finalUrl.contains("?") ? "&" : "?");
                finalUrl += builder.toString();
            }
        }
        return finalUrl;
    }

    public void setKeepStream(boolean keepStream) {
        this.keepStream = keepStream;
    }

    public boolean isKeepStream() {
        return keepStream;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public int getUseTimes() {
        return useTimes;
    }

    public String getApiName() {
        return apiName;
    }

    public RequestParams getParams() {
        return params;
    }

    public HttpClient getClient() {
        return client;
    }

    /**
     * 创建RequestParams
     */
    protected void generateParams(Method method, Object[] args) {
        params = new RequestParams();
        DefaultParams defaultParams = method.getAnnotation(DefaultParams.class);
        if (defaultParams != null) {
            int size = Math.min(defaultParams.keys().length, defaultParams.values().length);
            Class<?>[] types = defaultParams.types();
            for (int i = 0; i < size; i++) {
                String key = defaultParams.keys()[i];
                String value = defaultParams.values()[i];
                if (i < types.length) {
                    Class<?> type = types[i];
                    if (type.isPrimitive()) {
                        if (type == byte.class || type == short.class || type == int.class) {
                            params.addParams(key, Integer.parseInt(value));
                        } else if (type == long.class) {
                            params.addParams(key, Long.parseLong(value));
                        } else if (type == float.class) {
                            params.addParams(key, Float.parseFloat(value));
                        } else if (type == double.class) {
                            params.addParams(key, Double.parseDouble(value));
                        } else if (type == boolean.class) {
                            params.addParams(key, Boolean.parseBoolean(value));
                        }
                        continue;
                    }
                }
                params.addParams(key, value);
            }
        }
        DefaultHeaders defaultHeaders = method.getAnnotation(DefaultHeaders.class);
        if (defaultHeaders != null) {
            int size = Math.min(defaultHeaders.keys().length, defaultHeaders.values().length);
            for (int i = 0; i < size; i++) {
                params.addHeader(defaultHeaders.keys()[i], defaultHeaders.values()[i]);
            }
        }
        Multipart multipart = method.getAnnotation(Multipart.class);
        if (multipart != null) {
            params.setForceMultiForPostOrPut(true);
        }
        AsJson asJson = method.getAnnotation(AsJson.class);
        if (asJson != null) {
            params.setAsJson(true);
        }
        UrlEnCode urlEnCode = method.getAnnotation(UrlEnCode.class);
        if (urlEnCode != null) {
            params.setUrlEncodeForPostOrPut(true);
        }
        Gzip gzip = method.getAnnotation(Gzip.class);
        if (gzip != null) {
            params.setGzip(true);
        }
        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (types[i] == RequestParams.class) {
                Map<String, Object> old = params.getParams();
                List<StreamBody> oldFile = params.getFiles();
                params = (RequestParams) args[i];
                if (old.size() > 0) {
                    for (String key : old.keySet()) {
                        Object value = old.get(key);
                        //部分map不支持value为空，出现错误为使用这替换了错误的map类型
                        params.getParams().put(key, value);
//                        addObject(params, key, value);
                    }
                }
                if (oldFile.size() > 0) {
                    for (StreamBody file : oldFile) {
                        params.addParams(file.getKey(), file);
                    }
                }
            } else {
                addField(params, annotations[i], types[i], args[i]);
            }
        }
    }

    /**
     * 添加Field
     */
    private void addField(RequestParams params, Annotation[] annotations, Class<?> type, Object arg) {
        if (annotations == null) {
            return;
        }
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == Param.class) {
                String name = ((Param) annotations[i]).value();
                if (type == StreamBody.class) {
                    params.addParams(name, (StreamBody) arg);
                } else if (type == File.class) {
                    params.addParams(name, new FileBody(arg == null ? null : ((File) arg).getAbsolutePath()));
                } else if (type.isArray()) {
                    if (arg != null) {
                        Object[] array = (Object[]) arg;
                        addArray(params, name, array, type.getComponentType());
                    }
                } else if (arg instanceof List) {
                    List<?> list = (List<?>) arg;
                    if (list.size() > 0) {
                        Object[] array = list.toArray(new Object[0]);
                        Class<?> componentType = null;
                        for (Object o : array) {
                            if (o != null) {
                                if (componentType == null) {
                                    componentType = o.getClass();
                                } else if (!ClassUtil.instanceOf(o.getClass(), componentType) &&
                                        !ClassUtil.instanceOf(componentType, o.getClass())) {
                                    throw new IllegalArgumentException("List中只能包含一种类型数据");
                                }
                            }
                        }
                        addArray(params, name, array, componentType);
                    }
                } else {
                    addObject(params, name, arg);
                }
                break;
            } else if (annotations[i].annotationType() == Cookie.class) {
                String name = ((Cookie) annotations[i]).value();
                params.addCookies(new Cookies().addCookie(name, arg == null ? null : String.valueOf(arg)));
                break;
            } else if (annotations[i].annotationType() == ParamObject.class) {
                if (arg != null) {
                    params.fromObject(arg);
                }
                break;
            } else if (annotations[i].annotationType() == ParamKeyValue.class) {
                if (arg != null) {
                    params.fromKeyValue(arg.toString());
                }
                break;
            } else if (annotations[i].annotationType() == ParamJson.class) {
                if (arg != null) {
                    if (type == String.class) {
                        params.setJson((String) arg);
                    } else {
                        params.setJson(ApiClient.objectToString(arg));
                    }
                }
                break;
            } else if (annotations[i].annotationType() == Header.class) {
                String name = ((Header) annotations[i]).value();
                params.addHeader(name, arg == null ? "" : String.valueOf(arg));
                break;
            } else if (annotations[i].annotationType() == HeaderObject.class) {
                if (arg != null) {
                    params.fromHeaderObject(arg);
                }
                break;
            } else if (annotations[i].annotationType() == Query.class) {
                String name = ((Query) annotations[i]).value();
                if (querys == null) {
                    querys = new LinkedList<>();
                }
                if (type.isArray()) {
                    if (arg != null) {
                        Object[] array = (Object[]) arg;
                        for (Object o : array) {
                            UrlPath path = new UrlPath(name, o);
                            querys.add(path);
                        }
                    }
                } else if (arg instanceof List) {
                    List<?> list = (List<?>) arg;
                    if (list.size() > 0) {
                        Object[] array = list.toArray(new Object[0]);
                        for (Object o : array) {
                            UrlPath path = new UrlPath(name, o);
                            querys.add(path);
                        }
                    }
                } else {
                    UrlPath path = new UrlPath(name, arg);
                    querys.add(path);
                }
                break;
            } else if (annotations[i].annotationType() == Path.class) {
                String name = ((Path) annotations[i]).value();
                if (paths == null) {
                    paths = new LinkedList<>();
                }
                UrlPath path = new UrlPath(name, arg);
                paths.add(path);
                break;
            } else if (annotations[i].annotationType() == Url.class) {
                if (!TextUtil.isEmpty(anoUrl)) {
                    throw new IllegalArgumentException("只能指定一个Url注解");
                }
                anoUseValueUrl = ((Url) annotations[i]).useValueUrl();
                anoUrl = arg == null ? "" : String.valueOf(arg);
                break;
            }
        }
    }

    /**
     * 添加参数到param
     */
    private void addObject(RequestParams params, String name, Object value) {
        if (value == null) {
            params.addParams(name, (String) null);
        } else if (value instanceof Integer) {
            params.addParams(name, (Integer) value);
        } else if (value instanceof Long) {
            params.addParams(name, (Long) value);
        } else if (value instanceof Float) {
            params.addParams(name, (Float) value);
        } else if (value instanceof Double) {
            params.addParams(name, (Double) value);
        } else if (value instanceof Boolean) {
            params.addParams(name, (Boolean) value);
        } else if (value instanceof Primitive) {
            params.addParams(name, (Primitive) value);
        } else {
            params.addParams(name, String.valueOf(value));
        }
    }

    /**
     * 添加数组形参数到param
     */
    private void addArray(RequestParams params, String name, Object[] array, Class<?> componentType) {
        if (componentType == StreamBody.class) {
            for (Object o : array) {
                params.addParams(name, (StreamBody) o);
            }
        } else if (componentType == File.class) {
            for (Object o : array) {
                params.addParams(name, new FileBody(o == null ? null : ((File) o).getAbsolutePath()));
            }
        } else {
            params.addParams(name, array);
        }
    }
}
