package com.loror.lororUtil.image;

import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.util.LruCache;

public class ImageCach {
	/**
	 * 建立线程安全,支持高并发的容器
	 */
	private static ConcurrentHashMap<String, SoftReference<Bitmap>> softCach = new ConcurrentHashMap<String, SoftReference<Bitmap>>();
	/**
	 * 图片缓存
	 */
	private static LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>(
			(int) (Runtime.getRuntime().maxMemory() / 1024) / 8) {
		protected int sizeOf(String key, Bitmap value) {
			return value == null ? 0 : value.getByteCount() / 1024;
		};

		@Override
		protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
			if (oldValue != null) {
				// 当硬引用缓存容量已满时，会使用LRU算法将最近没有被使用的图片转入软引用缓存
				softCach.put(key, new SoftReference<Bitmap>(oldValue));
			}
		}
	};

	/**
	 * 获取缓存
	 */
	public static Bitmap getFromCache(String key) {
		Bitmap bitmap = null;
		if (softCach.get(key) != null) {
			bitmap = softCach.get(key).get();
		}
		return bitmap == null ? cache.get(key) : bitmap;

	}

	/**
	 * 加入缓存
	 */
	public static void pushToCach(String key, Bitmap value) {
		if (key != null && value != null) {
			cache.put(key, value);
		}
	}

	/**
	 * 清空缓存
	 */
	public static void clearCach() {
		cache.evictAll();
		softCach.clear();
	}
}
