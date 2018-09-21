package com.loror.lororUtil.image;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.loror.lororUtil.convert.MD5Util;
import com.loror.lororUtil.flyweight.ObjectPool;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class ImageUtil implements Cloneable {
    private static final int tagKey = 3 << 24;
    private static int globalDefaultImage;
    private static int globalErrorImage;
    private static ReadImage globalReadImage;

    private int defaultImage;
    private int errorImage;
    private ImageView imageView;
    private String path;
    private int widthLimit = 200;
    private ReadImage readImage;
    private boolean removeOldTask = true;
    private boolean cachUseAnimation;
    private boolean isGif;
    private ImageUtilCallBack callback;
    private ImageUtilCallBack onLoadListener;
    private String targetDirPath;
    private String targetName;
    private Animation loadAnimation;
    private Context context;
    private static ImageUtil imageUtil;
    private static ExecutorService server;

    private ImageUtil(Context context) {
        this.context = context;
    }

    private final void init(Context context) {
        if (targetDirPath == null) {
            targetDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/"
                    + context.getPackageName() + "/img";
        }
        if (targetName == null) {
            targetName = MD5Util.MD5(path);
        }
    }

    /**
     * 设置全局加载方法
     */
    @Deprecated
    public static void setGlobalReadImage(ReadImage globalReadImage) {
        ImageUtil.globalReadImage = globalReadImage;
    }

    /**
     * 设置全局占位图
     */
    public static void setGlobalDefaultImage(int globalDefaultImage) {
        ImageUtil.globalDefaultImage = globalDefaultImage;
    }

    /**
     * 设置全局错误占位图
     */
    public static void setGlobalErrorImage(int globalErrorImage) {
        ImageUtil.globalErrorImage = globalErrorImage;
    }

    /**
     * 获取实例
     */
    public static ImageUtil with(Context context) {
        if (imageUtil == null) {
            imageUtil = new ImageUtil(null);
        }
        try {
            ImageUtil imageUtil = (ImageUtil) ImageUtil.imageUtil.clone();
            imageUtil.context = context;
            return imageUtil;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new ImageUtil(context);
    }

    public ImageUtil setDefaultImage(int defaultImage) {
        this.defaultImage = defaultImage;
        return this;
    }

    public ImageUtil setErrorImage(int errorImage) {
        this.errorImage = errorImage;
        return this;
    }

    public ImageUtil to(ImageView imageView) {
        this.imageView = imageView;
        if (this.loadAnimation == null) {
            this.loadAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.loadAnimation.setDuration(300);
        }
        return this;
    }

    public ImageUtil from(String path) {
        this.path = path;
        return this;
    }

    public ImageUtil setWidthLimit(int widthLimit) {
        this.widthLimit = widthLimit;
        return this;
    }

    @Deprecated
    public ImageUtil setReadImage(ReadImage readImage) {
        if (readImage != null) {
            this.readImage = readImage;
        }
        return this;
    }

    public ImageUtil setRemoveOldTask(boolean removeOldTask) {
        this.removeOldTask = removeOldTask;
        return this;
    }

    public ImageUtil setCachUseAnimation(boolean cachUseAnimation) {
        this.cachUseAnimation = cachUseAnimation;
        return this;
    }

    public ImageUtil setIsGif(boolean isGif) {
        this.isGif = isGif;
        return this;
    }

    @Deprecated
    public ImageUtil setCallback(ImageUtilCallBack callback) {
        this.callback = callback;
        return this;
    }

    public ImageUtil setOnLoadListener(ImageUtilCallBack onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    public ImageUtil setTargetDir(String targetDir) {
        if (targetDir != null) {
            this.targetDirPath = targetDir;
        }
        return this;
    }

    public ImageUtil setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public ImageUtil setTargetFile(File targetFile) {
        if (targetFile != null) {
            this.targetDirPath = targetFile.getParentFile().getAbsolutePath();
            this.targetName = targetFile.getName();
        }
        return this;
    }

    public String getTargetDirPath() {
        init(this.context);
        return this.targetDirPath;
    }

    public String getTargetFile() {
        init(this.context);
        return (this.targetDirPath.endsWith("/") ? this.targetDirPath : (this.targetDirPath + "/")) + this.targetName;
    }

    public ImageUtil setLoadAnimation(Animation loadAnimation) {
        this.loadAnimation = loadAnimation;
        return this;
    }

    /**
     * 执行
     */
    public void loadImage() {
        final Context context = this.context;
        final int widthLimit = this.widthLimit;
        final int defaultImage = this.defaultImage != 0 ? this.defaultImage : globalDefaultImage;
        final int errorImage = this.errorImage != 0 ? this.errorImage : globalErrorImage;
        final boolean isGif = this.isGif;
        final String path = this.path;
        final boolean removeOldTask = this.removeOldTask;
        final ImageUtilCallBack onLoadListener = this.onLoadListener;
        final Animation loadAnimation = this.loadAnimation;

        ReadImage readImage = this.readImage != null ? this.readImage : globalReadImage;
        if (readImage == null) {
            init(context);
            final String targetDirPath = this.targetDirPath;
            final String targetName = this.targetName;
            readImage = new ReadImage() {

                @Override
                public ReadImageResult readImage(String path, int widthLimit, boolean mutiCache) {
                    String targetFile;
                    if (path.startsWith("http")) {
                        File targetDir = new File(targetDirPath);
                        if (!targetDir.exists()) {
                            targetDir.mkdirs();
                        }
                        String targetDirPath = targetDir.getAbsolutePath();
                        targetFile = (targetDirPath.endsWith("/") ? targetDirPath : (targetDirPath + "/")) + targetName;
                    } else {
                        targetFile = path;
                    }
                    ReadImageResult result = null;
                    LockMap.SingleLock lock = LockMap.getLock(path);//如出现加载同一张图片将获得同一把锁，只有一个任务去加载图片，其他任务只需等待加载完成即可
                    synchronized (lock) {
                        if (lock.mark == 0) {
                            result = SmartReadImage.getInstance(context, targetFile).readImage(path, widthLimit, isGif);
                            if (result.getErrorCode() == 0) {
                                lock.mark = 1;
                            }
                        } else {
                            result = ImageCach.getFromCache(path + widthLimit);//其他任务已加载该图片，从缓存中获取
                        }
                    }
                    return result;
                }
            };
        }

        ImageUtilCallBack callback = this.callback;
        if (callback == null) {
            if (server == null) {
                server = Executors.newFixedThreadPool(3);
            }
            callback = new ImageUtilCallBack() {

                private WeakReference<ImageView> weakReference;
                private String tag;
                private int index;

                @Override
                public void onStart(ImageView imageView) {
                    if (imageView != null) {
                        tag = String.valueOf(imageView.getTag(tagKey));
                        Animation animation = imageView.getAnimation();
                        if (animation != null) {
                            imageView.clearAnimation();
                        }
                        if (defaultImage != 0) {
                            imageView.setImageResource(defaultImage);
                        }
                    }
                    if (onLoadListener != null) {
                        onLoadListener.onStart(imageView);
                    }
                }

                @Override
                public void onLoadCach(final ImageView imageView, final ReadImageResult readImageResult) {
                    if (imageView != null) {
                        if (!isGif) {
                            if (cachUseAnimation && loadAnimation != null) {
                                imageView.startAnimation(loadAnimation);
                            }
                            imageView.setImageBitmap(readImageResult.getBitmap());
                        } else {
                            weakReference = new WeakReference<ImageView>(imageView);
                            final Handler handler = ObjectPool.getInstance().getHandler();
                            if (readImageResult.getCount() > 0) {
                                loadGif(handler, readImageResult);
                            } else {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (loadAnimation != null) {
                                            imageView.startAnimation(loadAnimation);
                                        }
                                        imageView.setImageBitmap(readImageResult.getBitmap());
                                    }
                                });
                            }
                        }
                    }
                    if (onLoadListener != null) {
                        onLoadListener.onLoadCach(imageView, readImageResult);
                    }
                }

                private final void loadGif(final Handler handler, final ReadImageResult readImageResult) {
                    final int size = readImageResult.getCount();
                    final boolean calledAnimation[] = {false};
                    Runnable runnable = new Runnable() {

                        @Override
                        public void run() {
                            ImageView imageView = weakReference.get();
                            if (imageView == null) {
                                return;
                            }
                            boolean useful = tag.equals(imageView.getTag(tagKey));
                            if (useful) {
                                if (readImageResult.isPause()) {
                                    long delay = readImageResult.getFrame(index % size).delay;
                                    handler.postDelayed(this, delay != 0 ? delay : 100);
                                } else {
                                    if (index != 0 && index % size == 0 && !readImageResult.isRepeate()) {
                                        return;
                                    }
                                    imageView.setImageBitmap(readImageResult.getFrame(index % size).image);
                                    if (!calledAnimation[0] && index == 0 && loadAnimation != null) {
                                        calledAnimation[0] = true;
                                        imageView.startAnimation(loadAnimation);
                                    }
                                    long delay = readImageResult.getFrame(index % size).delay;
                                    handler.postDelayed(this, delay != 0 ? delay : 100);
                                    index++;
                                }
                            }
                        }
                    };
                    runnable.run();
                }

                @Override
                public void onFinish(final ImageView imageView, final ReadImageResult readImageResult) {
                    if (imageView != null) {
                        if (!isGif) {
                            if (loadAnimation != null) {
                                imageView.startAnimation(loadAnimation);
                            }
                            imageView.setImageBitmap(readImageResult.getBitmap());
                        } else {
                            weakReference = new WeakReference<ImageView>(imageView);
                            final Handler handler = ObjectPool.getInstance().getHandler();
                            if (readImageResult.getCount() > 0) {
                                loadGif(handler, readImageResult);
                            } else {
                                handler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (loadAnimation != null) {
                                            imageView.startAnimation(loadAnimation);
                                        }
                                        imageView.setImageBitmap(readImageResult.getBitmap());
                                    }
                                });
                            }
                        }
                    }
                    if (onLoadListener != null) {
                        onLoadListener.onFinish(imageView, readImageResult);
                    }
                }

                @Override
                public void onFailed(ImageView imageView, ReadImageResult result) {
                    if (imageView != null && errorImage != 0) {
                        imageView.setImageResource(errorImage);
                    }
                    if (onLoadListener != null) {
                        onLoadListener.onFailed(imageView, result);
                    }
                }
            };
        }

        EfficientImageUtil.loadImage(imageView, path, widthLimit, readImage, callback, null, removeOldTask, isGif);
    }

    public static void releseTag(View view) {
        EfficientImageUtil.releseTag(view);
    }

    /**
     * 获取缓存
     */
    public static Bitmap getBitmapByPath(String path) {
        return EfficientImageUtil.getBitmapByPath(path);
    }

    /**
     * 获取缓存
     */
    public static Bitmap getBitmapByPath(String path, int widthLimit) {
        return EfficientImageUtil.getBitmapByPath(path, widthLimit);
    }

    /**
     * 清除缓存
     */
    public static void clearCachs() {
        EfficientImageUtil.clearCachs();
    }
}
