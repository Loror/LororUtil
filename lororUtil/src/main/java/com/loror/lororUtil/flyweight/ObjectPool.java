package com.loror.lororUtil.flyweight;

import com.loror.lororUtil.asynctask.RemoveableThreadPool;
import com.loror.lororUtil.asynctask.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;

public class ObjectPool {

    //禁止实例化
    private ObjectPool() {

    }

    //单例工厂
    private static class SingleFactory {
        private static ObjectPool instance = new ObjectPool();
    }

    public static ObjectPool getInstance() {
        return SingleFactory.instance;
    }

    private Handler handler;
    private Bitmap defaultImage;
    private ThreadPool threadPool;

    public synchronized RemoveableThreadPool getThreadPool() {
        if (threadPool == null) {
            threadPool = new ThreadPool(7);
        }
        return threadPool;
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
    public void release() {
        if (threadPool != null) {
            try {
                threadPool.release();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        threadPool = null;
        defaultImage = null;
    }
}
