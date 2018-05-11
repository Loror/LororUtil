package com.loror.lororUtil.image;

import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Loror on 2018/1/23.
 */

public abstract class Cache<T> {
    /**
     * 建立线程安全,支持高并发的容器
     */
    private ConcurrentHashMap<String, SoftReference<T>> softCach = new ConcurrentHashMap<String, SoftReference<T>>();
    /**
     * 图片缓存
     */
    private LruCache<String, T> cache = new LruCache<String, T>((int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
        protected int sizeOf(String key, T value) {
            return Cache.this.sizeOf(value);
        }

        @Override
        protected void entryRemoved(boolean evicted, String key, T oldValue, T newValue) {
            moveToWeak(key, oldValue);
            if (oldValue != null) {
                // 当硬引用缓存容量已满时，会使用LRU算法将最近没有被使用的图片转入软引用缓存
                softCach.put(key, new SoftReference<T>(oldValue));
            }
        }
    };

    protected abstract int sizeOf(T t);

    protected void moveToWeak(String key, T value) {

    }

    /**
     * 获取缓存
     */
    public T getFromCache(String key) {
        T bitmap = null;
        if (softCach.get(key) != null) {
            bitmap = softCach.get(key).get();
        }
        return bitmap == null ? cache.get(key) : bitmap;

    }

    /**
     * 加入缓存
     */
    public void pushToCach(String key, T value) {
        if (key != null && value != null) {
            cache.put(key, value);
        }
    }

    /**
     * 清空缓存
     */
    public void clearCach() {
        cache.evictAll();
        softCach.clear();
    }
}
