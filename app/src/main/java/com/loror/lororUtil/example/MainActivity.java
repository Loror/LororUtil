package com.loror.lororUtil.example;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.loror.lororUtil.example.bean.Image;
import com.loror.lororUtil.http.HttpsClient;
import com.loror.lororUtil.view.Click;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ItemClick;
import com.loror.lororUtil.view.ItemLongClick;
import com.loror.lororUtil.view.LongClick;
import com.loror.lororUtil.view.ViewUtil;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

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
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);
//        Test.connectNet();
//        Test.connectNetByApi();
//        Test.task();

//        ImageUtil.with(this)
//                .from("https://iconfont.alicdn.com/t/083f67b8-b930-4a31-8f42-060ce61942f0.png")
//                .loadTo((PathTarget) result -> Log.e("ImageUtil_Target", "下载完成：" + result));
        HttpsClient.setOnHttpsConfig(new HttpsClient.OnHttpsConfig() {
            @Override
            public void onHttpsConfig(Object connection) {
                if (connection instanceof HttpURLConnection) {
                    Log.e("HttpsClient", "onHttpsConfig:" + ((HttpURLConnection) connection).getURL() + "(" + connection.hashCode() + ")");
                } else {
                    Log.e("HttpsClient", "onHttpsConfig:" + connection);
                }
            }

            @Override
            public void onRequestFinish(Object connection) {
                if (connection instanceof HttpURLConnection) {
                    Log.e("HttpsClient", "onRequestFinish:" + ((HttpURLConnection) connection).getURL() + "(" + connection.hashCode() + ")");
                } else {
                    Log.e("HttpsClient", "onRequestFinish:" + connection);
                }
            }
        });
    }

    private void initData() {
        Test.testSql(this, images);
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
        Test.connectNetByApi();
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
