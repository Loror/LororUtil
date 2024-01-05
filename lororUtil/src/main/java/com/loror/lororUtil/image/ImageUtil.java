package com.loror.lororUtil.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.loror.lororUtil.convert.MD5Util;

import java.io.File;
import java.util.Locale;

public class ImageUtil implements Cloneable {

    private static int globalDefaultImage;
    private static int globalErrorImage;
    private static ReadImage globalReadImage;
    private static BitmapConverter globalBitmapConverter;

    private int defaultImage;
    private int errorImage;
    private ImageView imageView;
    private Target target;
    private String path;
    private int widthLimit;
    private boolean noSdCache;
    private ReadImage readImage;
    private BitmapConverter bitmapConverter;
    private boolean removeOldTask = true;
    private boolean cachUseAnimation;
    private boolean isGif;
    private boolean autoRotateIsDegree;
    private ImageUtilCallBack callback;
    private ImageUtilCallBack onLoadListener;
    private String targetDirPath;
    private String targetName;
    private Animation loadAnimation;
    private Context context;

    private static class SingletonFactory {
        private static final ImageUtil imageUtil = new ImageUtil(null);
    }

    private ImageUtil(Context context) {
        this.context = context;
    }

    private final void init(Context context) {
        if (targetDirPath == null) {
            try {
                targetDirPath = context.getExternalCacheDir().getAbsolutePath();
            } catch (Exception e) {
                targetDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/"
                        + context.getPackageName() + "/cache";
            }
        }
        if (targetName == null) {
//            if (path != null) {
//                String finalPath = path;
//                int index = path.lastIndexOf("/");
//                if (index != -1) {
//                    finalPath = path.substring(index + 1);
//                }
//                String regEx = "[/\\\\：:*\"<>|?？]";
//                Pattern pattern = Pattern.compile(regEx);
//                Matcher matcher = pattern.matcher(finalPath);
//                targetName = matcher.replaceAll("");
//                return;
//            }
            targetName = MD5Util.MD5(path);
            if (path != null) {
                String fix = path.toLowerCase(Locale.CHINA);
                int index = fix.lastIndexOf("?");
                if (index != -1) {
                    fix = fix.substring(0, index);
                }
                if (fix.endsWith(".mp4")) {
                    targetName += ".mp4";
                } else if (fix.endsWith(".gif")) {
                    targetName += ".gif";
                }
            }
        }
    }

