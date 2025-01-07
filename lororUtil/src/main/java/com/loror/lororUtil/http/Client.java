package com.loror.lororUtil.http;

public interface Client {

    int CORE_URL_CONNECTION = 0;
    int CORE_OKHTTP3 = 1;

    /**
     * 设置内核
     * */
    boolean setCore(int core);

    /**
     * post请求，参数，1，url地址，2，提交参数，无回调需开线程处理
     */
    Responce post(String urlStr, RequestParams parmas);

    /**
     * get请求，参数，1，url地址，2，提交参数，无回调需开线程处理
     */
    Responce get(String urlStr, RequestParams parmas);

    /**
     * put请求，参数，1，url地址，2，提交参数，无回调需开线程处理
     */
    Responce put(String urlStr, RequestParams parmas);

    /**
     * delete请求，参数，1，url地址，2，提交参数，无回调需开线程处理
     */
    Responce delete(String urlStr, RequestParams parmas);

    /**
     * 取消当前请求
     */
    boolean cancel();
}
