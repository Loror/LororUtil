package com.loror.lororUtil.http.api;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororUtil.http.ProgressListener;
import com.loror.lororUtil.http.Responce;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Observable<T> {

    private ApiTask apiTask;
    private ApiRequest apiRequest;
    private Observer<T> observer;
    protected ObservableManager observableManager;
    private static final ExecutorService server = Executors.newFixedThreadPool(3);

    public void setApiTask(ApiTask apiTask) {
        this.apiTask = apiTask;
    }

    protected void setApiRequest(ApiRequest apiRequest) {
        this.apiRequest = apiRequest;
    }

    public Observer<T> getObserver() {
        return observer;
    }

    /**
     * 监听进度，上传文件时有效
     */
    public Observable<T> listen(ProgressListener listener) {
        if (apiRequest != null) {
            apiRequest.progressListener = listener;
        }
        return this;
    }

    /**
     * 开始任务并提交监听
     */
    public Observable<T> subscribe(final Observer<T> observer) {
        this.observer = observer;
        if (observer == null) {
            return this;
        }
        if (observableManager != null) {
            observableManager.registerObservable(this);
        }
        server.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final Responce responce = apiTask.request();
                    ObjectPool.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (observableManager != null) {
                                observableManager.unRegisterObservable(Observable.this);
                            }
                            if (responce != null) {
                                Object result = apiTask.toResult(responce);
                                if (result instanceof Throwable) {
                                    observer.failed(responce.getCode(), (Throwable) result);
                                } else {
                                    observer.success((T) result);
                                }
                            }
                        }
                    });
                } catch (final Throwable t) {
                    ObjectPool.getInstance().getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (observableManager != null) {
                                observableManager.unRegisterObservable(Observable.this);
                            }
                            observer.failed(-1, t);
                        }
                    });
                }
            }
        });
        return this;
    }

    /**
     * 注册管理
     */
    public Observable<T> manage(ObservableManager observableManager) {
        this.observableManager = observableManager;
        return this;
    }

    /**
     * 注销监听
     */
    public void unSubscribe() {
        this.observer = null;
    }

    /**
     * 注销监听并关闭连接
     */
    public void cancel() {
        unSubscribe();
        if (apiRequest != null) {
            apiRequest.client.cancel();
        }
    }
}