    /**
     * 设置全局bitmap预转换接口
     */
    @Deprecated
    public static void setGlobalBitmapConverter(BitmapConverter globalBitmapConverter) {
        ImageUtil.globalBitmapConverter = globalBitmapConverter;
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
        try {
            ImageUtil imageUtil = (ImageUtil) SingletonFactory.imageUtil.clone();
            imageUtil.context = context;
            return imageUtil;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return new ImageUtil(context);
    }

    /**
     * 设置加载时占位图
     */
    public ImageUtil setDefaultImage(int defaultImage) {
        this.defaultImage = defaultImage;
        return this;
    }

    /**
     * 设置加载错误时占位图
     */
    public ImageUtil setErrorImage(int errorImage) {
        this.errorImage = errorImage;
        return this;
    }

    /**
     * 设置加载图片时限定宽度
     */
    public ImageUtil setWidthLimit(int widthLimit) {
        this.widthLimit = widthLimit;
        return this;
    }

    /**
     * 设置是否缓存网路图片到本地
     */
    public ImageUtil setNoSdCache(boolean noSdCache) {
        this.noSdCache = noSdCache;
        return this;
    }

    @Deprecated
    public ImageUtil setReadImage(ReadImage readImage) {
        if (readImage != null) {
            this.readImage = readImage;
        }
        return this;
    }

    /**
     * 设置加载成功后图片预处理
     */
    public ImageUtil setBitmapConverter(BitmapConverter bitmapConverter) {
        this.bitmapConverter = bitmapConverter;
        return this;
    }

    /**
     * 设置是否移除快速滑动时离开界面的任务，默认移除
     */
    public ImageUtil setRemoveOldTask(boolean removeOldTask) {
        this.removeOldTask = removeOldTask;
        return this;
    }

    /**
     * 设置是否为已缓存图片启用加载动画，默认不启用
     */
    public ImageUtil setCacheUseAnimation(boolean cachUseAnimation) {
        this.cachUseAnimation = cachUseAnimation;
        return this;
    }

    /**
     * 设置是否以gif方式加载图片
     */
    public ImageUtil setIsGif(boolean isGif) {
        this.isGif = isGif;
        return this;
    }

    /**
     * 设置是否自动旋转如果文件包含角度
     */
    public ImageUtil setAutoRotateIsDegree(boolean autoRotateIsDegree) {
        this.autoRotateIsDegree = autoRotateIsDegree;
        return this;
    }

    @Deprecated
    public ImageUtil setCallback(ImageUtilCallBack callback) {
        this.callback = callback;
        return this;
    }

    /**
     * 设置加载过程监听
     */
    public ImageUtil setOnLoadListener(ImageUtilCallBack onLoadListener) {
        this.onLoadListener = onLoadListener;
        return this;
    }

    /**
     * 设置缓存目录
     */
    public ImageUtil setTargetDir(String targetDir) {
        if (targetDir != null) {
            this.targetDirPath = targetDir;
        }
        return this;
    }

    /**
     * 设置缓存文件名
     */
    public ImageUtil setTargetName(String targetName) {
        this.targetName = targetName;
        return this;
    }

    /**
     * 设置缓存地址
     */
    public ImageUtil setTargetFile(File targetFile) {
        if (targetFile != null) {
            this.targetDirPath = targetFile.getParentFile().getAbsolutePath();
            this.targetName = targetFile.getName();
        }
        return this;
    }

    /**
     * 获取缓存目录
     */
    public String getTargetDirPath() {
        init(this.context);
        return this.targetDirPath;
    }

    /**
     * 获取缓存地址
     */
    public String getTargetFile() {
        init(this.context);
        return (this.targetDirPath.endsWith("/") ? this.targetDirPath : (this.targetDirPath + "/")) + this.targetName;
    }

    /**
     * 设置加载动画
     */
    public ImageUtil setLoadAnimation(Animation loadAnimation) {
        this.loadAnimation = loadAnimation;
        return this;
    }

    /**
     * 设置源
     */
    public ImageUtil from(String path) {
        this.path = path;
        return this;
    }

    /**
     * 设置加载目标imageView
     */
    public ImageUtil to(ImageView imageView) {
        this.imageView = imageView;
        if (this.loadAnimation == null) {
            this.loadAnimation = new AlphaAnimation(0.0f, 1.0f);
            this.loadAnimation.setDuration(300);
        }
        return this;
    }

    /**
     * 设置加载目标target
     */
    public ImageUtil to(BitmapTarget target) {
        this.target = target;
        return this;
    }

    /**
     * 设置加载目标target
     */
    public ImageUtil to(ResultTarget target) {
        this.target = target;
        return this;
    }

    /**
     * 设置加载目标target
     */
    public ImageUtil to(PathTarget target) {
        this.target = target;
        return this;
    }

    /**
     * 设置加载目标imageView并开始加载
     */
    public void loadTo(ImageView imageView) {
        to(imageView).loadImage();
    }

    /**
     * 设置加载目标target并开始加载
     */
    public void loadTo(BitmapTarget target) {
        to(target).loadImage();
    }

    /**
     * 设置加载目标target并开始加载
     */
    public void loadTo(ResultTarget target) {
        to(target).loadImage();
    }

    /**
     * 设置加载目标target并开始加载
     */
    public void loadTo(PathTarget target) {
        to(target).loadImage();
    }

    /**
     * 获取控件宽度
     */
    private int getViewWidth(ImageView view) {
        if (view != null) {
            int width = view.getWidth();
            if (width == 0) {
                width = view.getMeasuredWidth();
            }
            if (width == 0) {
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params != null) {
                    if (params.width > 0) {
                        width = params.width;
                    } else {
                        view.measure(params.width, params.height);
                        width = view.getMeasuredWidth();
                    }
                }
            }
            return width;
        }
        return 0;
    }

