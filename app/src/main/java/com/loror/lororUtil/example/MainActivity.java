package com.loror.lororUtil.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororUtil.sql.SQLiteUtil;
import com.loror.lororUtil.view.Find;
import com.loror.lororUtil.view.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Find
    GridView list;

    private List<Image> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
    }

    private void initData() {
        SQLiteUtil util = new SQLiteUtil(this, "images", 1);
        util.createTableIfNotExists(Image.class);
        if (util.count(Image.class) > 0) {
            images.addAll(util.getAll(Image.class));
        } else {
            String[] imgs = {"https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525921287&di=df51a86f3e7acc579f4fbd47c67f64cc&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F0117e2571b8b246ac72538120dd8a4.jpg%401280w_1l_2o_100sh.jpg",
                    "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525937402&di=9bf7c32cca7ab124c762805933c1acb7&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F01690955496f930000019ae92f3a4e.jpg%402o.jpg",
                    "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537525951939&di=b2b701ef7948dd58ba8c3f8821734cc5&imgtype=0&src=http%3A%2F%2Fimg12.3lian.com%2Fgaoqing02%2F01%2F58%2F85.jpg"};
            for (int i = 0; i < 20; i++) {
                Image image = new Image();
                image.path = imgs[i % imgs.length];
                util.insert(image);
            }
        }
        Log.e("TAG_", images.size() + " == count");
        util.close();
    }

    private void initView() {
        ViewUtil.find(this);
        SimpleAdapter adapter = new SimpleAdapter(this, images);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e("TAG_", ObjectPool.getInstance().getTheadPool().getAliveThread() + " == alive");
            }
        });
    }
}
