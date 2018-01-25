package com.loror.lororUtil.image;

import java.util.Hashtable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.loror.lororUtil.asynctask.RemoveableThreadPool;
import com.loror.lororUtil.flyweight.ObjectPool;
import com.loror.lororUtil.text.TextUtil;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

public class EfficientImageUtil {
    protected static Lock lock = new ReentrantLock();
    private static final int tagKey = 3 << 24;
    private static final int cachTagKey = 3 << 24 + 1;
    private static int tag = 1;
    private static Handler handler = ObjectPool.getInstance().getHandler();
    private static RemoveableThreadPool removeableThreadPool = ObjectPool.getInstance().getTheadPool();
    private static Hashtable<String, Runnable> tasks = new Hashtable<>();

    private EfficientImageUtil() {
    }

    /**
     * 更换线程池
     */
    public static void setRemoveableThreadPool(RemoveableThreadPool removeableThreadPool) {
        if (removeableThreadPool != null) {
            EfficientImageUtil.removeableThreadPool = removeableThreadPool;
        }
    }

    /**
     * 清除tag
     */
    public static void releseTag(View view) {
        view.setTag(tagKey, "");
    }

    /**
     * 获取缓存
     */
    public static Bitmap getBitmapByPath(String path) {
        return ImageCach.getFromCache(path + 200).getBitmap();
    }

    /**
     * 获取缓存
     */
    public static Bitmap getBitmapByPath(String path, int widthLimit) {
        return ImageCach.getFromCache(path + widthLimit).getBitmap();
    }

    /**
     * 清除缓存
     */
    public static void clearCachs() {
        ImageCach.clearCach();
        tasks.clear();
    }

    /**
     * 重载
     */
    public static void loadImage(ImageView imageView, String path, ReadImage readImage) {
        loadImage(imageView, path, 200, readImage, null, null, true);
    }

    /**
     * 重载
     */
    public static void loadImage(ImageView imageView, String path, int widthLimit, ReadImage readImage) {
        loadImage(imageView, path, widthLimit, readImage, null, null, true);
    }

    /**
     * 重载
     */
    public static void loadImage(ImageView imageView, String path, ReadImage readImage, ImageUtilCallBack callback) {
        loadImage(imageView, path, 200, readImage, callback, null, true);
    }

    /**
     * 重载
     */
    public static void loadImage(ImageView imageView, String path, int widthLimit, ReadImage readImage,
                                 ImageUtilCallBack callback) {
        loadImage(imageView, path, widthLimit, readImage, callback, null, true);
    }

    /**
     * 重载
     */
    public static void loadImage(ImageView imageView, String path, int widthLimit, ReadImage readImage,
                                 ImageUtilCallBack callback, Bitmap defaultImage) {
        loadImage(imageView, path, widthLimit, readImage, callback, defaultImage, true);
    }

    /**
     * 加载，参数，1，imageView，2，地址，3，宽度限制，4，读取图片接口，5，回掉，6，是否移除滑出的任务
     */
    public static void loadImage(final ImageView imageView, final String path, final int widthLimit,
                                 final ReadImage readImage, final ImageUtilCallBack callback, Bitmap defaultImage,
                                 final boolean removeOldTask) {
        if (!TextUtil.isEmpty(path)) {
            final String tag = String.valueOf(EfficientImageUtil.tag++);
            final String cachKey = path + widthLimit;
            final boolean hasImageView = imageView != null;
            if (EfficientImageUtil.tag > tagKey) {
                EfficientImageUtil.tag = 0;
            }
            Object old = hasImageView ? imageView.getTag(tagKey) : null;
            Object oldCach = hasImageView ? imageView.getTag(cachTagKey) : null;
            if (!cachKey.equals(oldCach)) {
                if (defaultImage == null) {
                    defaultImage = ObjectPool.getInstance().getDefaultImage();
                }

                lock.lock();
                if (hasImageView) {
                    imageView.setTag(tagKey, tag);
                    imageView.setTag(cachTagKey, cachKey);
                }
                lock.unlock();
                if (old != null && removeOldTask) {
                    Runnable runnable = tasks.remove(old);
                    if (runnable != null) {
                        removeableThreadPool.removeTask(runnable);
                    }
                }

                if (hasImageView) {
                    imageView.setImageBitmap(defaultImage);
                }
                if (callback != null) {
                    callback.onStart(imageView);
                }

                ReadImageResult result = ImageCach.getFromCache(cachKey);
                if (result != null) {
                    lock.lock();
                    boolean useful = !hasImageView || tag.equals(imageView.getTag(tagKey));
                    lock.unlock();
                    if (useful) {
                        if (callback != null) {
                            callback.onLoadCach(imageView, result);
                        } else {
                            if (hasImageView) {
                                imageView.setImageBitmap(result.getBitmap());
                            }
                        }
                    }

                } else {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            if (removeOldTask) {
                                EfficientImageUtil.tasks.remove(tag);
                            }
                            final ReadImageResult readImageResult = readImage.readImage(path, widthLimit);
                            readImageResult.setOriginPath(path);
                            if (readImageResult.getBitmap() == null) {
                                EfficientImageUtil.lock.lock();
                                boolean useful = !hasImageView
                                        || tag.equals(imageView.getTag(EfficientImageUtil.tagKey));
                                EfficientImageUtil.lock.unlock();
                                if (useful && callback != null) {
                                    EfficientImageUtil.handler.post(new Runnable() {
                                        public void run() {
                                            callback.onFailed(imageView, readImageResult);
                                        }
                                    });
                                }
                            } else {
                                if (readImageResult.getErrorCode() == 0) {
                                    ImageCach.pushToCach(cachKey, readImageResult);
                                }
                                handler.post(new Runnable() {
                                    public void run() {
                                        EfficientImageUtil.lock.lock();
                                        boolean useful = !hasImageView
                                                || tag.equals(imageView.getTag(EfficientImageUtil.tagKey));
                                        EfficientImageUtil.lock.unlock();
                                        if (useful) {
                                            if (callback != null) {
                                                callback.onFinish(imageView, readImageResult);
                                            } else {
                                                if (hasImageView) {
                                                    imageView.setImageBitmap(readImageResult.getBitmap());
                                                }
                                            }
                                        }

                                    }
                                });
                            }
                        }
                    };
                    if (removeOldTask) {
                        tasks.put(tag, runnable);
                    }
                    removeableThreadPool.excute(runnable, RemoveableThreadPool.EXCUTETYPE_ORDER);
                }
            }
        }
    }
}
