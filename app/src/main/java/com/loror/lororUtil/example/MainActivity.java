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
import com.loror.lororUtil.http.DefaultAsyncClient;
import com.loror.lororUtil.http.HttpClient;
import com.loror.lororUtil.http.RequestParams;
import com.loror.lororUtil.http.Responce;
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

public class MainActivity extends AppCompatActivity {

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
            String[] imgs = {"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525921287&di=df51a86f3e7acc579f4fbd47c67f64cc&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F0117e2571b8b246ac72538120dd8a4.jpg%401280w_1l_2o_100sh.jpg",
                    "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525937402&di=9bf7c32cca7ab124c762805933c1acb7&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F01690955496f930000019ae92f3a4e.jpg%402o.jpg",
                    "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525951939&di=b2b701ef7948dd58ba8c3f8821734cc5&imgtype=0&src=http%3A%2F%2Fimg12.3lian.com%2Fgaoqing02%2F01%2F58%2F85.jpg"};
            List<Image> images = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                Image image = new Image();
                image.path = imgs[i % imgs.length];
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
