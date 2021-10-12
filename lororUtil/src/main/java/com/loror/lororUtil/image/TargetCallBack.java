package com.loror.lororUtil.image;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.loror.lororUtil.flyweight.ObjectPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TargetCallBack implements ImageUtilCallBack {

    private ExecutorService server;
    private final Handler handler = ObjectPool.getInstance().getHandler();

    private final Context context;
    private final Target target;
    private final ImageView imageView;
    private final ImageUtilCallBack onLoadListener;
    private final int defaultImage, errorImage;
    private final int screenWidth;

    TargetCallBack(Context context, Target target, ImageView imageView, ImageUtilCallBack onLoadListener, int defaultImage, int errorImage) {
        this.context = context;
        this.target = target;
        this.imageView = imageView;
        this.onLoadListener = onLoadListener;
        this.defaultImage = defaultImage;
        this.errorImage = errorImage;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.screenWidth = displayMetrics.widthPixels;
    }

    void load(final ReadImage readImage, final String path, int widthLimit, final boolean gif) {
        if (server == null) {
            synchronized (TargetCallBack.class) {
                if (server == null) {
                    server = Executors.newFixedThreadPool(3);
                }
            }
        }

        if (widthLimit == 0) {
            widthLimit = screenWidth;
        }
        final String cachKey = path + widthLimit;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onStart(imageView);
            }
        });
        ReadImageResult result = ImageCache.getFromCache(cachKey);
        if (result != null) {
            onLoadCach(imageView, result);
            return;
        }
        final int finalWidthLimit = widthLimit;
        server.execute(new Runnable() {
            @Override
            public void run() {
                final ReadImageResult result = readImage.readImage(path, finalWidthLimit, gif);
                result.setOriginPath(path);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result.getBitmap() == null) {
                            onFailed(imageView, result);
                        } else {
                            if (result.getErrorCode() == 0) {
                                ImageCache.pushToCache(cachKey, result);
                            }
                            onFinish(imageView, result);
                        }
                    }
                });
            }
        });
    }

    public void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    @Override
    public void onStart(ImageView imageView) {
        if (onLoadListener != null) {
            onLoadListener.onStart(imageView);
        }
        if (target instanceof BitmapTarget) {
            if (defaultImage != 0) {
                ((BitmapTarget) target).target(BitmapUtil.drawableToBitmap(context.getResources().getDrawable(defaultImage)));
            }
        }
    }

    void result(ReadImageResult result) {
        if (target instanceof BitmapTarget) {
            ((BitmapTarget) target).target(result.getBitmap());
        } else if (target instanceof ResultTarget) {
            ((ResultTarget) target).target(result);
        } else if (target instanceof PathTarget) {
            ((PathTarget) target).target(result.getPath());
        }
    }

    @Override
    public void onLoadCach(ImageView imageView, ReadImageResult result) {
        if (onLoadListener != null) {
            onLoadListener.onLoadCach(imageView, result);
        }
        result(result);
    }

    @Override
    public void onFinish(ImageView imageView, ReadImageResult result) {
        if (onLoadListener != null) {
            onLoadListener.onFinish(imageView, result);
        }
        result(result);
    }

    @Override
    public void onFailed(ImageView imageView, ReadImageResult result) {
        if (onLoadListener != null) {
            onLoadListener.onFailed(imageView, result);
        }
        if (target instanceof BitmapTarget) {
            if (errorImage != 0) {
                ((BitmapTarget) target).target(BitmapUtil.drawableToBitmap(context.getResources().getDrawable(errorImage)));
            }
        } else if (target instanceof ResultTarget) {
            ((ResultTarget) target).target(result);
        }
    }
}
