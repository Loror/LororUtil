package com.loror.lororUtil.example;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loror.lororUtil.asynctask.AsyncUtil;
import com.loror.lororUtil.asynctask.FlowTask;
import com.loror.lororUtil.asynctask.Func;
import com.loror.lororUtil.asynctask.Func0;
import com.loror.lororUtil.asynctask.Func1;
import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
import com.loror.lororUtil.http.api.Observer;
import com.loror.lororUtil.image.ImageUtil;
import com.loror.lororUtil.image.PathTarget;
import com.loror.lororUtil.sql.SQLiteUtil;
import com.loror.lororUtil.sql.Where;
import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororUtil.view.ItemLongClick;
import com.loror.lororUtil.view.LongClick;
import com.loror.lororUtil.view.ViewUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    /**
     * {@link R.id#list}
     */
    @Find
    GridView list;

    private final List<Image> images = new ArrayList<>();
    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        requestPermission("android.permission.WRITE_EXTERNAL_STORAGE", 0);
//        connectNet();
        connectNetByApi();
        task();

        ImageUtil.with(this)
                .from("https://iconfont.alicdn.com/t/083f67b8-b930-4a31-8f42-060ce61942f0.png")
                .loadTo(new PathTarget() {
                    @Override
                    public void target(String result) {
                        Log.e("ImageUtil_Target", "下载完成：" + result);
                    }
                });
    }

    private void task() {
        new FlowTask()
                .ioSchedule()
                .create(new Func0<Integer>() {
                    @Override
                    public Integer func() {
                        Log.e("Task", "1运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                        return 1;
                    }
                })
                .mainHandlerSchedule()
                .map(new Func<Integer, String>() {
                    @Override
                    public String func(Integer it) {
                        Log.e("Task", it + ":2运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                        return "hello";
                    }
                })
                .ioSchedule()
                .call(new Func1<String>() {
                    @Override
                    public void func(String it) {
                        Log.e("Task", it + ":3运行线程：" + (Looper.getMainLooper() == Looper.myLooper() ? "主线程" : "子线程"));
                    }
                });
    }

    private void connectNetByApi() {
        final ApiTest apiTest = ApiCreator.getApiClient().create(ApiTest.class);
        //内置异步请求方式
        apiTest.test().subscribe(new Observer<String>() {
            @Override
            public void success(String data) {
                Log.e("RESULT_1", data);
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
                Log.e("RESULT_2", result);
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
                        Log.e("RESULT_3", s);
                    }
                });
    }

    private void connectNet() {
        HttpClient client = new HttpClient();
        RequestParams params = new RequestParams();
        params.addParams("name", "xiaoming");
        params.addParams("age", 10);
        params.addParams("young", true);
        params.setAsJson(true);
        client.asyncPost("http://192.168.1.142:8888/test", params, new DefaultAsyncClient() {
            @Override
            public void callBack(Responce responce) {
                Log.e("RESULT_", (Looper.getMainLooper().getThread() == Thread.currentThread() ? "主线程" : "子线程") + " = " +
                        responce.getCode() + " = " + responce, responce.getThrowable());
            }
        });
    }

    private void initData() {
        images.clear();
        SQLiteUtil util = new SQLiteUtil(this, "images", 1);
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

        images.addAll(util.model(Image.class).get());
        Log.e("TAG_WHERE", "model:" + images);

        List<Image> list = util.nativeQuery().executeQuery("select * from Image").list(Image.class);
        Log.e("TAG_WHERE", "native:" + list);

        Log.e("TAG_WHERE", "result:" + util.nativeQuery().executeUpdateStatement("update Image set flag = 0"));

        util.close();
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        ViewUtil.find(this);
        ViewUtil.click(this);
        adapter = new SimpleAdapter(this, images);
        list.setAdapter(adapter);
    }

    @ItemClick(id = R.id.list)
    public void itemClick(View v, int position) {
        Toast.makeText(this, "itemClick:position(" + position + ")点击", Toast.LENGTH_SHORT).show();
    }

    @ItemLongClick(id = R.id.list)
    public void itemLongClick(View v, int position) {
        Toast.makeText(this, "itemLongClick:position(" + position + ")点击", Toast.LENGTH_SHORT).show();
    }

    @Click(id = R.id.button)
    public void click(View v) {
        Toast.makeText(this, "click:短按点击", Toast.LENGTH_SHORT).show();
    }

    @LongClick(id = R.id.button)
    public void longClick(View v) {
        Toast.makeText(this, "longClick:长按点击", Toast.LENGTH_SHORT).show();
    }

    /**
     * 动态申请权限
     */
    public void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // 权限申请曾经被用户拒绝
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                Toast.makeText(this, "获取权限失败", Toast.LENGTH_SHORT).show();
            } else {
                // 进行权限请求
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            initData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "获取权限成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "获取权限被拒绝", Toast.LENGTH_SHORT).show();
        }
        initData();
    }
}
