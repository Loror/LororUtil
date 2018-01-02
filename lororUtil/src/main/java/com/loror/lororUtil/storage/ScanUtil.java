package com.loror.lororUtil.storage;

import java.io.File;
import java.util.Locale;

import com.loror.lororUtil.flyweight.ObjectPool;

import android.os.Handler;

public class ScanUtil {
	private Handler handler;

	public interface SDCardCallBack {
		void scanOne(int find, int scaned);

		void findOne(String path);

		void finish();
	}

	private ScanUtil() {
		handler = ObjectPool.getInstance().getHandler();
	}

	private static class SingletonFactory {
		private static ScanUtil instance = new ScanUtil();
	}

	public static ScanUtil getInstance() {
		return SingletonFactory.instance;
	}

	/**
	 * 扫描sd卡，传入参数，1，起点目录，2，需扫描后缀数组，3，回调接口
	 */
	public void scanSDCard(final File rootFileDir, final String[] suffixs, final SDCardCallBack callBack) {
		new Thread() {
			public void run() {
				count = 0;
				total = 0;
				inintPaths(rootFileDir.listFiles(), suffixs, callBack);
				handler.post(new Runnable() {

					@Override
					public void run() {
						callBack.finish();
					}
				});
			};
		}.start();
	}

	private int total;// 总扫描数
	private int count;// 扫描出目标数

	/**
	 * 递归扫描
	 */
	private void inintPaths(final File[] files, String[] suffixs, final SDCardCallBack callBack) {
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					final int index = i;
					if (contains(files[i].getName(), suffixs)) {
						handler.post(new Runnable() {

							@Override
							public void run() {
								callBack.findOne(files[index].getAbsolutePath());
							}
						});
						count++;
					}
					handler.post(new Runnable() {

						@Override
						public void run() {
							callBack.scanOne(count, total++);
						}
					});
				} else {
					try {
						String get = files[i].getName();
						if (!get.startsWith(".")) {
							inintPaths(files[i].listFiles(), suffixs, callBack);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * 检查后缀
	 */
	private boolean contains(String name, String[] suffixs) {
		String uperName = name.toUpperCase(Locale.CHINA);
		for (int i = 0; i < suffixs.length; i++) {
			if (uperName.endsWith(suffixs[i].toUpperCase(Locale.CHINA))) {
				return true;
			}
		}
		return false;
	}
}
