package com.loror.lororUtil.flyweight;

import com.loror.lororUtil.asynctask.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;

public class ObjectPool {
    private static ObjectPool instance;

    public interface SDCardCallBack {
        void scanOne(int find, int scaned);
    }

    private ObjectPool() {
        // TODO Auto-generated constructor stub
    }

    public static ObjectPool getInstance() {
        if (instance == null)
            instance = new ObjectPool();
        return instance;
    }

    private Handler handler;
    private Bitmap defaultImage;
    private ThreadPool theadPool;

    public synchronized ThreadPool getTheadPool() {
        if (theadPool == null) {
            theadPool = new ThreadPool(7);
        }
        return theadPool;
    }

    public synchronized Bitmap getDefaultImage() {
        if (defaultImage == null) {
            ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
            Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            colorDrawable.draw(canvas);
            defaultImage = bitmap;
        }
        return defaultImage;
    }

    public synchronized Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    /**
     * 释放资源
     */
    public void relese() {
        if (theadPool != null)
            try {
                theadPool.finalize();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        theadPool = null;
    }
}
