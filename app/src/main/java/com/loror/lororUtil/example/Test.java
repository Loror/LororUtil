package com.loror.lororUtil.example;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.loror.lororUtil.asynctask.AsyncUtil;
import com.loror.lororUtil.asynctask.FlowTask;
import com.loror.lororUtil.example.bean.Image;
import com.loror.lororUtil.example.net.ApiCreator;
import com.loror.lororUtil.example.net.ApiTest;
import com.loror.lororUtil.http.Client;
import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororUtil.http.api.Observer;
import com.loror.lororUtil.sql.SQLiteUtil;
import com.loror.lororUtil.sql.Where;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * sql测试用例
     */
    public static void testSql(Context context, List<Image> imageList) {
        imageList.clear();
        SQLiteUtil util = new SQLiteUtil(context, "images", 1);
        if (util.model(Image.class).count() == 0) {
            String[] imgs = {"https://iconfont.alicdn.com/t/cf6a71ea-63a7-40b1-87bc-2ee3e8de093f.png",
                    "https://iconfont.alicdn.com/t/79f02d6e-8e40-4c7f-bbc3-fa4c91be459c.png",
                    "https://iconfont.alicdn.com/t/ea972552-5433-4fa4-a8d6-f97268a016fd.png"};
            List<Image> images = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Image image = new Image();
                image.path = imgs[i % imgs.length];
                image.flag = true;
                images.add(image);
            }
            util.model(Image.class, false).save(images);
        }

        Log.e("TAG_WHERE", JSON.toJSONString(util.model(Image.class)
                .whereIn("id", new Integer[]{1, 2, 3})
                .where(new Where.OnWhere() {
                    @Override
                    public void where(Where where) {
                        where.where("id", 1)
                                .whereOr("id", 3);
                    }
                })
                .get()));
        Log.e("TAG_WHERE", "======================");

        imageList.addAll(util.model(Image.class).get());
        Log.e("TAG_WHERE", "model:" + imageList);

        List<Image> list = util.nativeQuery().executeQuery("select * from Image").list(Image.class);
        Log.e("TAG_WHERE", "native:" + list);

        Log.e("TAG_WHERE", "result:" + util.nativeQuery().executeUpdateDeleteStatement("update Image set flag = 0"));

        util.close();
    }
}