    /**
     * 开始加载
     */
    public void loadImage() {
        final Context context = this.context;
        final int widthLimit = this.widthLimit > 0 ? this.widthLimit : getViewWidth(this.imageView);
        final boolean isGif = this.isGif;

        ReadImage readImage = new ReadImage() {

            @Override
            public ReadImageResult readImage(String path, int widthLimit, boolean mutiCache) {
                ReadImage readImage = ImageUtil.this.readImage != null ? ImageUtil.this.readImage : ImageUtil.globalReadImage;
                ReadImageResult result = null;
                if (readImage == null) {
                    if (noSdCache) {
                        if (path.startsWith("http")) {
                            readImage = ReadHttpImage.getInstance();
                        } else {
                            readImage = ReadSDCardImage.getInstance();
                        }
                    } else {
                        init(context);
                        String targetDirPath = ImageUtil.this.targetDirPath;
                        String targetName = ImageUtil.this.targetName;
                        String targetFile;
                        if (path.startsWith("http")) {
                            File targetDir = new File(targetDirPath);
                            if (!targetDir.exists()) {
                                targetDir.mkdirs();
                            }
                            targetDirPath = targetDir.getAbsolutePath();
                            targetFile = (targetDirPath.endsWith("/") ? targetDirPath : (targetDirPath + "/")) + targetName;
                        } else {
                            targetFile = path;
                        }
                        readImage = SmartReadImage.getInstance(context, targetFile, autoRotateIsDegree);
                    }
                }
                LockMap.SingleLock lock = LockMap.getLock(path);//如出现加载同一张图片将获得同一把锁，只有一个任务去加载图片，其他任务只需等待加载完成即可
                synchronized (lock) {
                    if (lock.mark == 0) {
                        result = readImage.readImage(path, widthLimit, isGif);
                        if (result == null) {
                            throw new IllegalStateException("自定义ReadImage不允许返回null，请返回ReadImageResult并指定errorCode");
                        }
                        if (result.getErrorCode() == 0) {
                            lock.mark = 1;//加载成功，锁耗尽
                            ImageCache.pushToCache(path + widthLimit, result);//放入缓存
                        }
                    } else {
                        result = ImageCache.getFromCache(path + widthLimit);//其他任务已加载该图片，从缓存中获取
                        //可能其他任务获取的宽度与本次不同，尝试重新加载
                        if (result == null) {
                            result = readImage.readImage(path, widthLimit, isGif);
                            if (result == null) {
                                result = new ReadImageResult();
                                result.setErrorCode(4);//超时无法获取，标记错误码4
                            } else if (result.getErrorCode() == 0) {
                                ImageCache.pushToCache(path + widthLimit, result);//放入缓存
                            }
                        }
                    }
                }
                return result;
            }
        };

        ImageUtilCallBack callback = this.callback;
        if (callback == null) {
            if (target != null) {
                TargetCallBack targetCallBack = new TargetCallBack(context, target, imageView, bitmapConverter, onLoadListener, defaultImage, errorImage);
                targetCallBack.load(readImage, path, widthLimit, isGif);
                return;
            } else {
                int defaultImage = this.defaultImage != 0 ? this.defaultImage : globalDefaultImage;
                int errorImage = this.errorImage != 0 ? this.errorImage : globalErrorImage;
                BitmapConverter bitmapConverter = this.bitmapConverter != null ? this.bitmapConverter : globalBitmapConverter;
                callback = new GifAbleImageViewCallBack(context, imageView, onLoadListener, bitmapConverter, isGif,
                        defaultImage, errorImage, cachUseAnimation, loadAnimation);
            }
        }

        EfficientImageUtil.loadImage(imageView, path, widthLimit, readImage, callback, null, removeOldTask, isGif);
    }

    @Deprecated
    public static void releseTag(View view) {
        releaseTag(view);
    }

    /**
     * 清除tag
     */
    public static void releaseTag(View view) {
        EfficientImageUtil.releaseTag(view);
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
    public static void clearCaches() {
        EfficientImageUtil.clearCaches();
    }

    /**
     * 清除缓存
     */
    public static void clearCachesWithDisk(Context context) {
        EfficientImageUtil.clearCaches();
        ImageDownloader.tryClearAllSqlCache(context);
    }
}
