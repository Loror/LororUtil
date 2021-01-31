package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;

public class ApiResult {

    //基本信息
    protected String url;
    protected RequestParams params;
    protected Responce responce;
    protected ApiTask apiTask;

    protected boolean accept;//是否已经重新请求
    protected boolean isMock;//是否为mock数据

    //框架需使用的参数及拦截所需参数
    protected Object responceObject;

    public String getUrl() {
        return url;
    }

    public RequestParams getParams() {
        return params;
    }

    public Responce getResponce() {
        return responce;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setResponceObject(Object responce) {
        this.responceObject = responce;
    }

    /**
     * 通知重新请求，结束后请求将接收到新的结果
     * 仅对内部异步方式有效
     */
    public void requestAgain() {
        apiTask.requestAgain(this);
    }

}
