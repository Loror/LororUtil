package com.loror.lororUtil.example;

import com.loror.lororUtil.http.api.ApiTask;
import com.loror.lororUtil.http.api.ReturnAdapter;

import java.lang.reflect.Type;

import rx.Observable;
import rx.Subscriber;

public class ObservableAdapter implements ReturnAdapter {

    @Override
    public boolean filterType(Type type, Class<?> rawType) {
        return rawType == rx.Observable.class;
    }

    @Override
    public Object returnAdapter(final ApiTask apiTask) {
        return rx.Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                Object data = apiTask.execute();
                //无效请求
                if (data == null) {
                    return;
                }
                if (data instanceof Throwable) {
                    subscriber.onError((Throwable) data);
                } else {
                    subscriber.onNext(data);
                }
            }
        });
    }
}
