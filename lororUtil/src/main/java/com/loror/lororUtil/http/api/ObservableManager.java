package com.loror.lororUtil.http.api;

public interface ObservableManager {

    /**
     * 注册Observable
     */
    void registerObservable(Observable observable);

    /**
     * 注销Observable
     */
    void unRegisterObservable(Observable observable);
}
