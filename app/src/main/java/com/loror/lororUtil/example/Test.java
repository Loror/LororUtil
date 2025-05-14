package com.loror.lororUtil.example;

import android.os.Looper;
import android.util.Log;

import com.loror.lororUtil.asynctask.AsyncUtil;
import com.loror.lororUtil.asynctask.FlowTask;
import com.loror.lororUtil.http.BodyConverter;
import com.loror.lororUtil.http.Client;
import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororUtil.http.api.Observer;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Test {

    /**
     * api测试用例
     */
    public static void connectNetByApi() {
        final ApiTest apiTest = ApiCreator.getApiClient().create(ApiTest.class);
        //内置异步请求方式
        apiTest.test().subscribe(new Observer<>() {
            @Override
            public void success(String data) {
                Log.e("RESULT_1", "data->" + data);
            }

            @Override
            public void failed(int code, Throwable e) {
                Log.e("RESULT_1", "err:" + code);
            }
        });
        //同步请求方式
        AsyncUtil.excute(new AsyncUtil.Excute<String>() {
            @Override
            public String doBack() {
                return apiTest.test1();
            }

            @Override
            public void result(String result) {
                Log.e("RESULT_2", "data->" + result);
            }
        });
        //自定义异步请求方式
        apiTest.test2()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        Log.e("RESULT_3", "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("RESULT_3", "e:", e);
                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("RESULT_3", "data->" + s);
                    }
                });
    }

    /**
     * HttpClient测试用例
     */
    public static void connectNet() {
        HttpClient client = new HttpClient();
        client.setCore(Client.CORE_OKHTTP3);
        RequestParams params = new RequestParams();
        params.addParams("name", "xiao:ming");
        params.addParams("age", 10);
        params.addParams("young", true);
//        params.setBodyConverter(new BodyConverter() {
//            @Override
//            public String convert(String method, String form) {
//                return "change:" + form;
//            }
//        });
//        params.setAsJson(true);
        client.asyncPost("http://172.16.100.253:8080/test", params, new DefaultAsyncClient() {
            @Override
            public void callBack(Responce responce) {
                Log.e("RESULT_", (Looper.getMainLooper().getThread() == Thread.currentThread() ? "主线程" : "子线程") + " = " +
                        responce.getCode() + " = " + responce, responce.getThrowable());
            }
        });
    }

    /**
     * FlowTask测试用例
     */
    public static void task() {
        new FlowTask()
                .ioSchedule()
                .create(() -> {
                    Log.e("Task", "1运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                    return 1;
                })
                .mainHandlerSchedule()
                .map(it -> {
                    Log.e("Task", it + ":2运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                    return "hello";
                })
                .ioSchedule()
                .call(it -> {
                    Log.e("Task", it + ":3运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                });
    }

}
