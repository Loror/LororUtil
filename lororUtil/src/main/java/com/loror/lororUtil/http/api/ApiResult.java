package com.loror.lororUtil.http.api;

import com.loror.lororUtil.http.Responce;

public class ApiResult {

    //基本信息
    protected String url;
    protected ApiRequest apiRequest;
    protected ApiTask apiTask;
    protected Responce responce;

    protected boolean accept;//是否已经重新请求

    //框架需使用的参数及拦截所需参数
    protected Object responceObject;

    public String getUrl() {
        return url;
    }

    public ApiRequest getApiRequest() {
        return apiRequest;
    }

    public Responce getResponce() {
        return responce;
    }

    public void setResponce(Responce responce) {
        this.responce = responce;
    }

    public void setAccept(boolean accept) {
        this.accept = accept;
    }

    public boolean isMock() {
        return apiRequest.mockType != 0;
    }

    public int getMockType() {
        return apiRequest.mockType;
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

    public ApiTask getApiTask() {
        return apiTask;
    }
}
